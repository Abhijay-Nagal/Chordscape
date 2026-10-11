"""Builds the Keyscape Google Play promo video from the phone recordings and the app's own assets.

Inputs:  promo/recordings/phone/*.mkv          (scrcpy clips of a real phone, 1600x720, with the app's own sound)
         promo/out/audio/gymnopedie.wav         (background music, rendered with the app's piano engine by PromoTest)
         composeApp/build/screenshots/devices/tablet-home.png (tablet render, ScreenshotTest)
Output:  promo/out/keyscape-promo-1080p60.mp4, captions.srt, thumbnail.png
Run:     python promo/build_promo.py            (add --preview for a quick 10 fps draft)

Sound rules: the app's own sound (songs, chords, ear training) is never mixed with the background music - the
music fades out completely before the app sound starts and fades back in only after it has finished.
"""
import json, math, os, re, subprocess, sys, wave
import numpy as np
from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(ROOT)
FF = os.path.join(ROOT, "tools", "ffmpeg-9.0.2-essentials_build", "bin", "ffmpeg.exe")
REC = os.path.join(ROOT, "recordings", "phone")
OUT = os.path.join(ROOT, "out")
FONT_DIR = os.path.join(REPO, "composeApp", "src", "commonMain", "composeResources", "font")
LOGO = os.path.join(REPO, "composeApp", "src", "commonMain", "composeResources", "drawable", "keyscape_logo.png")
BADGE = os.path.join(ROOT, "tools", "google-play-badge.png")
TABLET = os.path.join(REPO, "composeApp", "build", "screenshots", "devices", "tablet-home.png")

W, H, FPS = 1920, 1080, 60
SR = 48000
XF = 1.3  # transition between shots (s); the outgoing screen holds still while it fades
CAP_GAP = 0.95  # captions are off screen for this long either side of every shot change (a short pause)
PREVIEW = "--preview" in sys.argv

CREAM = (252, 244, 229)
INK = (58, 28, 16)
CAPTION = (248, 236, 214)
SUB = (222, 190, 140)
GOLD = (214, 168, 78)
TAGLINE = "Play Beyond the Ordinary"

# Loudness targets (RMS of the audible part, dBFS) before the final -14 LUFS normalisation.
SONG_DB = -20.0
TOOL_DB = -21.0
BED_DB = -29.0   # background music sits well below the app's own sound


def font(size, italic=False, bold=False):
    f = ImageFont.truetype(os.path.join(FONT_DIR, "librebaskerville_italic.ttf" if italic else "librebaskerville.ttf"), size)
    if bold:
        try:
            f.set_variation_by_name("Bold")
        except Exception:
            pass
    return f


def smooth(x):
    """Smootherstep easing 0..1."""
    x = min(1.0, max(0.0, x))
    return x * x * x * (x * (x * 6 - 15) + 10)


# ----------------------------------------------------------------------------------------------- static layers
def stage_background():
    """Dark walnut gradient with a warm glow behind the device (the app's stage colours)."""
    y = np.linspace(0, 1, H)[:, None]
    x = np.linspace(-1, 1, W)[None, :]
    top, bottom = np.array([28, 17, 11]), np.array([50, 31, 19])
    base = top[None, None, :] * (1 - y[..., None]) + bottom[None, None, :] * y[..., None]
    r = np.sqrt((x * 0.85) ** 2 + ((y - 0.6) * 1.5) ** 2)
    glow = np.clip(1 - r, 0, 1)[..., None] ** 2 * np.array([66, 40, 19])[None, None, :]
    # Very fine noise so the gradient never bands after compression.
    noise = np.random.default_rng(3).normal(0, 1.2, (H, W, 1))
    return Image.fromarray(np.clip(base + glow + noise, 0, 255).astype(np.uint8), "RGB")


def cream_background():
    y = np.linspace(-1, 1, H)[:, None]
    x = np.linspace(-1, 1, W)[None, :]
    r = np.sqrt((x * 0.8) ** 2 + (y * 1.0) ** 2)
    c = np.array(CREAM, float)[None, None, :] - (np.clip(r - 0.35, 0, 1) ** 1.5)[..., None] * np.array([22, 26, 30])[None, None, :]
    noise = np.random.default_rng(4).normal(0, 0.8, (H, W, 1))
    return Image.fromarray(np.clip(c + noise, 0, 255).astype(np.uint8), "RGB")


