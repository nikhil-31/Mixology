package com.capstone.nik.mixology.Network.remoteModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by nik on 1/28/2018.
 */

public class Drink {

  @SerializedName("strDrink")
  @Expose
  private String strDrink;
  @SerializedName("strDrinkThumb")
  @Expose
  private String strDrinkThumb;
  @SerializedName("idDrink")
  @Expose
  private String idDrink;

  public String getStrDrink() {
    return strDrink;
  }

  public String getStrDrinkThumb() {
    return strDrinkThumb;
  }

  public String getIdDrink() {
    return idDrink;
  }
}

