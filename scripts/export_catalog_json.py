#!/usr/bin/env python3
"""Dump Mixology Postgres drinks + ingredients into the Android catalog asset.

Connection (any one of):
  DATABASE_URL=postgresql://user:pass@localhost:5432/mixology
  or PGHOST / PGPORT / PGUSER / PGPASSWORD / PGDATABASE

Defaults: current OS user, database mixology.

Examples:
  python scripts/export_catalog_json.py
  python scripts/export_catalog_json.py --output app/src/main/assets/catalog/cocktails.json
"""

from __future__ import annotations

import argparse
import json
import os
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DEFAULT_OUTPUT = ROOT / "app/src/main/assets/catalog/cocktails.json"
CATALOG_VERSION = 1

DRINKS_SQL = """
SELECT
    d.drink_id,
    d.name,
    d.thumb,
    d.video,
    d.iba,
    d.instructions,
    c.name AS category,
    a.name AS alcoholic,
    g.name AS glass,
    COALESCE(
        json_agg(
            json_build_object(
                'ingredient', i.name,
                'measure', di.measure
            )
            ORDER BY di.position
        ) FILTER (WHERE i.name IS NOT NULL),
        '[]'::json
    ) AS ingredients
FROM drinks d
LEFT JOIN categories c ON c.id = d.category_id
LEFT JOIN alcoholic_types a ON a.id = d.alcoholic_id
LEFT JOIN glasses g ON g.id = d.glass_id
LEFT JOIN drink_ingredients di ON di.drink_id = d.id
LEFT JOIN ingredients i ON i.id = di.ingredient_id
GROUP BY d.id, c.name, a.name, g.name
ORDER BY d.name
"""

INGREDIENTS_SQL = "SELECT name FROM ingredients ORDER BY name"


def connect():
    sys.path.insert(0, str(Path(__file__).resolve().parent))
    os.environ.setdefault("PGUSER", os.environ.get("USER", "postgres"))
    os.environ.setdefault("PGDATABASE", "mixology")
    from fetch_random_drink import connect as pg_connect

    return pg_connect()


def blank(value: object) -> str | None:
    if value is None:
        return None
    text = str(value).strip()
    return text or None


def drink_payload(row: dict) -> dict:
    ingredients = row["ingredients"]
    if isinstance(ingredients, str):
        ingredients = json.loads(ingredients)
    item = {
        "id": row["drink_id"],
        "name": row["name"],
        "thumb": blank(row["thumb"]) or "",
        "alcoholic": blank(row["alcoholic"]),
        "glass": blank(row["glass"]),
        "category": blank(row["category"]),
        "iba": blank(row["iba"]),
        "instructions": blank(row["instructions"]),
        "video": blank(row["video"]),
        "ingredients": [
            {
                "ingredient": slot["ingredient"],
                "measure": slot.get("measure") or "",
            }
            for slot in ingredients
            if slot.get("ingredient")
        ],
    }
    return {key: value for key, value in item.items() if value not in (None, [])}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Export Mixology Postgres catalog to the Android JSON asset.",
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=DEFAULT_OUTPUT,
        help=f"JSON path (default: {DEFAULT_OUTPUT})",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    conn = connect()
    try:
        with conn.cursor() as cur:
            cur.execute(DRINKS_SQL)
            columns = [col.name for col in cur.description]
            drinks = [drink_payload(dict(zip(columns, row))) for row in cur.fetchall()]
            cur.execute(INGREDIENTS_SQL)
            ingredients = [row[0] for row in cur.fetchall() if row[0]]
    finally:
        conn.close()

    args.output.parent.mkdir(parents=True, exist_ok=True)
    payload = {
        "version": CATALOG_VERSION,
        "drinks": drinks,
        "ingredients": ingredients,
    }
    args.output.write_text(json.dumps(payload, ensure_ascii=False, separators=(",", ":")))
    print(f"wrote {args.output}")
    print(f"{len(drinks)} drinks, {len(ingredients)} ingredients")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