def rounded_mask(size, radius):
    m = Image.new("L", size, 0)
    ImageDraw.Draw(m).rounded_rectangle([0, 0, size[0] - 1, size[1] - 1], radius, fill=255)
    return m


class Device:
    """A device frame (black bezel, rounded screen) with a soft shadow, around a screen of sw x sh pixels."""

    def __init__(self, sw, sh, bezel, radius, screen_radius):
        self.sw, self.sh, self.b = sw, sh, bezel
        ow, oh = sw + 2 * bezel, sh + 2 * bezel
        self.size = (ow, oh)
        frame = Image.new("RGBA", (ow, oh), (0, 0, 0, 0))
        d = ImageDraw.Draw(frame)
        d.rounded_rectangle([0, 0, ow - 1, oh - 1], radius, fill=(14, 12, 11, 255), outline=(78, 70, 63, 255), width=3)
        d.rounded_rectangle([3, 3, ow - 4, oh - 4], radius - 3, outline=(34, 30, 28, 255), width=2)
        hole = rounded_mask((sw, sh), screen_radius)
        alpha = frame.getchannel("A")
        alpha.paste(0, (bezel, bezel), hole)
        frame.putalpha(alpha)
        self.frame = frame
        self.screen_mask = hole
        pad = 100
        sh_img = Image.new("L", (ow + 2 * pad, oh + 2 * pad), 0)
        ImageDraw.Draw(sh_img).rounded_rectangle([pad, pad + 28, pad + ow, pad + oh + 28], radius, fill=150)
        self.shadow = sh_img.filter(ImageFilter.GaussianBlur(38))
        self.pad = pad

    def draw(self, canvas, screen, x, y):
        canvas.paste((0, 0, 0), (x - self.pad, y - self.pad, x - self.pad + self.shadow.width, y - self.pad + self.shadow.height), self.shadow)
        canvas.paste(screen, (x + self.b, y + self.b), self.screen_mask)
        canvas.paste(self.frame, (x, y), self.frame)


def caption_layer(title, sub=None, title_size=56, sub_size=31, color=CAPTION, sub_color=SUB):
    """A caption: title, a short gold rule and an optional subtitle, centred, as an RGBA image."""
    ft, fs = font(title_size, bold=True), font(sub_size, italic=True)
    d0 = ImageDraw.Draw(Image.new("RGBA", (10, 10)))
    tb = d0.textbbox((0, 0), title, font=ft)
    sb = d0.textbbox((0, 0), sub, font=fs) if sub else (0, 0, 0, 0)
    w = max(tb[2] - tb[0], sb[2] - sb[0]) + 60
    th, sh = tb[3] - tb[1], sb[3] - sb[1]
    h = 12 + th + (36 + sh if sub else 0) + 16
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.text(((w - (tb[2] - tb[0])) / 2 - tb[0], 12 - tb[1]), title, font=ft, fill=color + (255,))
    if sub:
        ry = 12 + th + 16
        d.rounded_rectangle([w / 2 - 34, ry, w / 2 + 34, ry + 3], 2, fill=GOLD + (255,))
        d.text(((w - (sb[2] - sb[0])) / 2 - sb[0], ry + 20 - sb[1]), sub, font=fs, fill=sub_color + (255,))
    return img


def tagline_image(width, color):
    """The tagline in italic Baskerville whose ink is exactly [width] pixels wide (as under the logo in the app)."""
    f = font(100, italic=True)
    bb = ImageDraw.Draw(Image.new("RGBA", (10, 10))).textbbox((0, 0), TAGLINE, font=f)
    f = font(int(round(100 * width / (bb[2] - bb[0]))), italic=True)
    bb = ImageDraw.Draw(Image.new("RGBA", (10, 10))).textbbox((0, 0), TAGLINE, font=f)
    img = Image.new("RGBA", (bb[2] - bb[0] + 4, bb[3] - bb[1] + 4), (0, 0, 0, 0))
    ImageDraw.Draw(img).text((2 - bb[0], 2 - bb[1]), TAGLINE, font=f, fill=color + (255,))
    return img


def with_alpha(img, a):
    if a >= 0.999:
        return img
    r = img.copy()
    r.putalpha(r.getchannel("A").point(lambda v: int(v * max(0.0, a))))
    return r


