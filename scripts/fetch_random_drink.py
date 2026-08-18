#!/usr/bin/env python3
"""Fetch a random cocktail from TheCocktailDB (Randomixer endpoint) and store it in Postgres.

Uses the same URL as CocktailService.getRandomixer():
  GET https://www.thecocktaildb.com/api/json/v1/1/random.php

Schema matches how Randomixer uses the payload:
  drinks              — recipe metadata
  ingredients         — unique ingredient names (strIngredientN)
  drink_ingredients   — drink + slot + ingredient_id + measure

Connection (any one of):
  DATABASE_URL=postgresql://user:pass@localhost:5432/mixology
  or PGHOST / PGPORT / PGUSER / PGPASSWORD / PGDATABASE

Examples:
  python fetch_random_drink.py
  python fetch_random_drink.py --count 10
  python fetch_random_drink.py --dry-run
"""

from __future__ import annotations

import argparse
import json
import os
import random
import sys
import time
import urllib.error
import urllib.request
from datetime import datetime
from pathlib import Path
from typing import Any, TYPE_CHECKING

if TYPE_CHECKING:
    import psycopg

RANDOM_URL = "https://www.thecocktaildb.com/api/json/v1/1/random.php"
INGREDIENT_IMAGE_BASE = "https://www.thecocktaildb.com/images/ingredients/"
INGREDIENT_IMAGE_SUFFIX = "-Small.png"
INGREDIENT_SLOTS = range(1, 16)
SCHEMA_SQL = (Path(__file__).resolve().parent / "schema.sql").read_text()

UPSERT_LOOKUP_SQL = """
INSERT INTO {table} (name) VALUES (%s)
ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
RETURNING id
"""

UPSERT_INGREDIENT_SQL = """
INSERT INTO ingredients (name, image_url) VALUES (%s, %s)
ON CONFLICT (name) DO UPDATE SET
    image_url = COALESCE(ingredients.image_url, EXCLUDED.image_url)
RETURNING id
"""

INSERT_DRINK_SQL = """
INSERT INTO drinks (
    drink_id, name, alternate_name, tags, video, category_id, iba, alcoholic_id,
    glass_id, instructions, instructions_es, instructions_de, instructions_fr,
    instructions_it, instructions_zh_hans, instructions_zh_hant, thumb,
    image_source, image_attribution, creative_commons_confirmed,
    date_modified, fetched_at, raw
) VALUES (
    %(drink_id)s, %(name)s, %(alternate_name)s, %(tags)s, %(video)s,
    %(category_id)s, %(iba)s, %(alcoholic_id)s, %(glass_id)s, %(instructions)s,
    %(instructions_es)s, %(instructions_de)s, %(instructions_fr)s,
    %(instructions_it)s, %(instructions_zh_hans)s, %(instructions_zh_hant)s,
    %(thumb)s, %(image_source)s, %(image_attribution)s,
    %(creative_commons_confirmed)s, %(date_modified)s, NOW(), %(raw)s
)
ON CONFLICT (drink_id) DO NOTHING
RETURNING id
"""

INSERT_INGREDIENT_SQL = """
INSERT INTO drink_ingredients (drink_id, position, ingredient_id, measure)
VALUES (%s, %s, %s, %s)
"""


def blank_to_none(value: Any) -> str | None:
    if value is None:
        return None
    text = str(value).strip()
    return text or None


def parse_date_modified(value: Any) -> datetime | None:
    text = blank_to_none(value)
    if text is None:
        return None
    for fmt in ("%Y-%m-%d %H:%M:%S", "%Y-%m-%d"):
        try:
            return datetime.strptime(text, fmt)
        except ValueError:
            continue
    return None


def ingredient_rows(drink: dict[str, Any]) -> list[tuple[str, int, str, str]]:
    """Keep occupied slots only, same rule as Drink.ingredientMeasures()."""
    drink_id = drink["idDrink"]
    rows: list[tuple[str, int, str, str]] = []
    for position in INGREDIENT_SLOTS:
        name = blank_to_none(drink.get(f"strIngredient{position}"))
        if name is None:
            continue
        measure = drink.get(f"strMeasure{position}")
        measure_text = measure.strip() if isinstance(measure, str) else ""
        rows.append((drink_id, position, name, measure_text))
    return rows


def drink_row(drink: dict[str, Any]) -> dict[str, Any]:
    drink_id = blank_to_none(drink.get("idDrink"))
    name = blank_to_none(drink.get("strDrink"))
    if drink_id is None or name is None:
        raise ValueError(f"Drink is missing idDrink/strDrink: {drink!r}")
    return {
        "drink_id": drink_id,
        "name": name,
        "alternate_name": blank_to_none(drink.get("strDrinkAlternate")),
        "tags": blank_to_none(drink.get("strTags")),
        "video": blank_to_none(drink.get("strVideo")),
        "iba": blank_to_none(drink.get("strIBA")),
        "instructions": blank_to_none(drink.get("strInstructions")),
        "instructions_es": blank_to_none(drink.get("strInstructionsES")),
        "instructions_de": blank_to_none(drink.get("strInstructionsDE")),
        "instructions_fr": blank_to_none(drink.get("strInstructionsFR")),
        "instructions_it": blank_to_none(drink.get("strInstructionsIT")),
        "instructions_zh_hans": blank_to_none(drink.get("strInstructionsZH-HANS")),
        "instructions_zh_hant": blank_to_none(drink.get("strInstructionsZH-HANT")),
        "thumb": blank_to_none(drink.get("strDrinkThumb")),
        "image_source": blank_to_none(drink.get("strImageSource")),
        "image_attribution": blank_to_none(drink.get("strImageAttribution")),
        "creative_commons_confirmed": blank_to_none(
            drink.get("strCreativeCommonsConfirmed")
        ),
        "date_modified": parse_date_modified(drink.get("dateModified")),
        "raw": drink,
        "category_name": blank_to_none(drink.get("strCategory")),
        "alcoholic_name": blank_to_none(drink.get("strAlcoholic")),
        "glass_name": blank_to_none(drink.get("strGlass")),
    }


