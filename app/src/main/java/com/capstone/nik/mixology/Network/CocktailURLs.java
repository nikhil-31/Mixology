package com.capstone.nik.mixology.Network;

/**
 * Created by nik on 12/9/2016.
 */

public class CocktailURLs {

  // API Key
  private static String API_KEY = "2912";

  // Base URL
  public static String BASE_URL = "https://www.thecocktaildb.com/api/json/v1/" + API_KEY + "/";

  // Search using name
  public static String COCKTAIL_SEARCH_URL_BY_NAME = "https://www.thecocktaildb.com/api/json/v1/" + API_KEY + "/search.php?s=";

  // Search using id
  public static String COCKTAIL_SEARCH_URL_BY_ID = "https://www.thecocktaildb.com/api/json/v1/" + API_KEY + "/lookup.php?i=";

  // Randomixer URL
  public static String COCKTAIL_URL_RANDOM = "https://www.thecocktaildb.com/api/json/v1/" + API_KEY + "/random.php";

  // Nav Drawer Filter URL
  public static String COCKTAIL_SEARCH_URL_INGREDIENT_GIN = "https://www.thecocktaildb.com/api/json/v1/" + API_KEY + "/filter.php?i=Gin";
  public static String COCKTAIL_SEARCH_URL_INGREDIENT_VODKA = "https://www.thecocktaildb.com/api/json/v1/" + API_KEY + "/filter.php?i=Vodka";
  public static String COCKTAIL_SEARCH_URL_ALCOHOLIC = "https://www.thecocktaildb.com/api/json/v1/" + API_KEY + "/filter.php?a=Alcoholic";
  public static String COCKTAIL_SEARCH_URL_NON_ALCOHOLIC = "https://www.thecocktaildb.com/api/json/v1/" + API_KEY + "/filter.php?a=Non_Alcoholic";
  public static String COCKTAIL_SEARCH_URL_COCKTAIL_GLASS = "https://www.thecocktaildb.com/api/json/v1/" + API_KEY + "/filter.php?g=Cocktail_glass";
  public static String COCKTAIL_SEARCH_URL_COCKTAIL = "https://www.thecocktaildb.com/api/json/v1/" + API_KEY + "/filter.php?c=Cocktail";
  public static String COCKTAIL_SEARCH_URL_ORDINARY = "https://www.thecocktaildb.com/api/json/v1/" + API_KEY + "/filter.php?c=Ordinary_Drink";
  public static String COCKTAIL_SEARCH_URL_HIGHBALL_GLASS = "https://www.thecocktaildb.com/api/json/v1/" + API_KEY + "/filter.php?g=Highball%20glass";

  // Ingredients URL
  public static String COCKTAIL_INGREDIENTS_URL = "https://www.thecocktaildb.com/images/ingredients/";
  public static String COCKTAIL_INGREDIENT_PNG_SMALL = "-Small.png";


}
