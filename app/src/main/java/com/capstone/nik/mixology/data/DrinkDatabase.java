package com.capstone.nik.mixology.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by nik on 12/17/2016.
 */
@Database(version = DrinkDatabase.VERSION)
public class DrinkDatabase {

  private DrinkDatabase() {
  }

  static final int VERSION = 1;

  @Table(AlcoholicColumn.class)
  static final String ALCOHOLIC = "Alcoholic";

  @Table(AlcoholicColumn.class)
  static final String NON_ALCOHOLIC = "Non_Alcoholic";

  @Table(AlcoholicColumn.class)
  static final String COCKTAIL = "Cocktail";

  @Table(AlcoholicColumn.class)
  static final String ORDINARY_DRINK = "Ordinary_Drink";

  @Table(AlcoholicColumn.class)
  static final String GIN = "Gin";

  @Table(AlcoholicColumn.class)
  static final String VODKA = "Vodka";

  @Table(AlcoholicColumn.class)
  static final String COCKTAIL_GLASS = "Cocktail_Glass";

  @Table(AlcoholicColumn.class)
  static final String HIGHBALL_GLASS = "Champagne_Flute";

  @Table(AlcoholicColumn.class)
  static final String SAVED = "Drink_Saved";


}