# ----------------------------------------------------------------------------------------------- clips
def clean_status_bars(a):
    """Paints out the phone's status bar (clock, battery, signal) and the navigation buttons on the right.
    The system bars are shown exactly when the middle of the top band is a flat colour (any theme, light or dark);
    the full-screen song player draws its toolbar there instead and is left untouched."""
    h, w = a.shape[:2]
    band = a[int(h * 2 / 720): int(h * 44 / 720), int(w * 0.25): int(w * 0.75)].mean(axis=2)
    if band.std() > 6:
        return a
    top = int(round(h * 56 / 720))      # status bar height (app content starts at ~61 px of 720)
    # One flat colour (the app background just below), blended over 6 px into the content - no streaks.
    colour = np.median(a[top + 2: top + 10, int(w * 0.05): int(w * 0.95)].reshape(-1, 3), axis=0)
    a[:top] = colour
    for k in range(6):
        f = (k + 1) / 7
        a[top + k] = (a[top + k] * f + colour * (1 - f)).astype(np.uint8)
    right = int(round(w * 1508 / 1600))  # navigation buttons column, including the app edge line at ~1512 px of 1600
    a[:, right:] = a[:, right - 3: right - 2]  # continue the app background from just inside its edge
    return a


class ClipReader:
    """Streams a recording's frames at 60 fps scaled to (w, h), with the status bars removed; holds the last frame."""

    def __init__(self, name, t_in, dur, w, h):
        self.w, self.h = w, h
        self.p = subprocess.Popen([FF, "-v", "error", "-ss", f"{t_in:.3f}", "-t", f"{dur + 1.5:.3f}", "-i", os.path.join(REC, name),
                                   "-vf", f"fps={FPS}:round=near,scale={w}:{h}:flags=lanczos", "-f", "rawvideo", "-pix_fmt", "rgb24", "-"],
                                  stdout=subprocess.PIPE, stderr=subprocess.DEVNULL)
        self.last = None

    def next(self):
        n = self.w * self.h * 3
        buf = self.p.stdout.read(n)
        if len(buf) == n:
            a = np.frombuffer(buf, np.uint8).reshape(self.h, self.w, 3).copy()
            self.last = Image.fromarray(clean_status_bars(a), "RGB")
        return self.last

    def skip(self, frames):
        for _ in range(frames):
            self.p.stdout.read(self.w * self.h * 3)

    def close(self):
        try:
            self.p.stdout.close()
        except Exception:
            pass
        self.p.kill()
        self.p.wait()


# ----------------------------------------------------------------------------------------------- shots
STAGE = CREAM_BG = PHONE = TABLET_DEV = PHONE_SMALL = None
PHONE_POS = (0, 0)
CAPTION_Y = 46


