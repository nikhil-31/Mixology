"""Records a captioned demo video of the app (under a minute) on an Android emulator.

    MIXOLOGY_DEMO_EMAIL=... MIXOLOGY_DEMO_PASSWORD=... ANDROID_SERIAL=emulator-5556 \
        python3 scripts/record_demo_video.py [--only scene,...] [--edit-only]

It needs the local backend on :8000, a debug build pointed at http://10.0.2.2:8000/ installed and
signed in as the demo account, and ffmpeg plus Pillow on the Mac. Steps:
  1. resets the demo account's data through the API so every run looks the same,
  2. records each scene as its own clip with the emulator's screen recorder,
  3. lays each clip in a phone frame under its caption and joins them with crossfades.
Output: build/demo/mixology-demo.mp4 (1080x1920) and mixology-demo-720p.mp4.
"""
import argparse
import json
import os
import subprocess
import time
import urllib.request
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

import demo_adb as d

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "build" / "demo"
CLIPS = OUT / "clips"
PARTS = OUT / "parts"
API = "http://localhost:8000"
# The demo account's login comes from the environment so it stays out of the repo.
EMAIL = os.environ.get("MIXOLOGY_DEMO_EMAIL", "")
PASSWORD = os.environ.get("MIXOLOGY_DEMO_PASSWORD", "")

W, H = 1080, 1920
BG = (20, 16, 18)
ACCENT = (214, 40, 46)
PHONE_H = 1560
CAPTION_TOP = 120
FADE = 0.35
FPS = 30

# --- Demo data -------------------------------------------------------------------------------

BAR = ["Gin", "Campari", "Sweet Vermouth", "Light rum", "Tequila", "Lime juice", "Sugar syrup",
       "Bourbon", "Angostura bitters", "Orange", "Soda water", "Mint"]
SAVED = ["11003", "11007", "11006", "178325", "11001", "17197"]
NOT_SAVED = ["11000"]  # The Mojito, saved on camera.
SHOPPING_DRINKS = ["17197", "178325"]  # French 75, Aperol Spritz.


def api(method, path, body=None, token=None):
    req = urllib.request.Request(API + path, method=method, data=json.dumps(body).encode() if body is not None else None)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", f"Bearer {token}")
    with urllib.request.urlopen(req, timeout=30) as res:
        raw = res.read()
        return json.loads(raw) if raw else None


def ingredients(drink_id):
    drink = api("GET", f"/lookup?i={drink_id}")["drinks"][0]
    return [drink[f"strIngredient{i}"] for i in range(1, 16) if drink.get(f"strIngredient{i}")]


def seed():
    token = api("POST", "/auth/login", {"email": EMAIL, "password": PASSWORD})["id_token"]
    api("PUT", "/bar", {"ingredients": BAR}, token)
    for drink_id in SAVED:
        api("PUT", f"/me/saved/{drink_id}", None, token)
    for drink_id in NOT_SAVED:
        api("DELETE", f"/me/saved/{drink_id}", None, token)
    api("PUT", "/me/shopping", [], token)
    for drink_id in SHOPPING_DRINKS:
        api("POST", "/me/shopping", {"names": ingredients(drink_id), "drink_id": drink_id}, token)
    api("DELETE", "/chat", None, token)
    print("seeded the demo account")


def sign_in():
    d.tap("Email", pause=0.8)
    d.type_text(EMAIL, chunk=40)
    d.tap("Password", pause=0.8)
    d.type_text(PASSWORD, chunk=40)
    close_keyboard()
    d.tap("Sign in", pause=4)


def fresh_app():
    """Restarts the app so it pulls the seeded data, then visits each tab once to load images."""
    d.shell("am", "force-stop", d.PACKAGE)
    time.sleep(1.5)
    d.shell("am", "start", "-W", "-n", f"{d.PACKAGE}/.Activities.ActivityMain")
    deadline = time.time() + 120
    while not d.find("My Bar"):
        if d.find("Welcome back"):
            sign_in()
        if time.time() > deadline:
            raise SystemExit("the app didn't reach its tabs")
        time.sleep(1)
    time.sleep(3)
    for tab in ["My Bar", "Shopping", "Ada", "Home"]:
        d.tap(tab, pause=2.5)
        if tab == "Ada":
            close_keyboard()
    d.tap("Shopping", pause=1)
    d.swipe(640, 700, 640, 1900, 500, pause=4)  # Pull to refresh, for the seeded list.
    d.tap("Home", pause=2)


