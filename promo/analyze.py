import os, subprocess, sys
import numpy as np
F = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'tools', 'ffmpeg-9.0.2-essentials_build', 'bin', 'ffmpeg.exe')
for clip in sys.argv[1:]:
    v = subprocess.run([F, '-v', 'error', '-i', clip, '-vf', 'fps=20,scale=48:22,format=gray', '-f', 'rawvideo', '-'], capture_output=True).stdout
    fr = np.frombuffer(v, np.uint8).reshape(-1, 22 * 48).astype(float)
    d = np.abs(np.diff(fr, axis=0)).mean(axis=1)
    cuts = [(round((i + 1) / 20, 2), round(x, 1)) for i, x in enumerate(d) if x > 12]
    a = subprocess.run([F, '-v', 'error', '-i', clip, '-vn', '-ac', '1', '-ar', '8000', '-f', 's16le', '-'], capture_output=True).stdout
    s = np.frombuffer(a, np.int16).astype(float)
    env = [np.sqrt(np.mean(s[i:i + 400] ** 2)) for i in range(0, len(s) - 400, 400)] if len(s) else []
    on = [round(i * 0.05, 2) for i in range(1, len(env)) if env[i] > 150 and env[i - 1] < 60]
    print(os.path.basename(clip), 'dur', round(len(fr) / 20, 1))
    print('  screen changes:', cuts[:40])
    print('  sound onsets  :', on[:40])
