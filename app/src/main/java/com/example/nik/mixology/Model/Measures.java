package com.example.nik.mixology.Model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by nik on 12/8/2016.
 */

public class Measures implements Parcelable {
    private String Ingredient;
    private String Measure;

    public Measures(){

    }

    public String getIngredient() {
        return Ingredient;
    }

    public void setIngredient(String ingredient) {
        Ingredient = ingredient;
    }

    public String getMeasure() {
        return Measure;
    }

    public void setMeasure(String measure) {
        Measure = measure;
    }


    private Measures(Parcel in) {
        Ingredient = in.readString();
        Measure = in.readString();
    }

    public static final Creator<Measures> CREATOR = new Creator<Measures>() {
        @Override
        public Measures createFromParcel(Parcel in) {
            return new Measures(in);
        }

        @Override
        public Measures[] newArray(int size) {
            return new Measures[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(Ingredient);
        parcel.writeString(Measure);
    }


}