LOOKUP_TABLES = frozenset(
    {"categories", "alcoholic_types", "glasses", "ingredients"}
)


def ingredient_image_url(name: str) -> str:
    return INGREDIENT_IMAGE_BASE + name.replace(" ", "%20") + INGREDIENT_IMAGE_SUFFIX


def upsert_lookup(
    cur: Any,
    table: str,
    name: str | None,
) -> int | None:
    if name is None:
        return None
    if table not in LOOKUP_TABLES:
        raise ValueError(f"Unknown lookup table: {table}")
    if table == "ingredients":
        cur.execute(UPSERT_INGREDIENT_SQL, (name, ingredient_image_url(name)))
    else:
        cur.execute(UPSERT_LOOKUP_SQL.format(table=table), (name,))
    row = cur.fetchone()
    return None if row is None else int(row[0])


def fetch_random_drink(timeout: float = 20.0) -> dict[str, Any]:
    request = urllib.request.Request(
        RANDOM_URL,
        headers={"User-Agent": "mixology-randomixer-script/1.0"},
    )
    try:
        with urllib.request.urlopen(request, timeout=timeout) as response:
            payload = json.loads(response.read().decode("utf-8"))
    except urllib.error.URLError as exc:
        raise SystemExit(f"Failed to call {RANDOM_URL}: {exc}") from exc

    drinks = payload.get("drinks") if isinstance(payload, dict) else None
    if not drinks:
        raise SystemExit(f"Unexpected API response: {payload!r}")
    return drinks[0]


def connect() -> "psycopg.Connection":
    try:
        import psycopg
    except ImportError as exc:
        raise SystemExit(
            "psycopg is required. Install with: pip install -r scripts/requirements.txt"
        ) from exc

    database_url = os.environ.get("DATABASE_URL")
    if database_url:
        return psycopg.connect(database_url)
    return psycopg.connect(
        host=os.environ.get("PGHOST", "localhost"),
        port=os.environ.get("PGPORT", "5432"),
        user=os.environ.get("PGUSER", "postgres"),
        password=os.environ.get("PGPASSWORD", ""),
        dbname=os.environ.get("PGDATABASE", "mixology"),
    )


def ensure_schema(conn: "psycopg.Connection") -> None:
    with conn.cursor() as cur:
        cur.execute(SCHEMA_SQL)
    conn.commit()


def insert_drink(conn: "psycopg.Connection", drink: dict[str, Any]) -> bool:
    from psycopg.types.json import Jsonb

    row = drink_row(drink)
    ingredients = ingredient_rows(drink)
    with conn.cursor() as cur:
        cur.execute("SELECT 1 FROM drinks WHERE drink_id = %s", (row["drink_id"],))
        if cur.fetchone() is not None:
            return False
        row["category_id"] = upsert_lookup(cur, "categories", row.pop("category_name"))
        row["alcoholic_id"] = upsert_lookup(
            cur, "alcoholic_types", row.pop("alcoholic_name")
        )
        row["glass_id"] = upsert_lookup(cur, "glasses", row.pop("glass_name"))
        row["raw"] = Jsonb(row["raw"])
        cur.execute(INSERT_DRINK_SQL, row)
        pk_row = cur.fetchone()
        if pk_row is None:
            return False
        drink_pk = int(pk_row[0])
        ingredient_inserts: list[tuple[int, int, int, str]] = []
        for _api_id, position, name, measure in ingredients:
            ingredient_id = upsert_lookup(cur, "ingredients", name)
            if ingredient_id is None:
                continue
            ingredient_inserts.append((drink_pk, position, ingredient_id, measure))
        cur.executemany(INSERT_INGREDIENT_SQL, ingredient_inserts)
    conn.commit()
    return True


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Fetch TheCocktailDB random.php and store drinks in Postgres.",
    )
    parser.add_argument(
        "--count",
        type=int,
        default=1,
        help="Number of random drinks to fetch (default: 1).",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Print the drink without writing to Postgres.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    if args.count < 1:
        print("--count must be at least 1", file=sys.stderr)
        return 2

    conn = None
    if not args.dry_run:
        conn = connect()
        ensure_schema(conn)

    stored = 0
    skipped = 0
    try:
        for i in range(args.count):
            drink = fetch_random_drink()
            name = drink.get("strDrink") or "unknown"
            if conn is None:
                print(f"{name} dry-run")
            elif insert_drink(conn, drink):
                stored += 1
                print(f"{name} stored")
            else:
                skipped += 1
                print(f"{name} skipped")
            if i < args.count - 1:
                time.sleep(random.uniform(0, 0.3))

        print()
        print(
            f"done: {args.count} fetches, {stored} stored, {skipped} skipped"
        )
        if conn is not None:
            with conn.cursor() as cur:
                cur.execute("SELECT COUNT(*) FROM drinks")
                drink_total = cur.fetchone()[0]
                cur.execute("SELECT COUNT(*) FROM ingredients")
                ingredient_total = cur.fetchone()[0]
            print(f"database: {drink_total} drinks, {ingredient_total} ingredients")
    finally:
        if conn is not None:
            conn.close()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
