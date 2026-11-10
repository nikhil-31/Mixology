package com.capstone.nik.mixology.Network;

import com.capstone.nik.mixology.BuildConfig;

public class CocktailURLs {

    /**
     * Read at run time, not inlined: a compile-time constant gets copied into every class that
     * uses it, and incremental builds don't always recompile them when the address changes in
     * local.properties, leaving some calls (Ada's chat) on an old server address.
     */
    public static final String BASE_URL = String.valueOf(BuildConfig.MIXOLOGY_API_BASE_URL);

    public static final String COCKTAIL_INGREDIENTS_URL = "https://www.thecocktaildb.com/images/ingredients/";
    public static final String COCKTAIL_INGREDIENT_PNG_SMALL = "-Small.png";
}
