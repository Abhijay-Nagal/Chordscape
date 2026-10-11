import subprocess, sys, os
from PIL import Image
F = os.path.join(os.path.dirname(__file__), 'tools', 'ffmpeg-9.0.2-essentials_build', 'bin', 'ffmpeg.exe')
clip, out = sys.argv[1], sys.argv[2]
ts = [float(x) for x in sys.argv[3:]]
ims = []
for i, t in enumerate(ts):
    p = out + f'_{i}.png'
    subprocess.run([F, '-v', 'error', '-y', '-ss', str(t), '-i', clip, '-frames:v', '1', '-vf', 'scale=640:-1', p])
    if os.path.exists(p): ims.append(Image.open(p)); 
w, h = ims[0].size
m = Image.new('RGB', (w * 2, h * ((len(ims) + 1) // 2)), 'white')
for i, im in enumerate(ims): m.paste(im, ((i % 2) * w, (i // 2) * h))
m.save(out + '.png')
for i in range(len(ts)):
    p = out + f'_{i}.png'
    if os.path.exists(p): os.remove(p)
