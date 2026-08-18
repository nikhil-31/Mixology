CREATE TABLE IF NOT EXISTS categories (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS alcoholic_types (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS glasses (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS ingredients (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    image_url TEXT
);

ALTER TABLE ingredients ADD COLUMN IF NOT EXISTS image_url TEXT;

UPDATE ingredients
SET image_url = 'https://www.thecocktaildb.com/images/ingredients/'
    || replace(name, ' ', '%20')
    || '-Small.png'
WHERE image_url IS NULL;

CREATE TABLE IF NOT EXISTS drinks (
    id SERIAL PRIMARY KEY,
    drink_id TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    alternate_name TEXT,
    tags TEXT,
    video TEXT,
    category_id INT REFERENCES categories(id),
    iba TEXT,
    alcoholic_id INT REFERENCES alcoholic_types(id),
    glass_id INT REFERENCES glasses(id),
    instructions TEXT,
    instructions_es TEXT,
    instructions_de TEXT,
    instructions_fr TEXT,
    instructions_it TEXT,
    instructions_zh_hans TEXT,
    instructions_zh_hant TEXT,
    thumb TEXT,
    image_source TEXT,
    image_attribution TEXT,
    creative_commons_confirmed TEXT,
    date_modified TIMESTAMP,
    fetched_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    raw JSONB NOT NULL
);

CREATE TABLE IF NOT EXISTS drink_ingredients (
    drink_id INT NOT NULL REFERENCES drinks(id) ON DELETE CASCADE,
    position SMALLINT NOT NULL CHECK (position BETWEEN 1 AND 15),
    ingredient_id INT NOT NULL REFERENCES ingredients(id),
    measure TEXT NOT NULL DEFAULT '',
    PRIMARY KEY (drink_id, position)
);

CREATE INDEX IF NOT EXISTS drinks_category_id_idx ON drinks (category_id);
CREATE INDEX IF NOT EXISTS drinks_alcoholic_id_idx ON drinks (alcoholic_id);
CREATE INDEX IF NOT EXISTS drinks_glass_id_idx ON drinks (glass_id);
CREATE INDEX IF NOT EXISTS drink_ingredients_ingredient_id_idx
    ON drink_ingredients (ingredient_id);
