"""Small adb helper for recording the Keyscape promo clips on a connected phone.

Usage: python promo/device.py <command> [args]
  shot <name>                 screenshot -> promo/recordings/shots/<name>.png (+ a half-size copy)
  tap x y | swipe x1 y1 x2 y2 ms | text "..." | key CODE
  demo on|off                 clean status bar (09:41, full battery, no notifications)
  rec <name> <seconds> <script.txt>
                              record a clip while running an input script, pull to promo/recordings/phone/
Script lines: "tap x y", "swipe x1 y1 x2 y2 ms", "wait s", "key CODE", "text ..."
"""
import os, subprocess, sys, threading, time

ADB = r"C:\Users\HP\AppData\Local\Android\Sdk\platform-tools\adb.exe"
SERIAL = os.environ.get("SERIAL", "29bedab4")
ROOT = os.path.dirname(os.path.abspath(__file__))
SHOTS = os.path.join(ROOT, "recordings", "shots")
PHONE = os.path.join(ROOT, "recordings", "phone")


def adb(*args, capture=False):
    cmd = [ADB, "-s", SERIAL, *args]
    if capture:
        return subprocess.run(cmd, capture_output=True).stdout
    subprocess.run(cmd, check=False)


def sh(cmd):
    adb("shell", cmd)


def shot(name):
    from PIL import Image
    os.makedirs(SHOTS, exist_ok=True)
    path = os.path.join(SHOTS, name + ".png")
    with open(path, "wb") as f:
        f.write(adb("exec-out", "screencap", "-p", capture=True))
    im = Image.open(path)
    small = im.resize((im.width // 2, im.height // 2))
    small.save(os.path.join(SHOTS, name + "-half.png"))
    print(path, im.size)


def demo(on):
    if on:
        sh("settings put global sysui_demo_allowed 1")
        for extra in ["-e command enter",
                      "-e command clock -e hhmm 0941",
                      "-e command battery -e level 100 -e plugged false",
                      "-e command network -e wifi show -e level 4 -e mobile show -e datatype none -e level 4",
                      "-e command notifications -e visible false",
                      "-e command status -e volume hide -e bluetooth hide -e alarm hide -e speakerphone hide"]:
            sh("am broadcast -a com.android.systemui.demo " + extra)
        sh("settings put system screen_off_timeout 1800000")
        sh("cmd notification set_dnd priority")
    else:
        sh("am broadcast -a com.android.systemui.demo -e command exit")
        sh("cmd notification set_dnd off")


def run_line(line):
    p = line.split()
    if not p or p[0].startswith("#"):
        return
    if p[0] == "wait":
        time.sleep(float(p[1]))
    elif p[0] == "tap":
        sh(f"input tap {p[1]} {p[2]}")
    elif p[0] == "swipe":
        sh("input swipe " + " ".join(p[1:]))
    elif p[0] == "key":
        sh("input keyevent " + p[1])
    elif p[0] == "text":
        sh("input text " + "%s".join(line.split(" ", 1)[1].split(" ")))
    elif p[0] == "shot":
        shot(p[1])


def rec(name, seconds, script, sizes=("1600x720", "1280x576")):
    os.makedirs(PHONE, exist_ok=True)
    remote = f"/sdcard/kp_{name}.mp4"
    for size in sizes:
        sh("rm -f " + remote)
        proc = subprocess.Popen([ADB, "-s", SERIAL, "shell",
            f"screenrecord --size {size} --bit-rate 24000000 --time-limit {seconds} {remote}"],
            stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
        time.sleep(1.0)
        if proc.poll() is not None:  # the encoder refused this size
            print(size, "failed:", proc.stdout.read().strip())
            continue
        for line in open(script, encoding="utf-8"):
            run_line(line.strip())
        proc.wait()
        time.sleep(0.5)
        out = os.path.join(PHONE, name + ".mp4")
        adb("pull", remote, out)
        sh("rm " + remote)
        print(out, size)
        return
    sys.exit("recording failed")


SCRCPY = os.path.join(ROOT, "tools", "scrcpy_dl", "scrcpy-win64-v3.1", "scrcpy.exe")


def rec_av(name, seconds, script):
    """Records video and the app's audio with scrcpy (Android 11+) while running an input script."""
    os.makedirs(PHONE, exist_ok=True)
    out = os.path.join(PHONE, name + ".mkv")
    if os.path.exists(out):
        os.remove(out)
    env = dict(os.environ, ADB=ADB)
    proc = subprocess.Popen([SCRCPY, "-s", SERIAL, "--no-playback", "--no-control", "--no-window",
        f"--record={out}", "--max-size=1600", "--video-bit-rate=20M", "--max-fps=90",
        "--audio-source=output", "--audio-codec=aac", "--audio-bit-rate=256K",
        f"--time-limit={seconds}"], env=env, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    time.sleep(1.5)
    for line in open(script, encoding="utf-8"):
        run_line(line.strip())
    out_text, _ = proc.communicate()
    print(out_text.strip()[-600:])
    print(out)


if __name__ == "__main__":
    c, a = sys.argv[1], sys.argv[2:]
    if c == "shot": shot(a[0])
    elif c == "demo": demo(a[0] == "on")
    elif c == "rec": rec(a[0], a[1], a[2])
    elif c == "recav": rec_av(a[0], a[1], a[2])
    elif c == "run":
        for line in open(a[0], encoding="utf-8"): run_line(line.strip())
    else: run_line(" ".join([c, *a]))
