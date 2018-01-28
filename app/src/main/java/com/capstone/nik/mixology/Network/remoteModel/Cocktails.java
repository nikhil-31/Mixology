package com.capstone.nik.mixology.Network.remoteModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by nik on 1/28/2018.
 */

public class Cocktails {
  @SerializedName("drinks")
  @Expose
  private List<Drink> drinks = null;

  public List<Drink> getDrinks() {
    return drinks;
  }

  public void setDrinks(List<Drink> drinks) {
    this.drinks = drinks;
  }
}
