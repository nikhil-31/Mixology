package com.example.nik.mixology.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by nik on 12/17/2016.
 */
@Database(version = DrinkDatabase.VERSION)
public class DrinkDatabase {

    private DrinkDatabase() {
    }

    public static final int VERSION = 1;

    @Table(columnDrink.class) public static final String DRINK_SAVED = "drinkSaved";


}
