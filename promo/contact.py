"""Contact sheet of stills from a video: python promo/contact.py video.mp4 out.png t1 t2 ..."""
import os, subprocess, sys
from PIL import Image
F = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'tools', 'ffmpeg-9.0.2-essentials_build', 'bin', 'ffmpeg.exe')
video, out = sys.argv[1], sys.argv[2]
ts = [float(x) for x in sys.argv[3:]]
ims = []
for i, t in enumerate(ts):
    p = out + f'.{i}.png'
    subprocess.run([F, '-v', 'error', '-y', '-ss', str(t), '-i', video, '-frames:v', '1', '-vf', 'scale=480:-1', p])
    ims.append(Image.open(p)); 
cols = 4
m = Image.new('RGB', (480 * cols, 270 * ((len(ims) + cols - 1) // cols)))
for i, im in enumerate(ims): m.paste(im, ((i % cols) * 480, (i // cols) * 270))
m.save(out)
for i in range(len(ts)): os.remove(out + f'.{i}.png')
