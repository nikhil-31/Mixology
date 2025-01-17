package com.capstone.nik.mixology.data;

import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Created by nik on 12/17/2016.
 */

public interface AlcoholicColumn {

    @DataType(DataType.Type.INTEGER)
    @PrimaryKey
    String _ID = "_id";
    @DataType(DataType.Type.TEXT)
    @NotNull
    String DRINK_NAME = "name";
    @DataType(DataType.Type.TEXT)
    @NotNull
    String DRINK_THUMB = "thumb";
}