# --- Scenes ----------------------------------------------------------------------------------
# Each scene: caption, target seconds in the final cut, and the actions recorded for it.


def home():
    time.sleep(1.2)
    d.swipe(1150, 900, 150, 900, 350, pause=1.3)
    d.swipe(1150, 900, 150, 900, 350, pause=1.3)
    d.swipe(640, 2300, 640, 1100, 700, pause=1.6)


def scroll_top():
    d.swipe(640, 900, 640, 2400, 300, pause=0.5)
    d.swipe(640, 900, 640, 2400, 300, pause=1.0)


def close_keyboard():
    """Escape is Back when no keyboard is up, so only send it while one is showing."""
    if "mInputShown=true" in d.shell("dumpsys", "input_method"):
        d.hide_keyboard()


def search():
    d.tap(desc="Search", pause=1.0)
    d.type_text("mojito", chunk=20)
    d.key(66, pause=2.6)


def details():
    d.tap("Mojito", pause=2.2)
    d.tap(desc="Save cocktail", pause=1.4)
    d.swipe(640, 2300, 640, 1200, 800, pause=1.8)


def leave_details():
    d.tap(desc="Up navigation", pause=1.2)
    d.tap(desc="Up navigation", pause=1.2)


def my_bar():
    d.tap("My Bar", pause=2.2)
    d.swipe(640, 2300, 640, 1300, 900, pause=1.4)
    d.swipe(640, 2300, 640, 1300, 900, pause=1.8)


def shopping_aisles():
    d.tap("Shopping", pause=2.6)
    d.swipe(640, 2200, 640, 1600, 900, pause=1.6)
    d.swipe(640, 1600, 640, 2200, 700, pause=1.0)


def shopping_actions():
    d.tap("Add an item, like ice or limes", pause=0.8)
    d.type_text("Ice", chunk=20)
    d.key(66, pause=0.8)
    close_keyboard()
    d.tap("Remove", pause=1.6)
    d.tap("Aperol", pause=0.7)
    d.tap("Champagne", pause=0.7)
    d.tap("Prosecco", pause=1.0)
    d.tap("Done shopping", pause=2.0)
    d.tap("Add 3 to My Bar", pause=2.2)


def ada():
    d.tap("Ada", pause=1.5)
    d.tap("Something with gin", pause=0.6)
    d.tap(desc="Send", pause=1.5)
    wait_for_reply()
    time.sleep(2.5)


def wait_for_reply(seconds=150):
    """Until the reply has drink cards and its text has stopped streaming."""
    sent = time.time()
    deadline = sent + seconds
    last, steady, baseline = None, 0, None
    while time.time() < deadline:
        found = d.nodes()
        texts = tuple(n[0] for n in found if n[0] and "…" not in n[0] and "..." not in n[0])
        if baseline is None:
            baseline = texts
        elif texts != baseline and sent:
            # Ada was thinking until now: cut that wait from the video.
            d.LOOKUPS.append((sent + 0.8, time.time() - 1.2))
            sent = None
        has_cards = any(n[1] == "Image of the cocktail" for n in found)
        if has_cards and texts == last:
            steady += 1
            if steady >= 3:
                return
        else:
            steady = 0
        last = texts
        time.sleep(1.0)


def import_recipe():
    d.tap("Import from a link", pause=1.4)
    d.tap("Link or recipe text", pause=0.8)
    d.type_text("diffordsguide.com/negroni", chunk=40)
    close_keyboard()
    time.sleep(1.4)


def saved():
    d.tap(desc="Up navigation", pause=1.2)
    d.tap("See all", pause=2.4)
    d.swipe(640, 2300, 640, 1500, 900, pause=1.4)


