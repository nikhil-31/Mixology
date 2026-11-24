"""Small adb helpers for driving the app on an emulator: find on-screen elements by text or
content description, tap, type and swipe. Used by record_demo_video.py.

Run on its own to list what's on screen:  python3 scripts/demo_adb.py
"""
import os
import re
import subprocess
import time

SERIAL = os.environ.get("ANDROID_SERIAL", "emulator-5554")
PACKAGE = "com.capstone.nik.mixology"


def adb(*args, check=True, text=True):
    return subprocess.run(["adb", "-s", SERIAL, *args], check=check, capture_output=True, text=text)


def shell(*args):
    return adb("shell", *args).stdout


def nodes():
    """Every on-screen node as (text, desc, (cx, cy), (x1, y1, x2, y2))."""
    for _ in range(6):
        if adb("shell", "uiautomator", "dump", "/sdcard/ui.xml", check=False).returncode == 0:
            break
        time.sleep(0.5)
    xml = shell("cat", "/sdcard/ui.xml")
    found = []
    for node in re.findall(r"<node [^>]*>", xml):
        t = re.search(r'text="([^"]*)"', node)
        d = re.search(r'content-desc="([^"]*)"', node)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', node)
        if not b:
            continue
        x1, y1, x2, y2 = map(int, b.groups())
        found.append((t.group(1) if t else "", d.group(1) if d else "", ((x1 + x2) // 2, (y1 + y2) // 2), (x1, y1, x2, y2)))
    return found


def find(text=None, desc=None, contains=False, index=0):
    def hit(value, wanted):
        return wanted is not None and ((wanted in value) if contains else (value == wanted))

    start = time.time()
    matches = [n for n in nodes() if hit(n[0], text) or hit(n[1], desc)]
    LOOKUPS.append((start, time.time()))
    return matches[index][2] if len(matches) > index else None


def wait_for(text=None, desc=None, contains=False, seconds=20):
    deadline = time.time() + seconds
    while time.time() < deadline:
        point = find(text, desc, contains)
        if point:
            return point
        time.sleep(0.5)
    raise SystemExit(f"timed out waiting for text={text!r} desc={desc!r}")


def tap_at(x, y, pause=1.0):
    shell("input", "tap", str(x), str(y))
    time.sleep(pause)


# Each lookup is a UI dump of about 3 s with nothing moving on screen. They're logged so the
# video can cut them out.
LOOKUPS = []


def tap(text=None, desc=None, contains=False, index=0, pause=1.0, seconds=20):
    deadline = time.time() + seconds
    while True:
        point = find(text, desc, contains, index)
        if point:
            tap_at(*point, pause=pause)
            return point
        if time.time() > deadline:
            raise SystemExit(f"nothing to tap: text={text!r} desc={desc!r}")
        time.sleep(0.5)


def swipe(x1, y1, x2, y2, ms=500, pause=0.8):
    shell("input", "swipe", str(x1), str(y1), str(x2), str(y2), str(ms))
    time.sleep(pause)


def type_text(value, chunk=3, gap=0.35):
    """The emulator drops keys when text arrives too fast, so it goes a few characters at a time."""
    for i in range(0, len(value), chunk):
        part = value[i:i + chunk].replace(" ", "%s")
        shell("input", "text", part)
        time.sleep(gap)


def key(code, pause=0.6):
    shell("input", "keyevent", str(code))
    time.sleep(pause)


def hide_keyboard():
    key(111)  # Escape


if __name__ == "__main__":
    for text, desc, center, _ in nodes():
        if text or desc:
            print(f"{center}\ttext={text!r}\tdesc={desc!r}")
