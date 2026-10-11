"""Timing helpers for the promo clips: music start after the count-in, and the finale sync offset."""
import os, subprocess, sys
import numpy as np
F = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'tools', 'ffmpeg-9.0.2-essentials_build', 'bin', 'ffmpeg.exe')
REC = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'recordings', 'phone')


def audio(path, video_timeline=True):
    """Mono 8 kHz audio; with video_timeline the samples are placed on the video's clock (audio may start later)."""
    x = np.frombuffer(subprocess.run([F, '-v', 'error', '-i', path, '-vn', '-ac', '1', '-ar', '8000', '-f', 'f32le', '-'],
                                     capture_output=True).stdout, np.float32)
    if video_timeline:
        st = subprocess.run([F.replace('ffmpeg.exe', 'ffprobe.exe'), '-v', 'error', '-select_streams', 'a', '-show_entries',
                             'stream=start_time', '-of', 'csv=p=0', path], capture_output=True, text=True).stdout.strip()
        try:
            x = np.concatenate([np.zeros(int(float(st) * 8000), np.float32), x])
        except ValueError:
            pass
    return x


def env_db(x, w=80):
    n = len(x) // w
    return 20 * np.log10(np.sqrt(np.mean(x[: n * w].reshape(n, w) ** 2, axis=1)) + 1e-6)


def music_start(clip):
    """The count-in is a few isolated clicks; the music is the first sound that is not followed by ~0.25 s of silence."""
    e = env_db(audio(os.path.join(REC, clip)))
    on = e > -50
    for i in range(1, len(e) - 40):
        if on[i] and not on[i - 1] and on[i:i + 30].mean() > 0.9:
            return i / 100
    return None


def finale_offset(clip, finale, frm=6.0, to=13.7):
    c = env_db(audio(os.path.join(REC, clip)))
    r = env_db(audio(finale, False))
    seg = r[int(frm * 100): int(to * 100)]
    best = max((np.corrcoef(c[l:l + len(seg)], seg)[0, 1], l) for l in range(0, len(c) - len(seg)))
    return round(best[1] / 100 - frm, 2), round(best[0], 3)


if __name__ == '__main__':
    for clip in sys.argv[1:]:
        print(clip, 'music starts at', music_start(clip))