class Shot:
    dur = 6.0
    captions = ()      # [(t0, t1, title, subtitle)] in shot time
    clip = None
    t_in = 0.0
    sound = None       # None (background music) / "app" (the recording's own sound)
    gate = None        # clip time from which the recording's sound is used (mutes the count-in clicks)
    def start(self): pass
    def frame(self, t): raise NotImplementedError
    def stop(self):
        r = getattr(self, "reader", None)
        if r:
            r.close()

    def draw_captions(self, img, t):
        for k, (t0, t1, title, sub) in enumerate(self.captions):
            t0 = max(t0, CAP_GAP)
            t1 = min(t1, self.dur - CAP_GAP)
            if not (t0 - 0.05 <= t <= t1 + 0.05):
                continue
            layer = self.cap_layers[k]
            a = smooth((t - t0) / 0.6) * (1 - smooth((t - (t1 - 0.55)) / 0.55))
            if a <= 0:
                continue
            dy = int(round(16 * (1 - smooth((t - t0) / 0.8))))
            img.paste(with_alpha(layer, a), ((W - layer.width) // 2, CAPTION_Y + dy), with_alpha(layer, a))

    def prepare_captions(self):
        self.cap_layers = [caption_layer(c[2], c[3]) for c in self.captions]


class Intro(Shot):
    """The app's launch screen: the logo settles in, then the tagline fades in beneath it at the logo's width."""
    dur = 5.2
    LOGO_PX = 500

    def start(self):
        n = self.LOGO_PX
        self.logo = Image.open(LOGO).convert("RGBA").resize((n, n), Image.LANCZOS)
        self.tag = tagline_image(n, INK)

    def frame(self, t):
        img = CREAM_BG.copy()
        n = self.LOGO_PX
        a = smooth(t / 0.8)
        s = 0.9 + 0.1 * smooth(t / 1.4)
        lw = int(n * s)
        logo = with_alpha(self.logo.resize((lw, lw), Image.LANCZOS), a)
        cx, cy = W // 2, 440
        img.paste(logo, (cx - lw // 2, cy - lw // 2), logo)
        ta = smooth((t - 1.0) / 0.8)
        if ta > 0:
            tag = with_alpha(self.tag, ta)
            img.paste(tag, (cx - tag.width // 2, cy + n // 2 + 40 + int(16 * (1 - ta))), tag)
        return img


class PhoneShot(Shot):
    def __init__(self, clip, t_in, dur, captions, sound=None, gate=None, tail=-0.3):
        self.clip, self.t_in, self.dur, self.captions, self.sound, self.gate, self.tail = clip, t_in, dur, captions, sound, gate, tail

    def start(self):
        self.reader = ClipReader(self.clip, self.t_in, self.dur, PHONE.sw, PHONE.sh)
        self.prepare_captions()

    def frame(self, t):
        img = STAGE.copy()
        scr = self.reader.last if (t > self.dur - XF and self.reader.last is not None) else self.reader.next()
        if PREVIEW and t <= self.dur - XF:
            self.reader.skip(5)
        PHONE.draw(img, scr, *PHONE_POS)
        self.draw_captions(img, t)
        return img


class TabletShot(Shot):
    """Tablet (rendered at a real 10" tablet's layout) with the phone in front, both showing Keyscape.
    The phone plays the finale piece; its in-point is set in main() so the falling notes match the finale audio."""
    sync_finale = True

    def __init__(self, clip, t_in, dur, captions):
        self.clip, self.t_in, self.dur, self.captions = clip, t_in, dur, captions

    def start(self):
        self.tab = Image.open(TABLET).convert("RGB").resize((TABLET_DEV.sw, TABLET_DEV.sh), Image.LANCZOS)
        self.reader = ClipReader(self.clip, self.t_in, self.dur, PHONE_SMALL.sw, PHONE_SMALL.sh)
        self.prepare_captions()

    def frame(self, t):
        img = STAGE.copy()
        k = smooth(t / 1.4)
        TABLET_DEV.draw(img, self.tab, 110 - int(24 * (1 - k)), 210)
        scr = self.reader.last if (t > self.dur - XF and self.reader.last is not None) else self.reader.next()
        if PREVIEW and t <= self.dur - XF:
            self.reader.skip(5)
        PHONE_SMALL.draw(img, scr, W - PHONE_SMALL.size[0] - 60 + int(30 * (1 - k)), H - PHONE_SMALL.size[1] - 40)
        self.draw_captions(img, t)
        return img


class EndCard(Shot):
    dur = 6.4

    def start(self):
        self.logo = Image.open(LOGO).convert("RGBA").resize((360, 359), Image.LANCZOS)
        self.tag = tagline_image(360, INK)
        b = Image.open(BADGE).convert("RGBA")
        self.badge = b.resize((int(b.width * 120 / b.height), 120), Image.LANCZOS)
        self.sub = caption_layer("Learn piano with the music you love", None, title_size=34, color=(110, 72, 44))

    def frame(self, t):
        img = CREAM_BG.copy()
        cx = W // 2
        for layer, (x, y), t0 in ((self.logo, (cx - 180, 150), 0.0), (self.tag, (cx - self.tag.width // 2, 150 + 359 + 30), 0.4),
                                  (self.sub, (cx - self.sub.width // 2, 636), 0.8), (self.badge, (cx - self.badge.width // 2, 760), 1.1)):
            a = smooth((t - t0) / 0.6)
            if a > 0:
                l = with_alpha(layer, a)
                img.paste(l, (x, y + int(12 * (1 - a))), l)
        return img


def storyboard():
    S = lambda *c: tuple(c)
    return [
        Intro(),
        PhoneShot("09-composers-story.mkv", 0.6, 6.8,
                  [S(0.3, 6.5, "Meet the great composers", "Mozart, Beethoven, Bach, Chopin, Debussy and more")]),
        PhoneShot("01-fur-elise.mkv", 3.2, 22.6,
                  [S(0.2, 5.0, "Learn piano with the music you love", "Pick a piece, a level and a way to follow it"),
                   S(5.4, 13.6, "Falling notes, in time with the music", "Beethoven - Fur Elise"),
                   S(14.0, 22.3, "Watch each hand on its own", "Right hand, left hand or both - the music keeps playing")],
                  sound="app", gate=7.95),
        PhoneShot("04-sheet-music.mkv", 2.2, 8.6,
                  [S(0.3, 8.3, "Or read real sheet music", "Mozart - Rondo alla Turca")], sound="app", gate=2.2),
        PhoneShot("03-moonlight-presto.mkv", 7.0, 10.8,
                  [S(0.3, 10.5, "Silky-smooth, even at full speed", "Beethoven - Moonlight Sonata, 3rd movement")],
                  sound="app", gate=7.3, tail=1.4),
        PhoneShot("05-song-library.mkv", 0.8, 8.8,
                  [S(0.3, 8.5, "222 complete pieces", "Classical, romantic, ragtime, folk, holiday and more")]),
        PhoneShot("09-composers-story.mkv", 15.2, 8.4,
                  [S(0.3, 8.1, "Illustrated storybooks", "The lives behind the music")]),
        PhoneShot("08-lesson.mkv", 6.3, 9.2,
                  [S(0.3, 8.9, "Step-by-step lessons", "172 lessons, from your very first note")]),
        PhoneShot("06-chord-explorer.mkv", 2.0, 7.0,
                  [S(0.3, 6.7, "Any chord, in any key", "See it on the staff and the keys, and hear it")], sound="app"),
        PhoneShot("10-chord-finder.mkv", 3.0, 6.0,
                  [S(0.3, 5.7, "Chord Finder", "Play any notes - get the chord name")], sound="app"),
        PhoneShot("11-circle.mkv", 3.0, 8.4,
                  [S(0.3, 8.1, "Circle of Fifths", "Every key, its chords and cadences - tap to hear")], sound="app"),
        PhoneShot("12-scales.mkv", 2.2, 7.0,
                  [S(0.3, 6.7, "23 scales and modes", "On the staff and the keys, and played for you")], sound="app"),
        PhoneShot("07-ear-trainer.mkv", 1.0, 7.4,
                  [S(0.3, 7.1, "Train your ear", "Intervals, chords and cadences")], sound="app"),
        TabletShot("13-revolutionary.mkv", 0.0, 9.2,
                   [S(0.3, 4.6, "On your phone and tablet", "Plug in a USB MIDI keyboard to play on real keys"),
                    S(4.9, 9.0, "Play the masterpieces", "Chopin - 'Revolutionary' Etude")]),
        EndCard(),
    ]


# The finale: the ending of Chopin's "Revolutionary" Etude, rendered by the app's piano (PromoTest). The closing
# chords are seen falling on the phone; the last one rings out over the end card.
FINALE = os.path.join(OUT, "audio", "finale.wav")
FINALE_FROM = 5.6     # render time where the excerpt starts (a fortissimo chord)
FINALE_HIT = 13.63    # render time of the last chord: it lands just as the end card starts to fade in
FINALE_CLIP_OFFSET = 6.25  # 13-revolutionary.mkv video time = finale render time + this (aligned on the closing chords)
FINALE_DB = -18.0


# ----------------------------------------------------------------------------------------------- audio
def read_audio(path, t_in=0.0, dur=None):
    args = [FF, "-v", "error", "-ss", f"{t_in:.3f}"] + (["-t", f"{dur:.3f}"] if dur else []) + \
           ["-i", path, "-vn", "-ac", "1", "-ar", str(SR), "-f", "f32le", "-"]
    return np.frombuffer(subprocess.run(args, capture_output=True).stdout, dtype=np.float32).copy()


def level_db(x):
    """RMS level of the audible part (10 ms windows above -50 dBFS)."""
    w = 480
    n = len(x) // w
    if n == 0:
        return -90.0
    e = np.sqrt(np.mean(x[: n * w].reshape(n, w).astype(np.float64) ** 2, axis=1))
    loud = e[e > 10 ** (-50 / 20)]
    return 20 * math.log10(np.sqrt(np.mean(loud ** 2))) if len(loud) else -90.0


def cos_ramp(n, up=True):
    r = 0.5 - 0.5 * np.cos(np.linspace(0, math.pi, max(n, 1)))
    return r if up else r[::-1]


def sound_events(x, gap=1.5):
    """Spans (seconds) where the recording is audible, joining sounds less than [gap] seconds apart."""
    w = 480
    m = len(x) // w
    if m == 0:
        return []
    e = np.sqrt(np.mean(x[: m * w].reshape(m, w).astype(np.float64) ** 2, axis=1)) > 10 ** (-52 / 20)
    spans, start, last = [], None, None
    for i, on in enumerate(e):
        if on:
            if start is None:
                start = i
            elif (i - last) * 0.01 > gap:
                spans.append((start * 0.01, last * 0.01 + 0.01)); start = i
            last = i
    if start is not None:
        spans.append((start * 0.01, last * 0.01 + 0.01))
    return spans


def build_audio(shots, starts, total):
    n = int(math.ceil(total * SR))
    app = np.zeros(n, np.float32)
    events = []  # global [a, b] where the app's own sound plays
    for shot, s in zip(shots, starts):
        if shot.sound != "app":
            continue
        x = read_audio(os.path.join(REC, shot.clip), shot.t_in, shot.dur + max(0.0, shot.tail) + 1.0)
        if not len(x):
            continue
        if shot.gate is not None:  # silence the count-in clicks before the music
            g = max(0, int((shot.gate - shot.t_in) * SR))
            x[:g] = 0
            f = int(0.03 * SR)
            x[g:g + f] *= cos_ramp(min(f, len(x) - g))
        target = SONG_DB if shot.gate is not None or "moonlight" in shot.clip else TOOL_DB
        gain = 10 ** ((target - level_db(x)) / 20)
        x *= min(gain, 40.0)
        # Sound enters as the new screen appears (0.75-1.15 s) and leaves before the next shot
        # starts (by default fading out 1.0-0.3 s before the end), so there is a short pause at every change.
        a0, a1 = int(0.75 * SR), int(1.15 * SR)
        x[:a0] = 0
        x[a0:a1] *= cos_ramp(a1 - a0)
        end = min(int((shot.dur + shot.tail) * SR), len(x))
        fo = int((0.7 if shot.tail <= 0 else shot.tail + 0.6) * SR)
        x = x[:end]
        x[-fo:] *= cos_ramp(fo, up=False)
        i0 = int(s * SR)
        i1 = min(n, i0 + len(x))
        app[i0:i1] += x[: i1 - i0]
        events.extend((s + a, s + b) for a, b in sound_events(x))
    if os.path.exists(FINALE):
        x = read_audio(FINALE, FINALE_FROM)
        x *= 10 ** ((FINALE_DB - level_db(x)) / 20)
        fi = int(0.25 * SR)
        x[:fi] *= cos_ramp(fi)
        at = starts[-1] - 0.15 - (FINALE_HIT - FINALE_FROM)
        i0 = int(at * SR)
        i1 = min(n, i0 + len(x))
        x = x[: i1 - i0]
        fo = int(0.6 * SR)
        x[-fo:] *= cos_ramp(fo, up=False)
        app[i0:i1] += x
        events.extend((at + a0, at + b0) for a0, b0 in sound_events(x))
    # Background music level per shot: full under silent shots, a quiet texture in the gaps of the practice-tool
    # shots, none under the songs. Shot changes glide over 1.5 s.
    base = np.zeros(n, np.float32)
    for k, (shot, s) in enumerate(zip(shots, starts)):
        lvl = 1.0 if shot.sound != "app" else (0.0 if (shot.gate is not None or "moonlight" in shot.clip) else 0.25)
        a = s + (XF / 2 if k > 0 else 0)
        b = s + shot.dur - (XF / 2 if k < len(shots) - 1 else 0)
        base[int(a * SR): int(b * SR)] = lvl
    k = int(1.5 * SR)
    base = np.convolve(base, np.ones(k, np.float32) / k, mode="same")
    # And it is completely silent whenever the app makes a sound: fades out over 1.2 s before, back in over 1.2 s after.
    mute = np.ones(n, np.float32)
    pre, post = int(1.2 * SR), int(1.2 * SR)
    for a, b in events:
        ia, ib = int(a * SR), min(n, int(b * SR))
        lo = max(0, ia - pre)
        mute[lo:ia] = np.minimum(mute[lo:ia], cos_ramp(pre, up=False)[pre - (ia - lo):])
        mute[ia:ib] = 0
        hi = min(n, ib + post)
        mute[ib:hi] = np.minimum(mute[ib:hi], cos_ramp(post)[: hi - ib])
    bed_gain = base * mute
    fi = int(1.0 * SR)
    bed_gain[:fi] *= cos_ramp(fi)
    fo = int(3.0 * SR)
    bed_gain[-fo:] *= cos_ramp(fo, up=False)
    merged = events
    # The bed keeps its own continuous timeline, so it resumes musically where it would be.
    bed = read_audio(os.path.join(OUT, "audio", "gymnopedie.wav"))
    bed = np.pad(bed, (0, max(0, n - len(bed))))[:n]
    bed *= 10 ** ((BED_DB - level_db(bed)) / 20)
    mix = app + bed * bed_gain
    raw = os.path.join(OUT, "promo-mix-raw.wav")
    with wave.open(raw, "wb") as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR)
        w.writeframes((np.clip(mix, -1, 1) * 32767).astype("<i2").tobytes())
    # Two-pass loudness normalisation to -14 LUFS (linear, so no pumping).
    r = subprocess.run([FF, "-hide_banner", "-i", raw, "-af", "loudnorm=I=-14:TP=-1.5:LRA=11:print_format=json", "-f", "null", "-"],
                       capture_output=True, text=True).stderr
    m = json.loads(r[r.rindex("{"): r.rindex("}") + 1])
    out = os.path.join(OUT, "promo-mix.wav")
    subprocess.run([FF, "-v", "error", "-y", "-i", raw, "-af",
                    f"loudnorm=I=-14:TP=-1.5:LRA=11:measured_I={m['input_i']}:measured_TP={m['input_tp']}:"
                    f"measured_LRA={m['input_lra']}:measured_thresh={m['input_thresh']}:offset={m['target_offset']}:linear=true",
                    "-ar", str(SR), out])
    print("app sound:", [(round(a, 1), round(b, 1)) for a, b in merged])
    return out


# ----------------------------------------------------------------------------------------------- captions file
def srt_time(t):
    ms = int(round(t * 1000))
    return f"{ms // 3600000:02d}:{ms // 60000 % 60:02d}:{ms // 1000 % 60:02d},{ms % 1000:03d}"


def write_srt(shots, starts):
    lines, k = [], 1
    for shot, s in zip(shots, starts):
        for t0, t1, title, sub in shot.captions:
            lines += [str(k), f"{srt_time(s + t0)} --> {srt_time(s + t1)}", title + (f"\n{sub}" if sub else ""), ""]
            k += 1
    with open(os.path.join(OUT, "captions.srt"), "w", encoding="utf-8") as f:
        f.write("\n".join(lines))


# ----------------------------------------------------------------------------------------------- thumbnail
def thumbnail(still_path):
    img = STAGE.copy()
    still = Image.open(still_path).convert("RGB")
    dev = Device(1120, 504, 18, 48, 30)
    x0, y0 = PHONE_POS[0] + PHONE.b, PHONE_POS[1] + PHONE.b
    scr = still.crop((x0, y0, x0 + PHONE.sw, y0 + PHONE.sh)).resize((dev.sw, dev.sh), Image.LANCZOS)
    dev.draw(img, scr, W - dev.size[0] - 60, (H - dev.size[1]) // 2 + 40)
    logo = Image.open(LOGO).convert("RGBA").resize((420, 419), Image.LANCZOS)
    img.paste(logo, (120, 250), logo)
    tag = tagline_image(420, CAPTION)
    img.paste(tag, (120, 250 + 419 + 28), tag)
    title = caption_layer("Learn piano", None, title_size=64)
    img.paste(title, (120 + 210 - title.width // 2, 120), title)
    img.resize((1280, 720), Image.LANCZOS).save(os.path.join(OUT, "thumbnail.png"))


# ----------------------------------------------------------------------------------------------- transitions
def screen_colour(frame):
    x0, y0 = PHONE_POS[0] + PHONE.b, PHONE_POS[1] + PHONE.b
    small = np.asarray(frame.crop((x0, y0, x0 + PHONE.sw, y0 + PHONE.sh)).resize((40, 18)))
    return np.median(small.reshape(-1, 3), axis=0)


def blank_phone(colour):
    img = STAGE.copy()
    PHONE.draw(img, Image.new("RGB", (PHONE.sw, PHONE.sh), tuple(int(c) for c in colour)), *PHONE_POS)
    return img


def page_change(out_frame, in_frame, p):
    """Phone-to-phone change: the old screen fades to a plain screen, rests a moment, then the new one fades in."""
    colour = (screen_colour(out_frame) + screen_colour(in_frame)) / 2
    blank = blank_phone(colour)
    if p < 0.38:
        return Image.blend(out_frame, blank, smooth(p / 0.38))
    if p < 0.62:
        return blank
    return Image.blend(blank, in_frame, smooth((p - 0.62) / 0.38))


# ----------------------------------------------------------------------------------------------- main
def main():
    global STAGE, CREAM_BG, PHONE, TABLET_DEV, PHONE_SMALL, PHONE_POS
    os.makedirs(OUT, exist_ok=True)
    STAGE = stage_background()
    CREAM_BG = cream_background()
    PHONE = Device(1600, 720, 24, 70, 44)        # the recordings at their native 1600x720 - pixel for pixel
    PHONE_POS = ((W - PHONE.size[0]) // 2, H - PHONE.size[1] - 52)
    TABLET_DEV = Device(1200, 750, 34, 44, 14)    # 16:10 tablet
    PHONE_SMALL = Device(960, 432, 16, 40, 26)

    shots = storyboard()
    starts, t = [], 0.0
    for i, s in enumerate(shots):
        starts.append(t)
        t += s.dur - (XF if i < len(shots) - 1 else 0)
    total = starts[-1] + shots[-1].dur
    print(f"total {total:.2f}s, {len(shots)} shots")
    # The phone in the tablet shot plays the finale piece in sync with the finale audio.
    at = starts[-1] - 0.15 - (FINALE_HIT - FINALE_FROM)
    for sh, st in zip(shots, starts):
        if getattr(sh, "sync_finale", False):
            sh.t_in = (st - at) + FINALE_FROM + FINALE_CLIP_OFFSET
            print(f"finale clip in-point {sh.t_in:.2f}s")

    audio = build_audio(shots, starts, total)
    write_srt(shots, starts)

    out = os.path.join(OUT, "keyscape-promo-preview.mp4" if PREVIEW else "keyscape-promo-1080p60.mp4")
    fps = FPS // 6 if PREVIEW else FPS
    enc = subprocess.Popen([FF, "-v", "error", "-y", "-f", "rawvideo", "-pix_fmt", "rgb24", "-s", f"{W}x{H}", "-r", str(fps), "-i", "-",
                            "-i", audio,
                            "-c:v", "libx264", "-preset", "veryfast" if PREVIEW else "slow", "-crf", "16", "-tune", "film",
                            "-pix_fmt", "yuv420p", "-profile:v", "high", "-g", "120", "-bf", "2", "-movflags", "+faststart",
                            "-c:a", "aac", "-b:a", "320k", "-ar", "48000", "-ac", "2", "-shortest", out], stdin=subprocess.PIPE)
    step = 6 if PREVIEW else 1
    frames = int(round(total * FPS))
    state = {}
    thumb_t = starts[1] + 6.0
    thumb_path = os.path.join(OUT, "frame-moonlight.png")
    for f in range(0, frames, step):
        gt = f / FPS
        layers = [(i, s, gt - st) for i, (s, st) in enumerate(zip(shots, starts)) if st - 1e-9 <= gt < st + s.dur]
        live = {i for i, _, _ in layers}
        for i in [k for k, v in state.items() if v and k not in live]:
            shots[i].stop(); state[i] = False
        img = None
        prev = None
        for i, s, lt in layers:
            if i not in state:
                s.start(); state[i] = True
            fr = s.frame(lt)
            if img is None:
                img, prev = fr, s
            elif isinstance(prev, PhoneShot) and isinstance(s, PhoneShot):
                img = page_change(img, fr, lt / XF)
            else:
                img = Image.blend(img, fr, smooth(lt / XF))
        if abs(gt - thumb_t) < 0.5 * step / FPS:
            img.save(thumb_path)
        enc.stdin.write(img.tobytes())
        if f % (FPS * 10) < step:
            print(f"  {gt:5.1f}s", flush=True)
    enc.stdin.close()
    enc.wait()
    for i, v in state.items():
        if v:
            shots[i].stop()
    if os.path.exists(thumb_path):
        thumbnail(thumb_path)
    print(out)


if __name__ == "__main__":
    main()