SCENES = [
    # name, caption, final seconds, action, tidy-up after recording (not filmed)
    ("home", "Cocktails picked for you", 5.0, home, scroll_top),
    ("search", "Search 10,000+ cocktails", 5.0, search, None),
    ("details", "Every recipe, step by step", 5.0, details, leave_details),
    ("bar", "Tell it what's in your bar,\nsee what you can make", 6.0, my_bar, None),
    ("aisles", "A shopping list\nsorted by aisle", 4.5, shopping_aisles, None),
    ("shop", "Add anything, check it off,\nrestock your bar", 8.0, shopping_actions, None),
    ("ada", "Ask Ada, your AI bartender", 9.5, ada, lambda: (close_keyboard(), d.tap("Home", pause=1.5), scroll_top())),
    ("import", "Import recipes from any link", 4.5, import_recipe, None),
    ("saved", "Save the ones you love", 4.0, saved, None),
]


def record(name, action, after):
    CLIPS.mkdir(parents=True, exist_ok=True)
    clip = CLIPS / f"{name}.webm"
    clip.unlink(missing_ok=True)
    d.adb("emu", "screenrecord", "start", "--time-limit", "170", str(clip))
    started = time.time()
    time.sleep(1.0)
    d.LOOKUPS.clear()
    action()
    # Lookups and waits to cut, as clip times, trimmed a little so motion either side stays.
    cuts = [(a - started + 0.15, b - started - 0.05) for a, b in d.LOOKUPS if b - a > 0.4]
    # The recorder runs on a while the stop goes through; nothing after the last action is wanted.
    cuts.append((time.time() - started + 0.4, 9999.0))
    (CLIPS / f"{name}.cuts.json").write_text(json.dumps(cuts))
    time.sleep(0.5)
    d.adb("emu", "screenrecord", "stop")
    for _ in range(40):  # The file is finished a moment after stop.
        if clip.exists() and probe(clip) > 0:
            break
        time.sleep(0.5)
    if probe(clip) <= 0:
        raise SystemExit(f"the recorder wrote nothing for {name}; run again with --only {name}")
    print(f"recorded {name}: {probe(clip):.1f}s")
    if after:
        after()


def probe(path):
    out = subprocess.run(["ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "csv=p=0", str(path)],
                         capture_output=True, text=True).stdout.strip()
    try:
        return float(out)
    except ValueError:
        return 0.0


# --- Editing ---------------------------------------------------------------------------------


def font(size, weight="Bold"):
    f = ImageFont.truetype("/System/Library/Fonts/SFNS.ttf", size)
    try:
        f.set_variation_by_name(weight)
    except Exception:
        pass
    return f


def phone_box():
    width = round(PHONE_H * 1280 / 2856 / 2) * 2
    x = (W - width) // 2
    y = H - PHONE_H - 70
    return x, y, width, PHONE_H


def backdrop(caption, path):
    """The caption over the dark background, with a soft glow where the phone sits."""
    img = Image.new("RGB", (W, H), BG)
    draw = ImageDraw.Draw(img)
    x, y, w, h = phone_box()
    draw.rounded_rectangle((x - 10, y - 10, x + w + 10, y + h + 10), radius=62, fill=(44, 38, 41))
    f = font(58)
    lines = caption.split("\n")
    top = CAPTION_TOP + (0 if len(lines) == 2 else 36)
    for i, line in enumerate(lines):
        tw = draw.textlength(line, font=f)
        draw.text(((W - tw) / 2, top + i * 76), line, font=f, fill=(255, 255, 255))
    draw.rounded_rectangle((W / 2 - 36, top - 40, W / 2 + 36, top - 32), radius=4, fill=ACCENT)
    img.save(path)


def corners(path, radius=52):
    """Rounds the screen's corners: transparent except the corners, which take the frame colour."""
    x, y, w, h = phone_box()
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    mask = Image.new("L", (w, h), 255)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, w - 1, h - 1), radius=radius, fill=0)
    img.paste(Image.new("RGBA", (w, h), (44, 38, 41, 255)), (x, y), mask)
    img.save(path)


