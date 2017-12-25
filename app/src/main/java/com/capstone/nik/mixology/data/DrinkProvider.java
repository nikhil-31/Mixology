package com.capstone.nik.mixology.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

import static com.capstone.nik.mixology.data.AlcoholicColumn._ID;

/**
 * Created by nik on 12/17/2016.
 */

@ContentProvider(authority = DrinkProvider.AUTHORITY, database = DrinkDatabase.class)
public final class DrinkProvider {

  static final String AUTHORITY = "com.example.nik.mixology.data.DrinkProvider";
  private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

  interface Path {
    String ALCOHOLIC = "Alcoholic";
    String NON_ALCOHOLIC = "Non_Alcoholic";
    String COCKTAIL = "Cocktail";
    String ORDINARY_DRINK = "Ordinary_Drink";
    String GIN = "Gin";
    String VODKA = "Vodka";
    String COCKTAIL_GLASS = "Cocktail_Glass";
    String HIGHBALL_GLASS = "highball_glass";
    String SAVED = "Drink_Saved";
  }

  private static Uri buildUri(String... paths) {
    Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
    for (String path : paths) {
      builder.appendPath(path);
    }
    return builder.build();
  }

  @TableEndpoint(table = DrinkDatabase.ALCOHOLIC)
  public static class Alcoholic {
    @ContentUri(
        path = Path.ALCOHOLIC,
        type = "vnd.android.cursor.dir/drink")
    public static final Uri CONTENT_URI_ALCOHOLIC = buildUri(Path.ALCOHOLIC);

  }

  @TableEndpoint(table = DrinkDatabase.NON_ALCOHOLIC)
  public static class NonAlcoholic {
    @ContentUri(
        path = Path.NON_ALCOHOLIC,
        type = "vnd.android.cursor.dir/drink")
    public static final Uri CONTENT_URI_NON_ALCOHOLIC = buildUri(Path.NON_ALCOHOLIC);

  }

  @TableEndpoint(table = DrinkDatabase.COCKTAIL)
  public static class Cocktail {
    @ContentUri(
        path = Path.COCKTAIL,
        type = "vnd.android.cursor.dir/drink")
    public static final Uri CONTENT_URI_COCKTAIL = buildUri(Path.COCKTAIL);

  }

  @TableEndpoint(table = DrinkDatabase.ORDINARY_DRINK)
  public static class OrdinaryDrink {
    @ContentUri(
        path = Path.ORDINARY_DRINK,
        type = "vnd.android.cursor.dir/drink")
    public static final Uri CONTENT_URI_ORDINARY_DRINK = buildUri(Path.ORDINARY_DRINK);

  }

  @TableEndpoint(table = DrinkDatabase.GIN)
  public static class Gin {
    @ContentUri(
        path = Path.GIN,
        type = "vnd.android.cursor.dir/drink")
    public static final Uri CONTENT_URI_GIN = buildUri(Path.GIN);

  }

  @TableEndpoint(table = DrinkDatabase.VODKA)
  public static class Vodka {
    @ContentUri(
        path = Path.VODKA,
        type = "vnd.android.cursor.dir/drink")
    public static final Uri CONTENT_URI_VODKA = buildUri(Path.VODKA);

  }

  @TableEndpoint(table = DrinkDatabase.COCKTAIL_GLASS)
  public static class CocktailGlass {
    @ContentUri(
        path = Path.COCKTAIL_GLASS,
        type = "vnd.android.cursor.dir/drink")
    public static final Uri CONTENT_URI_COCKTAIL_GLASS = buildUri(Path.COCKTAIL_GLASS);

  }

  @TableEndpoint(table = DrinkDatabase.HIGHBALL_GLASS)
  public static class ChampagneFlute {
    @ContentUri(
        path = Path.HIGHBALL_GLASS,
        type = "vnd.android.cursor.dir/drink")
    public static final Uri CONTENT_URI_HIGHBALL_GLASS = buildUri(Path.HIGHBALL_GLASS);

  }

  @TableEndpoint(table = DrinkDatabase.SAVED)
  public static class SavedDrink {
    @ContentUri(
        path = Path.SAVED,
        type = "vnd.android.cursor.dir/drink")
    public static final Uri CONTENT_URI_DRINK_SAVED = buildUri(Path.SAVED);


    @InexactContentUri(
        name = "ARCHIVED_DRINK_ID",
        path = Path.SAVED + "/#",
        type = "vnd.android.cursor.item/drink_id",
        whereColumn = _ID,
        pathSegment = 1
    )
    public static Uri withId(String id) {
      return buildUri(Path.SAVED, String.valueOf(id));
    }

  }


}
