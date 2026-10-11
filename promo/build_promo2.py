"""Keyscape promo video - sample 2, the cinematic cut.

A separate build from build_promo.py (sample 1 and its video are left as they are). Same phone recordings and app
sound, with a more polished finish:
  - background: dark walnut stage with slowly drifting golden bokeh, and a continuous, gentle camera move
  - the phone has a glass sheen; between phone shots the screen goes soft (blur dissolve) while a light sweeps across
    the glass, and whole-scene changes use a blur dissolve
  - captions: a small gold section label, title words that rise in one after another, a gold rule that draws
    itself, then the subtitle; a section label stays on screen while consecutive shots share it
  - music: Gymnopedie under the opening, the app's own sound for the three songs, Bach's Prelude in C running
    through the second half (library, stories, lessons, practice tools) and Chopin's 'Revolutionary' Etude ending,
    in sync with the phone, over the finale and the end card
Output: promo/out/keyscape-promo-sample2-1080p60.mp4, captions-sample2.srt, thumbnail-sample2.png
Run:    python promo/build_promo2.py [--preview] [--workers N]
"""
import json, math, os, shutil, subprocess, sys, wave
import multiprocessing as mp
import numpy as np
from PIL import Image, ImageChops, ImageDraw, ImageFilter

import build_promo as bp

OUT, REC, FF = bp.OUT, bp.REC, bp.FF
W, H, FPS, SR = 1920, 1080, 60, 48000
XF = 1.4                      # every transition (s)
PREVIEW = "--preview" in sys.argv
NAME = "keyscape-promo-sample2"
TMP = os.path.join(OUT, "sample2-tmp")

CREAM, INK, CAPTION, SUB, GOLD = bp.CREAM, bp.INK, bp.CAPTION, bp.SUB, bp.GOLD

# Sound levels (RMS of the audible part, dBFS) before the final -14 LUFS normalisation.
SONG_DB, GYM_DB, BACH_DB, FINALE_DB = -20.0, -27.5, -24.5, -18.0
FINALE = os.path.join(OUT, "audio", "finale.wav")
FIN_LAST = 13.63      # render time of the last chord of the finale
FIN_FROM = 4.0        # the excerpt starts just before a soft chord, building to the fortissimo 1.5 s later
FIN_OFFSET = 7.26     # 13-revolutionary.mkv time = finale render time + this (aligned on the closing chords)

# Caption layout (y of baselines).
EYEBROW_BASE, TITLE_BASE, RULE_Y, SUB_BASE = 52, 120, 140, 182


def smooth(x):
    return bp.smooth(x)


def ease_out(x):
    x = min(1.0, max(0.0, x))
    return 1 - (1 - x) ** 3


# ----------------------------------------------------------------------------------------------- bokeh
class Bokeh:
    """Soft out-of-focus lights drifting slowly upwards with a gentle sway; positions depend only on time."""

    def __init__(self, n, colours, alpha, seed):
        rng = np.random.default_rng(seed)
        self.p = []
        for _ in range(n):
            r = float(np.exp(rng.uniform(np.log(7), np.log(64))))
            col = colours[rng.integers(len(colours))]
            a = rng.uniform(*alpha) * (0.75 if r > 40 else 1.0)
            self.p.append(dict(r=r, x=rng.uniform(0, W), y=rng.uniform(0, H + 2 * r), v=rng.uniform(5, 16) * (1.25 - r / 90),
                               amp=rng.uniform(8, 34), f=rng.uniform(0.05, 0.16), ph=rng.uniform(0, 6.28),
                               spr=self.sprite(r, col, a)))

    @staticmethod
    def sprite(r, col, a):
        s = int(2 * r + 6)
        yy, xx = np.mgrid[0:s, 0:s]
        d = np.hypot(xx - s / 2, yy - s / 2) / r
        edge = np.clip((1 - d) / 0.22, 0, 1)                 # soft edge
        body = 0.72 + 0.28 * np.clip((d - 0.55) / 0.45, 0, 1)  # slightly brighter rim, like a lens
        alpha = (edge * body * a * 255).astype(np.uint8)
        img = Image.new("RGBA", (s, s), col + (0,))
        img.putalpha(Image.fromarray(alpha, "L"))
        return img

    def draw(self, img, t):
        for q in self.p:
            span = H + 2 * q["r"]
            y = (q["y"] - q["v"] * t) % span - q["r"]
            x = q["x"] + q["amp"] * math.sin(q["f"] * 2 * math.pi * t + q["ph"])
            s = q["spr"]
            img.paste(s, (int(x - s.width / 2), int(y - s.height / 2)), s)