def card(title, subtitle, path):
    img = Image.new("RGB", (W, H), BG)
    draw = ImageDraw.Draw(img)
    icon = ROOT / "app/src/main/res/mipmap-xxxhdpi/ic_launcher.png"
    top = 640
    if icon.exists():
        logo = Image.open(icon).convert("RGBA").resize((240, 240))
        img.paste(logo, ((W - 240) // 2, top), logo)
    tf, sf = font(112), font(52, "Regular")
    tw = draw.textlength(title, font=tf)
    draw.text(((W - tw) / 2, top + 300), title, font=tf, fill=(255, 255, 255))
    sw = draw.textlength(subtitle, font=sf)
    draw.text(((W - sw) / 2, top + 450), subtitle, font=sf, fill=(232, 150, 150))
    img.save(path)


def ffmpeg(*args):
    subprocess.run(["ffmpeg", "-v", "error", "-y", *args], check=True)


def render_scene(name, caption, seconds):
    clip = CLIPS / f"{name}.webm"
    cuts_file = CLIPS / f"{name}.cuts.json"
    cuts = [(0.0, 0.5)] + (json.loads(cuts_file.read_text()) if cuts_file.exists() else [])
    cuts = [(a, b) for a, b in cuts if b > a]
    raw = probe(clip) - sum(b - a for a, b in cuts)
    speed = max(1.0, raw / seconds)
    drop = "+".join(f"between(t,{a:.3f},{b:.3f})" for a, b in cuts)
    bg, mask = PARTS / f"{name}-bg.png", PARTS / "corners.png"
    backdrop(caption, bg)
    x, y, w, h = phone_box()
    out = PARTS / f"{name}.mp4"
    ffmpeg(
        "-loop", "1", "-i", str(bg), "-i", str(clip), "-loop", "1", "-i", str(mask),
        "-filter_complex",
        f"[1:v]fps={FPS},select='not({drop})',setpts=N/{FPS}/TB,setpts=PTS/{speed:.3f},fps={FPS},scale={w}:{h},tpad=stop_mode=clone:stop_duration={seconds}[v];"
        f"[0:v][v]overlay={x}:{y}:shortest=0[a];[a][2:v]overlay=0:0,format=yuv420p,trim=duration={seconds}[out]",
        "-map", "[out]", "-r", str(FPS), "-c:v", "libx264", "-preset", "medium", "-crf", "18", "-t", str(seconds), str(out),
    )
    print(f"rendered {name}: {raw:.1f}s raw at {speed:.2f}x -> {seconds}s")
    return out, seconds


def render_card(name, title, subtitle, seconds):
    png, out = PARTS / f"{name}.png", PARTS / f"{name}.mp4"
    card(title, subtitle, png)
    ffmpeg("-loop", "1", "-i", str(png), "-vf", f"fps={FPS},format=yuv420p", "-c:v", "libx264", "-crf", "18",
           "-t", str(seconds), str(out))
    return out, seconds


def edit():
    PARTS.mkdir(parents=True, exist_ok=True)
    corners(PARTS / "corners.png")
    parts = [render_card("title", "Mixology", "Your pocket bartender", 2.6)]
    parts += [render_scene(name, caption, seconds) for name, caption, seconds, _, _ in SCENES]
    parts.append(render_card("end", "Mixology", "Make something good tonight", 3.0))

    inputs, chain, offset = [], "", 0.0
    for path, _ in parts:
        inputs += ["-i", str(path)]
    last = "[0:v]"
    for i, (_, seconds) in enumerate(parts[1:], start=1):
        offset += parts[i - 1][1] - FADE
        label = f"[x{i}]"
        chain += f"{last}[{i}:v]xfade=transition=fade:duration={FADE}:offset={offset:.3f}{label};"
        last = label
    final = OUT / "mixology-demo.mp4"
    ffmpeg(*inputs, "-filter_complex", chain.rstrip(";"), "-map", last, "-c:v", "libx264", "-preset", "slow",
           "-crf", "20", "-pix_fmt", "yuv420p", "-movflags", "+faststart", str(final))
    ffmpeg("-i", str(final), "-vf", "scale=720:1280", "-c:v", "libx264", "-crf", "23", "-movflags", "+faststart",
           str(OUT / "mixology-demo-720p.mp4"))
    print(f"wrote {final} ({probe(final):.1f}s)")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--only", help="comma-separated scenes to re-record")
    parser.add_argument("--edit-only", action="store_true")
    args = parser.parse_args()
    if not args.edit_only:
        if not EMAIL or not PASSWORD:
            parser.error("set MIXOLOGY_DEMO_EMAIL and MIXOLOGY_DEMO_PASSWORD to the demo account's login")
        only =set(args.only.split(",")) if args.only else None
        scenes = [s for s in SCENES if only is None or s[0] in only]
        seed()
        fresh_app()
        for name, _, _, action, after in scenes:
            record(name, action, after)
    edit()


if __name__ == "__main__":
    main()
