#!/usr/bin/env python3
"""Loop TheCocktailDB Randomixer endpoint and write unique drinks to a CSV.

Uses the same URL as CocktailService.getRandomixer():
  GET https://www.thecocktaildb.com/api/json/v1/1/random.php

Does not modify app code. Stdlib only.

Examples:
  python scripts/fetch_randomixer_csv.py
  python scripts/fetch_randomixer_csv.py --output scripts/data/randomixer_drinks.csv
  python scripts/fetch_randomixer_csv.py --stop-after-duplicates 50 --max-requests 2000
"""

from __future__ import annotations

import argparse
import csv
import json
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path
from typing import Any

RANDOM_URL = "https://www.thecocktaildb.com/api/json/v1/1/random.php"
DEFAULT_OUTPUT = Path(__file__).resolve().parent / "data" / "randomixer_drinks.csv"
USER_AGENT = "mixology-randomixer-csv/1.0"

CSV_COLUMNS = [
    "idDrink",
    "strDrink",
    "strDrinkAlternate",
    "strTags",
    "strVideo",
    "strCategory",
    "strIBA",
    "strAlcoholic",
    "strGlass",
    "strInstructions",
    "strDrinkThumb",
    "strImageSource",
    "strImageAttribution",
    "strCreativeCommonsConfirmed",
    "dateModified",
    *[f"strIngredient{n}" for n in range(1, 16)],
    *[f"strMeasure{n}" for n in range(1, 16)],
]


def cell(value: Any) -> str:
    if value is None:
        return ""
    text = str(value).strip()
    if not text or text.lower() == "null":
        return ""
    return text


def drink_row(drink: dict[str, Any]) -> dict[str, str]:
    return {column: cell(drink.get(column)) for column in CSV_COLUMNS}


class FetchError(Exception):
    """A single random.php call failed after retries."""


def fetch_random_drink(timeout: float = 20.0, retries: int = 3) -> dict[str, Any]:
    request = urllib.request.Request(
        RANDOM_URL,
        headers={"User-Agent": USER_AGENT},
    )
    last_error: Exception | None = None
    payload: Any = None
    for attempt in range(retries):
        try:
            with urllib.request.urlopen(request, timeout=timeout) as response:
                payload = json.loads(response.read().decode("utf-8"))
            break
        except urllib.error.HTTPError as exc:
            last_error = exc
            if exc.code not in {429, 500, 502, 503, 504} or attempt == retries - 1:
                raise FetchError(f"Failed to call {RANDOM_URL}: {exc}") from exc
            time.sleep(2 ** attempt)
        except urllib.error.URLError as exc:
            last_error = exc
            if attempt == retries - 1:
                raise FetchError(f"Failed to call {RANDOM_URL}: {exc}") from exc
            time.sleep(2 ** attempt)
    else:
        raise FetchError(f"Failed to call {RANDOM_URL}: {last_error}")

    drinks = payload.get("drinks") if isinstance(payload, dict) else None
    if not drinks:
        raise FetchError(f"Unexpected API response: {payload!r}")
    drink = drinks[0]
    if not isinstance(drink, dict):
        raise FetchError(f"Unexpected drink payload: {drink!r}")
    return drink


def load_existing_ids(path: Path) -> set[str]:
    if not path.exists() or path.stat().st_size == 0:
        return set()
    with path.open(newline="", encoding="utf-8") as handle:
        reader = csv.DictReader(handle)
        if not reader.fieldnames or "idDrink" not in reader.fieldnames:
            raise SystemExit(f"{path} is missing an idDrink column")
        return {cell(row.get("idDrink")) for row in reader if cell(row.get("idDrink"))}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Fetch unique Randomixer drinks into a CSV file.",
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=DEFAULT_OUTPUT,
        help=f"CSV path (default: {DEFAULT_OUTPUT})",
    )
    parser.add_argument(
        "--stop-after-duplicates",
        type=int,
        default=50,
        help="Stop after this many consecutive already-seen IDs (default: 50).",
    )
    parser.add_argument(
        "--max-requests",
        type=int,
        default=2000,
        help="Safety cap on random.php calls (default: 2000).",
    )
    parser.add_argument(
        "--delay",
        type=float,
        default=0.5,
        help="Seconds to sleep between requests (default: 0.5).",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    if args.stop_after_duplicates < 1:
        print("--stop-after-duplicates must be at least 1", file=sys.stderr)
        return 2
    if args.max_requests < 1:
        print("--max-requests must be at least 1", file=sys.stderr)
        return 2
    if args.delay < 0:
        print("--delay must be >= 0", file=sys.stderr)
        return 2

    output: Path = args.output
    output.parent.mkdir(parents=True, exist_ok=True)

    seen = load_existing_ids(output)
    resumed = len(seen)
    write_header = not output.exists() or output.stat().st_size == 0
    duplicate_streak = 0
    added = 0

    with output.open("a", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=CSV_COLUMNS, extrasaction="ignore")
        if write_header:
            writer.writeheader()

        for request_number in range(1, args.max_requests + 1):
            try:
                drink = fetch_random_drink()
            except FetchError as exc:
                print(f"request {request_number}: {exc}", file=sys.stderr)
                if request_number < args.max_requests:
                    time.sleep(args.delay)
                continue

            drink_id = cell(drink.get("idDrink"))
            name = cell(drink.get("strDrink"))
            if not drink_id or not name:
                print(f"request {request_number}: skipped drink missing idDrink/strDrink")
                duplicate_streak += 1
            elif drink_id in seen:
                duplicate_streak += 1
                print(
                    f"request {request_number}: duplicate {name} ({drink_id}) "
                    f"streak={duplicate_streak} unique={len(seen)}"
                )
            else:
                seen.add(drink_id)
                writer.writerow(drink_row(drink))
                handle.flush()
                added += 1
                duplicate_streak = 0
                print(
                    f"request {request_number}: stored {name} ({drink_id}) "
                    f"unique={len(seen)}"
                )

            if duplicate_streak >= args.stop_after_duplicates:
                print(
                    f"stopping: {duplicate_streak} consecutive duplicates "
                    f"(limit {args.stop_after_duplicates})"
                )
                break
            if request_number < args.max_requests:
                time.sleep(args.delay)
        else:
            print(f"stopping: reached --max-requests {args.max_requests}")

    print(
        f"done: {added} new drinks written to {output} "
        f"(unique total {len(seen)}, resumed {resumed})"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