# ----------------------------------------------------------------------------------------------- glass and light
def glass(sw, sh, radius, strength=1.0):
    """A faint diagonal sheen and top highlight on a screen (RGB colour + L mask inside the rounded screen)."""
    yy, xx = np.mgrid[0:sh, 0:sw].astype(np.float32)
    u = (xx + 0.55 * yy) / (sw + 0.55 * sh)
    a = np.exp(-((u - 0.2) / 0.085) ** 2) * 0.05 + np.exp(-((u - 0.33) / 0.022) ** 2) * 0.03
    a += np.clip(1 - yy / (sh * 0.3), 0, 1) ** 2 * 0.03
    m = Image.fromarray(np.clip(a * 255 * strength, 0, 255).astype(np.uint8), "L")
    return Image.new("RGB", (sw, sh), (255, 248, 236)), ImageChops.multiply(m, bp.rounded_mask((sw, sh), radius))


SWEEP_U = None


def light_sweep(img, p, peak=0.2):
    """A soft band of warm light crossing the screen during a transition."""
    global SWEEP_U
    w, h = img.size
    if SWEEP_U is None or SWEEP_U.shape != (h, w):
        yy, xx = np.mgrid[0:h, 0:w].astype(np.float32)
        SWEEP_U = (xx + 0.5 * yy) / (w + 0.5 * h)
    c = -0.25 + 1.5 * p
    a = np.exp(-((SWEEP_U - c) / 0.07) ** 2) * peak * math.sin(math.pi * min(1, max(0, p)))
    if a.max() < 0.004:
        return img
    m = Image.fromarray((a * 255).astype(np.uint8), "L")
    out = img.copy()
    out.paste((255, 244, 222), (0, 0), m)
    return out


def soft(img, small):
    """A strongly blurred copy (downscaled blur, cheap)."""
    return img.resize(small, Image.BILINEAR).filter(ImageFilter.GaussianBlur(2.4)).resize(img.size, Image.BICUBIC)


def dissolve(a, b, p, small):
    """Blur dissolve: a goes soft, the soft images cross, b comes into focus."""
    ka = smooth(p / 0.6)
    kb = 1 - smooth((p - 0.4) / 0.6)
    A = Image.blend(a, soft(a, small), ka) if ka > 0.003 else a
    B = Image.blend(b, soft(b, small), kb) if kb > 0.003 else b
    return Image.blend(A, B, smooth((p - 0.28) / 0.44))


# ----------------------------------------------------------------------------------------------- text
def text_sprite(text, f, fill, tracking=0.0, shadow=0.6, pad=16):
    asc, desc = f.getmetrics()
    width = (sum(f.getlength(c) for c in text) + tracking * (len(text) - 1)) if tracking else f.getlength(text)
    w, h = int(math.ceil(width)) + 2 * pad, asc + desc + 2 * pad
    m = Image.new("L", (w, h), 0)
    d = ImageDraw.Draw(m)
    if tracking:
        x = pad
        for c in text:
            d.text((x, pad + asc), c, font=f, fill=255, anchor="ls")
            x += f.getlength(c) + tracking
    else:
        d.text((pad, pad + asc), text, font=f, fill=255, anchor="ls")
    out = Image.new("RGBA", (w, h), (12, 7, 4, 0))
    if shadow:
        out.putalpha(m.filter(ImageFilter.GaussianBlur(6)).point(lambda v: int(min(255, v * shadow * 1.4))))
    out.paste(Image.new("RGBA", (w, h), fill + (255,)), (0, 0), m)
    return out, pad, asc, width


def paste_alpha(img, spr, x, y, a):
    if a <= 0.004:
        return
    l = bp.with_alpha(spr, a)
    img.paste(l, (int(round(x)), int(round(y))), l)


