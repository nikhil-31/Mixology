import argparse
import os
import re
import subprocess
import time
from pathlib import Path

DEFAULT_OUT = Path("docs/screenshots/play-store")


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument("--serial", default=os.environ.get("ANDROID_SERIAL"))
    parser.add_argument("--out", default=str(DEFAULT_OUT))
    return parser.parse_args()


ARGS = parse_args()
SERIAL = ARGS.serial
OUT = Path(ARGS.out)


def adb(*args, check=True):
    cmd = ["adb"]
    if SERIAL:
        cmd.extend(["-s", SERIAL])
    cmd.extend(args)
    return subprocess.run(cmd, check=check, capture_output=True)


def dump_xml():
    last = None
    for _ in range(6):
        result = subprocess.run(
            (["adb", "-s", SERIAL] if SERIAL else ["adb"])
            + ["shell", "uiautomator", "dump", "/sdcard/ui.xml"],
            capture_output=True,
            text=True,
        )
        last = result
        if result.returncode == 0:
            local = f"/tmp/mix-ui-{SERIAL or 'default'}.xml"
            adb("pull", "/sdcard/ui.xml", local)
            return Path(local).read_text(errors="replace")
        time.sleep(1.0)
    raise SystemExit(f"uiautomator dump failed: {last.returncode} {last.stderr}")


def parse_bounds(s):
    m = re.search(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", s or "")
    if not m:
        return None
    x1, y1, x2, y2 = map(int, m.groups())
    return (x1 + x2) // 2, (y1 + y2) // 2


def tap(text=None, desc=None, contains=False, index=0, required=True):
    xml = dump_xml()
    matches = []
    for m in re.finditer(r"<node [^>]*>", xml):
        node = m.group(0)
        t = re.search(r'text="([^"]*)"', node)
        d = re.search(r'content-desc="([^"]*)"', node)
        b = re.search(r'bounds="([^"]*)"', node)
        tv = t.group(1) if t else ""
        dv = d.group(1) if d else ""
        ok = False
        if text is not None:
            ok = (text in tv) if contains else (tv == text)
        if desc is not None:
            ok = ok or ((desc in dv) if contains else (dv == desc))
        if ok and b:
            c = parse_bounds(b.group(1))
            if c:
                matches.append(c)
    if not matches:
        shown = re.findall(r'text="([^"]+)"', xml)[:30]
        if not required:
            return False
        raise SystemExit(f"no match text={text!r} desc={desc!r} texts={shown}")
    x, y = matches[index]
    adb("shell", "input", "tap", str(x), str(y))
    time.sleep(1.2)
    return True


def shot(name):
    p = OUT / name
    remote = f"/sdcard/mix-shot-{SERIAL or 'default'}.png"
    adb("shell", "screencap", "-p", remote)
    adb("pull", remote, str(p))
    print("wrote", p, p.stat().st_size)


def wait_text(text, seconds=20):
    for _ in range(seconds * 2):
        xml = dump_xml()
        if f'text="{text}"' in xml or f'content-desc="{text}"' in xml:
            return
        time.sleep(0.5)
    raise SystemExit(f"timeout waiting for {text}")


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    adb("shell", "am", "start", "-n", "com.capstone.nik.mixology/.Activities.ActivityMain")
    time.sleep(2)
    wait_text("Home")
    wait_text("Cocktail")
    shot("01-hot.png")

    tap(desc="Image of the cocktail", index=0)
    time.sleep(2.0)
    wait_text("Ingredients")
    tap(desc="Save cocktail", required=False)
    time.sleep(0.8)
    shot("02-details.png")
    tap(desc="Up navigation", required=False)
    time.sleep(0.8)

    tap(desc="Search")
    wait_text("Drink name")
    adb("shell", "input", "text", "gin")
    time.sleep(1.5)
    adb("shell", "input", "keyevent", "66")
    time.sleep(2.0)
    shot("03-search.png")
    tap(desc="Up navigation", required=False)
    time.sleep(0.8)

    tap(text="Mixer")
    time.sleep(2.5)
    shot("04-randomixer.png")

    tap(text="Saved")
    time.sleep(1.5)
    shot("05-saved.png")

    tap(desc="Settings")
    wait_text("Shopping list")
    shot("06-settings.png")
    tap(text="Shopping list")
    time.sleep(1.2)
    shot("07-shopping.png")

    print("done")
    for p in sorted(OUT.glob("*.png")):
        print(p.name, p.stat().st_size)


if __name__ == "__main__":
    main()
