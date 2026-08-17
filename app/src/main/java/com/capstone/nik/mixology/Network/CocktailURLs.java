package com.capstone.nik.mixology.Network;

import com.capstone.nik.mixology.BuildConfig;

public class CocktailURLs {

    public static final String BASE_URL =
            "https://www.thecocktaildb.com/api/json/v1/" + BuildConfig.COCKTAIL_DB_API_KEY + "/";

    public static final String COCKTAIL_INGREDIENTS_URL = "https://www.thecocktaildb.com/images/ingredients/";
    public static final String COCKTAIL_INGREDIENT_PNG_SMALL = "-Small.png";
}