class Caption:
    """Title words rise in one after another, a gold rule draws itself, then the subtitle fades up."""

    def __init__(self, t0, t1, title, sub):
        self.t0, self.t1 = t0, t1
        ft, fs = bp.font(52, bold=True), bp.font(29, italic=True)
        space = ft.getlength(" ")
        words, x = [], 0.0
        for w_ in title.split(" "):
            spr, pad, asc, wd = text_sprite(w_, ft, CAPTION)
            words.append((spr, x - pad))
            x += wd + space
        left = (W - (x - space)) / 2
        self.words = [(s, left + xo) for s, xo in words]
        self.title_top = TITLE_BASE - 16 - ft.getmetrics()[0]
        self.sub = None
        if sub:
            spr, pad, asc, wd = text_sprite(sub, fs, SUB)
            self.sub = (spr, (W - wd) / 2 - pad, SUB_BASE - pad - asc)
        self.rule = Image.new("RGBA", (80, 3), GOLD + (255,))

    def draw(self, img, t):
        if t < self.t0 - 0.02 or t > self.t1 + 0.02:
            return
        e = smooth((t - (self.t1 - 0.5)) / 0.5)
        out, lift = 1 - e, -8 * e
        for i, (spr, x) in enumerate(self.words):
            s = t - (self.t0 + 0.12 + 0.07 * i)
            paste_alpha(img, spr, x, self.title_top + 22 * (1 - ease_out(s / 0.75)) + lift, smooth(s / 0.55) * out)
        s = t - (self.t0 + 0.4)
        rw = int(80 * ease_out(s / 0.8))
        if rw > 1:
            r = self.rule.crop((40 - rw // 2, 0, 40 - rw // 2 + rw, 3))
            paste_alpha(img, r, W / 2 - rw / 2, RULE_Y + lift, out)
        if self.sub:
            spr, x, y = self.sub
            s = t - (self.t0 + 0.55)
            paste_alpha(img, spr, x, y + 10 * (1 - ease_out(s / 0.8)) + lift, smooth(s / 0.6) * out)


class Eyebrow:
    """The small gold section label above the title."""

    def __init__(self, t0, t1, text):
        self.t0, self.t1 = t0, t1
        spr, pad, asc, wd = text_sprite(text, bp.font(21, bold=True), GOLD, tracking=4.6, shadow=0.55)
        self.spr, self.x, self.y = spr, (W - wd) / 2 - pad, EYEBROW_BASE - pad - asc

    def draw(self, img, t):
        if t < self.t0 - 0.02 or t > self.t1 + 0.02:
            return
        a = smooth((t - self.t0) / 0.5) * (1 - smooth((t - (self.t1 - 0.45)) / 0.45))
        paste_alpha(img, self.spr, self.x, self.y + 8 * (1 - ease_out((t - self.t0) / 0.6)), a)


class LogoBug:
    """A small Keyscape logo and name in the top-left corner during the app footage."""

    def __init__(self, t0, t1):
        self.t0, self.t1 = t0, t1
        logo = Image.open(bp.LOGO).convert("RGBA").resize((50, 50), Image.LANCZOS)
        name, pad, asc, wd = text_sprite("Keyscape", bp.font(27, bold=True), CAPTION, shadow=0.5)
        hh = max(50, name.height) + 4
        self.spr = Image.new("RGBA", (50 + 12 + name.width, hh), (0, 0, 0, 0))
        self.spr.paste(logo, (0, (hh - 50) // 2), logo)
        self.spr.alpha_composite(name, (62 - pad, (hh - name.height) // 2))

    def draw(self, img, t):
        a = 0.88 * smooth((t - self.t0) / 1.0) * (1 - smooth((t - (self.t1 - 1.0)) / 1.0))
        paste_alpha(img, self.spr, 44, 30, a)


# ----------------------------------------------------------------------------------------------- shots
STAGE = CREAM_BG = PHONE = TABLET_DEV = PHONE_SMALL = None
STAGE_BOKEH = CREAM_BOKEH = None
PHONE_POS = (0, 0)
GLASS = GLASS_SMALL = GLASS_TAB = None


def init_globals():
    global STAGE, CREAM_BG, PHONE, TABLET_DEV, PHONE_SMALL, STAGE_BOKEH, CREAM_BOKEH, PHONE_POS, GLASS, GLASS_SMALL, GLASS_TAB
    stage = np.asarray(bp.stage_background()).astype(np.float32)
    yy, xx = np.mgrid[0:H, 0:W].astype(np.float32)
    vig = 1 - 0.38 * np.clip(np.hypot((xx - W / 2) / (W * 0.62), (yy - H * 0.55) / (H * 0.7)) - 0.35, 0, 1) ** 1.6
    STAGE = Image.fromarray(np.clip(stage * vig[..., None], 0, 255).astype(np.uint8), "RGB")
    CREAM_BG = bp.cream_background()
    STAGE_BOKEH = Bokeh(38, [(236, 192, 122), (222, 156, 84), (250, 228, 186)], (0.05, 0.19), 11)
    CREAM_BOKEH = Bokeh(22, [(214, 168, 78), (226, 150, 92)], (0.035, 0.09), 12)
    PHONE = bp.Device(1600, 720, 24, 70, 44)
    PHONE_POS = ((W - PHONE.size[0]) // 2, H - PHONE.size[1] - 52)
    TABLET_DEV = bp.Device(1200, 750, 34, 44, 14)
    PHONE_SMALL = bp.Device(960, 432, 16, 40, 26)
    GLASS = glass(1600, 720, 44)
    GLASS_SMALL = glass(960, 432, 26)
    GLASS_TAB = glass(1200, 750, 14, 0.8)


def draw_device(img, dev, screen, x, y, gl):
    dev.draw(img, screen, x, y)
    img.paste(gl[0], (x + dev.b, y + dev.b), gl[1])


def phone_frame(screen, gt):
    img = STAGE.copy()
    STAGE_BOKEH.draw(img, gt)
    draw_device(img, PHONE, screen, *PHONE_POS, GLASS)
    return img


class Shot:
    dur = 6.0
    kind = "scene"
    caps = ()          # (t0, t1, eyebrow, title, subtitle) in shot time
    sound = None
    reader = None

    def start(self, offset=0.0):
        pass

    def stop(self):
        if self.reader:
            self.reader.close()
            self.reader = None


class PhoneShot(Shot):
    kind = "phone"

    def __init__(self, clip, t_in, dur, caps, sound=None, gate=None):
        self.clip, self.t_in, self.dur, self.caps, self.sound, self.gate = clip, t_in, dur, caps, sound, gate

    def start(self, offset=0.0):
        self.reader = bp.ClipReader(self.clip, self.t_in + offset, self.dur - offset, PHONE.sw, PHONE.sh)

    def screen(self, t):
        # The outgoing screen holds still during the transition.
        if t > self.dur - XF and self.reader.last is not None:
            return self.reader.last
        s = self.reader.next()
        if PREVIEW:
            self.reader.skip(5)
        return s


class Intro(Shot):
    """The app's launch screen: the logo settles in, light glints across it, then the tagline fades in beneath."""
    dur = 5.6
    N = 500

    def start(self, offset=0.0):
        self.logo = Image.open(bp.LOGO).convert("RGBA").resize((self.N, self.N), Image.LANCZOS)
        self.tag = bp.tagline_image(self.N, INK)
        sh = Image.new("L", (self.N + 160, self.N + 160), 0)
        ImageDraw.Draw(sh).rounded_rectangle([80, 98, 80 + self.N, 98 + self.N], int(self.N * 0.24), fill=90)
        self.shadow = sh.filter(ImageFilter.GaussianBlur(26))

    def frame(self, t, gt):
        img = CREAM_BG.copy()
        CREAM_BOKEH.draw(img, gt)
        n = self.N
        a = smooth(t / 0.9)
        s = 0.9 + 0.1 * ease_out(t / 1.6)
        lw = int(n * s)
        cx, cy = W // 2, 430
        if a > 0:
            shadow = self.shadow.resize((int(self.shadow.width * s), int(self.shadow.height * s))).point(lambda v: int(v * a))
            img.paste((70, 40, 20), (cx - shadow.width // 2, cy - shadow.height // 2), shadow)
            logo = shine(self.logo, (t - 1.7) / 0.9).resize((lw, lw), Image.LANCZOS)
            paste_alpha(img, logo, cx - lw // 2, cy - lw // 2, a)
        ta = smooth((t - 1.1) / 0.9)
        paste_alpha(img, self.tag, cx - self.tag.width // 2, cy + n // 2 + 40 + 16 * (1 - ease_out((t - 1.1) / 1.0)), ta)
        return img


def shine(logo, p):
    """A diagonal glint of light across the logo (p from 0 to 1)."""
    if not (0 < p < 1):
        return logo
    w, h = logo.size
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float32)
    u = (xx + 0.6 * yy) / (w + 0.6 * h)
    band = np.exp(-((u - (-0.2 + 1.4 * p)) / 0.06) ** 2) * 0.55 * math.sin(math.pi * p)
    m = Image.fromarray((band * np.asarray(logo.getchannel("A"), np.float32)).astype(np.uint8), "L")
    out = logo.copy()
    out.paste((255, 250, 238, 255), (0, 0), m)
    out.putalpha(logo.getchannel("A"))
    return out


class TabletShot(Shot):
    """The tablet (the app's 10" tablet layout) with the phone in front playing the finale piece in sync."""
    kind = "scene"

    def __init__(self, clip, dur, caps):
        self.clip, self.dur, self.caps, self.t_in = clip, dur, caps, 0.0

    def start(self, offset=0.0):
        self.tab = Image.open(bp.TABLET).convert("RGB").resize((TABLET_DEV.sw, TABLET_DEV.sh), Image.LANCZOS)
        self.reader = bp.ClipReader(self.clip, self.t_in + offset, self.dur - offset, PHONE_SMALL.sw, PHONE_SMALL.sh)

    def frame(self, t, gt):
        img = STAGE.copy()
        STAGE_BOKEH.draw(img, gt)
        k = ease_out(t / 2.0)
        draw_device(img, TABLET_DEV, self.tab, 96 - int(26 * (1 - k)), 216, GLASS_TAB)
        if t > self.dur - XF and self.reader.last is not None:
            scr = self.reader.last
        else:
            scr = self.reader.next()
            if PREVIEW:
                self.reader.skip(5)
        draw_device(img, PHONE_SMALL, scr, W - PHONE_SMALL.size[0] - 58 + int(34 * (1 - k)), H - PHONE_SMALL.size[1] - 40, GLASS_SMALL)
        return img


class EndCard(Shot):
    dur = 6.8

    def start(self, offset=0.0):
        self.logo = Image.open(bp.LOGO).convert("RGBA").resize((360, 359), Image.LANCZOS)
        self.tag = bp.tagline_image(360, INK)
        b = Image.open(bp.BADGE).convert("RGBA")
        self.badge = b.resize((int(b.width * 120 / b.height), 120), Image.LANCZOS)
        self.sub, pad, asc, wd = text_sprite("Learn piano with the music you love", bp.font(34), (110, 72, 44), shadow=0)
        self.sub_x = (W - wd) / 2 - pad

    def frame(self, t, gt):
        img = CREAM_BG.copy()
        CREAM_BOKEH.draw(img, gt)
        cx = W // 2
        logo = shine(self.logo, (t - 1.1) / 0.9)
        for layer, x, y, t0 in ((logo, cx - 180, 140, 0.0), (self.tag, cx - self.tag.width // 2, 140 + 359 + 30, 0.4),
                                (self.sub, self.sub_x, 610, 0.8), (self.badge, cx - self.badge.width // 2, 744, 1.1)):
            a = smooth((t - t0) / 0.7)
            paste_alpha(img, layer, x, y + 14 * (1 - ease_out((t - t0) / 0.9)), a)
        f = smooth((t - (self.dur - 1.7)) / 1.7)
        if f > 0:
            img = Image.blend(img, Image.new("RGB", (W, H), (0, 0, 0)), f)
        return img


def storyboard():
    return [
        Intro(),
        PhoneShot("09-composers-story.mkv", 0.6, 7.0,
                  [(1.0, 6.1, "COMPOSERS", "Meet the great composers", "Mozart, Beethoven, Bach, Chopin, Debussy and more")]),
        PhoneShot("01-fur-elise.mkv", 1.65, 23.0,
                  [(1.0, 5.0, "YOUR MUSIC", "Learn piano with the music you love", "Pick a piece, a level and a way to follow it"),
                   (5.6, 13.0, "WATERFALL MODE", "Falling notes, in time with the music", "Beethoven – Für Elise"),
                   (13.6, 22.0, "LISTEN MODE", "Watch each hand on its own", "Right hand, left hand or both – the music keeps playing")],
                  sound="song", gate=6.76),
        PhoneShot("04-sheet-music.mkv", 2.75, 9.6,
                  [(1.0, 8.7, "SHEET MUSIC MODE", "Or read real sheet music", "Mozart – Rondo alla Turca")], sound="song"),
        PhoneShot("03-moonlight-presto.mkv", 6.54, 11.6,
                  [(1.0, 10.7, "FALLEN NOTE MODE", "Silky-smooth, even at full speed", "Beethoven – Moonlight Sonata, 3rd movement")],
                  sound="song", gate=7.44),
        PhoneShot("05-song-library.mkv", 0.8, 9.4,
                  [(1.0, 8.5, "SONG LIBRARY", "222 complete pieces", "Classical, romantic, ragtime, folk, holiday and more")]),
        PhoneShot("09-composers-story.mkv", 15.2, 9.0,
                  [(1.0, 8.1, "STORYBOOKS", "Illustrated storybooks", "The lives behind the music")]),
        PhoneShot("08-lesson.mkv", 6.3, 9.8,
                  [(1.0, 8.9, "LESSONS", "Step-by-step lessons", "172 lessons, from your very first note")]),
        PhoneShot("06b-chord-explorer.mkv", 0.6, 11.2,
                  [(1.0, 10.3, "PRACTICE TOOLS", "Any chord, in any key", "See it on the staff and the keys, and hear it")]),
        PhoneShot("10b-chord-finder.mkv", 5.5, 9.4,
                  [(1.0, 8.5, "PRACTICE TOOLS", "Chord Finder", "Play any notes – get the chord name")]),
        PhoneShot("11b-circle.mkv", 0.8, 12.6,
                  [(1.0, 11.7, "PRACTICE TOOLS", "Circle of Fifths", "Every key, its chords and cadences – tap to hear")]),
        PhoneShot("12b-scales.mkv", 0.6, 11.0,
                  [(1.0, 10.1, "PRACTICE TOOLS", "23 scales and modes", "On the staff and the keys, and played for you")]),
        PhoneShot("07-ear-trainer.mkv", 1.2, 8.0,
                  [(1.0, 7.1, "PRACTICE TOOLS", "Train your ear", "Intervals, chords and cadences")]),
        TabletShot("13-revolutionary.mkv", 11.8,
                   [(1.0, 5.6, "EVERY SCREEN", "On your phone and tablet", "Plug in a USB MIDI keyboard to play on real keys"),
                    (6.0, 10.9, "MASTERPIECES", "Play the masterpieces", "Chopin – ‘Revolutionary’ Étude")]),
        EndCard(),
    ]


def timeline():
    shots = storyboard()
    starts, t = [], 0.0
    for i, s in enumerate(shots):
        starts.append(t)
        t += s.dur - (XF if i < len(shots) - 1 else 0)
    total = starts[-1] + shots[-1].dur
    fin_at = starts[-1] - 0.15 - FIN_LAST   # global time of the finale render's t = 0: its last chord lands as the end card begins
    for s, st in zip(shots, starts):
        if isinstance(s, TabletShot):
            s.t_in = (st - fin_at) + FIN_OFFSET
    return shots, starts, total, fin_at


def overlays(shots, starts):
    caps, brows = [], []
    for s, st in zip(shots, starts):
        for t0, t1, eb, title, sub in s.caps:
            caps.append((st + t0, st + t1, eb, title, sub))
    items = [Caption(a, b, title, sub) for a, b, eb, title, sub in caps]
    # One label per run of captions that share it, so it stays on screen across those shots.
    for a, b, eb, title, sub in caps:
        if brows and brows[-1][2] == eb and a - brows[-1][1] < 3.5:
            brows[-1][1] = b
        else:
            brows.append([a, b, eb])
    items += [Eyebrow(a, b, eb) for a, b, eb in brows]
    items.append(LogoBug(starts[1] + XF * 0.6, starts[-1] + 0.4))
    return items, caps


def camera(img, gt):
    """A slow, continuous push-in and drift (never jumps between shots)."""
    z = 1.0 + 0.02 * (0.5 - 0.5 * math.cos(2 * math.pi * gt / 19.0))
    w, h = W / z, H / z
    cx = min(max(W / 2 + 9 * math.sin(gt / 7.3), w / 2), W - w / 2)
    cy = min(max(H / 2 + 5 * math.sin(gt / 9.1 + 1.0), h / 2), H - h / 2)
    return img.resize((W, H), Image.BICUBIC, box=(cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2))


def compose(shots, active, gt, items):
    if len(active) == 1:
        i, t = active[0]
        s = shots[i]
        base = phone_frame(s.screen(t), gt) if s.kind == "phone" else s.frame(t, gt)
    else:
        (ia, ta), (ib, tb) = active[0], active[1]
        a, b = shots[ia], shots[ib]
        p = tb / XF
        if a.kind == "phone" and b.kind == "phone":
            scr = dissolve(a.screen(ta), b.screen(tb), p, (200, 90))
            base = phone_frame(light_sweep(scr, p), gt)
        else:
            fa = phone_frame(a.screen(ta), gt) if a.kind == "phone" else a.frame(ta, gt)
            fb = phone_frame(b.screen(tb), gt) if b.kind == "phone" else b.frame(tb, gt)
            base = dissolve(fa, fb, p, (240, 135))
    img = camera(base, gt)
    for it in items:
        it.draw(img, gt)
    return img


# ----------------------------------------------------------------------------------------------- render (parallel)
def render_chunk(args):
    idx, f0, f1, step = args
    init_globals()
    shots, starts, total, fin_at = timeline()
    items, _ = overlays(shots, starts)
    path = os.path.join(TMP, f"seg{idx:02d}.mp4")
    enc = subprocess.Popen([FF, "-v", "error", "-y", "-f", "rawvideo", "-pix_fmt", "rgb24", "-s", f"{W}x{H}", "-r", str(FPS // step), "-i", "-",
                            "-c:v", "libx264", "-preset", "veryfast" if PREVIEW else "slow", "-crf", "16", "-tune", "film",
                            "-x264-params", "threads=4", "-pix_fmt", "yuv420p", "-profile:v", "high", "-g", "120", "-bf", "2", path],
                           stdin=subprocess.PIPE)
    state = {}
    for f in range(f0, f1, step):
        gt = f / FPS
        active = [(i, gt - st) for i, (s, st) in enumerate(zip(shots, starts)) if st - 1e-9 <= gt < st + s.dur]
        live = {i for i, _ in active}
        for i in [k for k, v in state.items() if v and k not in live]:
            shots[i].stop(); state[i] = False
        for i, t in active:
            if i not in state:
                shots[i].start(t); state[i] = True
        enc.stdin.write(compose(shots, active, gt, items).tobytes())
    enc.stdin.close()
    enc.wait()
    for i, v in state.items():
        if v:
            shots[i].stop()
    return path


def chunk_bounds(starts, total, n, step):
    """Split points away from transitions (each chunk starts its shots mid-way, outside any cross-fade)."""
    bad = [(st - 0.3, st + XF + 0.3) for st in starts[1:]]
    frames = int(round(total * FPS))
    cuts = [0]
    for k in range(1, n):
        t = k * total / n
        while any(a <= t <= b for a, b in bad):
            t += 0.25
        f = int(round(t * FPS)) // step * step
        if cuts[-1] < f < frames:
            cuts.append(f)
    cuts.append(frames)
    return sorted(set(cuts))


# ----------------------------------------------------------------------------------------------- audio
def place(mix, x, at):
    i0 = int(round(at * SR))
    j0 = max(0, -i0)
    i0 = max(0, i0)
    i1 = min(len(mix), i0 + len(x) - j0)
    if i1 > i0:
        mix[i0:i1] += x[j0: j0 + (i1 - i0)]


def ramp(x, a, b, up=True):
    """Cosine fade between sample positions a and b; silence before (up) or after (down)."""
    a, b = max(0, int(a)), max(0, int(b))
    if up:
        x[:a] = 0
        if b > a:
            x[a:b] *= bp.cos_ramp(b - a)
    else:
        if b > a:
            x[a:b] *= bp.cos_ramp(b - a, up=False)
        x[b:] = 0
    return x


def build_audio(shots, starts, total, fin_at):
    n = int(math.ceil(total * SR))
    mix = np.zeros(n, np.float32)
    idx = {getattr(s, "clip", type(s).__name__): i for i, s in enumerate(shots)}
    # The three songs: the app's own sound. Count-in clicks muted; each song leaves before the next comes in.
    for s, st in zip(shots, starts):
        if s.sound != "song":
            continue
        x = bp.read_audio(os.path.join(REC, s.clip), s.t_in, s.dur + 1.0)
        if s.gate is not None:
            g = (s.gate - s.t_in) * SR
            ramp(x, g, g + 0.25 * SR)
        else:
            ramp(x, 0.7 * SR, 1.25 * SR)
        x *= min(40.0, 10 ** ((SONG_DB - bp.level_db(x)) / 20))
        ramp(x, (s.dur - 1.4) * SR, (s.dur - 0.55) * SR, up=False)
        place(mix, x, st)
    # Gymnopedie under the opening, gone just before Fur Elise begins.
    fur = idx["01-fur-elise.mkv"]
    fur_music = starts[fur] + shots[fur].gate - shots[fur].t_in
    x = bp.read_audio(os.path.join(OUT, "audio", "gymnopedie.wav"))[: int((fur_music + 1) * SR)]
    x *= 10 ** ((GYM_DB - bp.level_db(x)) / 20)
    ramp(x, 0, 1.5 * SR)
    ramp(x, (fur_music - 1.9) * SR, (fur_music - 0.2) * SR, up=False)
    place(mix, x, 0.0)
    # Bach's Prelude in C from the song library to the practice tools: it begins exactly as Moonlight falls silent.
    lib = idx["05-song-library.mkv"]
    tab = next(i for i, s in enumerate(shots) if isinstance(s, TabletShot))
    bach_at = starts[lib] + XF - 0.25
    tab_st = starts[tab]
    x = bp.read_audio(os.path.join(OUT, "audio", "prelude-c.wav"))[: int((tab_st + 1 - bach_at) * SR)]
    x *= 10 ** ((BACH_DB - bp.level_db(x)) / 20)
    ramp(x, 0, 0.25 * SR)
    ramp(x, (tab_st - 1.75 - bach_at) * SR, (tab_st + 0.45 - bach_at) * SR, up=False)
    place(mix, x, bach_at)
    # The finale: Chopin's 'Revolutionary' Etude, in sync with the phone, the last chord ringing over the end card.
    x = bp.read_audio(FINALE, FIN_FROM)
    x *= 10 ** ((FINALE_DB - bp.level_db(x)) / 20)
    ramp(x, 0, 0.35 * SR)
    place(mix, x, fin_at + FIN_FROM)
    # Fade out with the picture.
    fo = int(2.4 * SR)
    mix[-fo:] *= bp.cos_ramp(fo, up=False)
    raw = os.path.join(TMP, "mix-raw.wav")
    with wave.open(raw, "wb") as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR)
        w.writeframes((np.clip(mix, -1, 1) * 32767).astype("<i2").tobytes())
    r = subprocess.run([FF, "-hide_banner", "-i", raw, "-af", "loudnorm=I=-14:TP=-1.5:LRA=11:print_format=json", "-f", "null", "-"],
                       capture_output=True, text=True).stderr
    m = json.loads(r[r.rindex("{"): r.rindex("}") + 1])
    out = os.path.join(OUT, "promo-mix-sample2.wav")
    subprocess.run([FF, "-v", "error", "-y", "-i", raw, "-af",
                    f"loudnorm=I=-14:TP=-1.5:LRA=11:measured_I={m['input_i']}:measured_TP={m['input_tp']}:"
                    f"measured_LRA={m['input_lra']}:measured_thresh={m['input_thresh']}:offset={m['target_offset']}:linear=true",
                    "-ar", str(SR), out])
    return out


def write_srt(caps):
    lines = []
    for k, (a, b, eb, title, sub) in enumerate(caps, 1):
        lines += [str(k), f"{bp.srt_time(a)} --> {bp.srt_time(b)}", title + (f"\n{sub}" if sub else ""), ""]
    with open(os.path.join(OUT, "captions-sample2.srt"), "w", encoding="utf-8") as f:
        f.write("\n".join(lines))


def thumbnail(shots):
    """1280x720 thumbnail: logo and tagline beside the phone playing the Moonlight Presto."""
    moon = next(s for s in shots if getattr(s, "clip", "") == "03-moonlight-presto.mkv")
    moon.start(5.0)
    scr = moon.screen(0.0)
    moon.stop()
    img = STAGE.copy()
    STAGE_BOKEH.draw(img, 3.0)
    dev = bp.Device(1120, 504, 18, 48, 30)
    draw_device(img, dev, scr.resize((dev.sw, dev.sh), Image.LANCZOS), W - dev.size[0] - 60, (H - dev.size[1]) // 2 + 40, glass(1120, 504, 30))
    logo = Image.open(bp.LOGO).convert("RGBA").resize((420, 419), Image.LANCZOS)
    img.paste(logo, (120, 250), logo)
    tag = bp.tagline_image(420, CAPTION)
    img.paste(tag, (120, 250 + 419 + 28), tag)
    t, pad, asc, wd = text_sprite("Learn piano", bp.font(64, bold=True), CAPTION)
    img.paste(t, (int(120 + 210 - wd / 2 - pad), 96), t)
    img.resize((1280, 720), Image.LANCZOS).save(os.path.join(OUT, "thumbnail-sample2.png"))


def main():
    workers = int(sys.argv[sys.argv.index("--workers") + 1]) if "--workers" in sys.argv else 10
    os.makedirs(TMP, exist_ok=True)
    init_globals()
    shots, starts, total, fin_at = timeline()
    print(f"total {total:.2f}s, {len(shots)} shots; finale clip in-point {[s.t_in for s in shots if isinstance(s, TabletShot)][0]:.2f}s")
    items, caps = overlays(shots, starts)
    write_srt(caps)
    audio = build_audio(shots, starts, total, fin_at)
    step = 6 if PREVIEW else 1
    cuts = chunk_bounds(starts, total, workers, step)
    jobs = [(k, cuts[k], cuts[k + 1], step) for k in range(len(cuts) - 1)]
    print(f"rendering {len(jobs)} chunks in parallel")
    with mp.Pool(len(jobs)) as pool:
        segs = pool.map(render_chunk, jobs)
    lst = os.path.join(TMP, "segments.txt")
    with open(lst, "w") as f:
        f.writelines(f"file '{os.path.basename(p)}'\n" for p in segs)
    out = os.path.join(OUT, f"{NAME}-preview.mp4" if PREVIEW else f"{NAME}-1080p60.mp4")
    subprocess.run([FF, "-v", "error", "-y", "-f", "concat", "-safe", "0", "-i", lst, "-i", audio,
                    "-map", "0:v", "-map", "1:a", "-c:v", "copy", "-c:a", "aac", "-b:a", "320k", "-ar", "48000", "-ac", "2",
                    "-shortest", "-movflags", "+faststart", out], check=True)
    if not PREVIEW:
        thumbnail(shots)
    shutil.rmtree(TMP, ignore_errors=True)
    print(out)


if __name__ == "__main__":
    main()
