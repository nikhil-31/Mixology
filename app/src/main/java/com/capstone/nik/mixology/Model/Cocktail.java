package com.capstone.nik.mixology.Model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by nik on 12/7/2016.
 */

public class Cocktail implements Parcelable {
    private String mDrinkName;
    private String mDrinkThumb;
    private String mDrinkId;

    public Cocktail(){

    }

    public Cocktail(String Id,String name,String Thumb){
        this.mDrinkId = Id;
        this.mDrinkName = name;
        this.mDrinkThumb = Thumb;
    }

    public String getmDrinkName() {
        return mDrinkName;
    }

    public void setmDrinkName(String mDrinkName) {
        this.mDrinkName = mDrinkName;
    }

    public String getmDrinkThumb() {
        return mDrinkThumb;
    }

    public void setmDrinkThumb(String mDrinkThumb) {
        this.mDrinkThumb = mDrinkThumb;
    }

    public String getmDrinkId() {
        return mDrinkId;
    }

    public void setmDrinkId(String mDrinkId) {
        this.mDrinkId = mDrinkId;
    }


    private Cocktail(Parcel in) {
        mDrinkName = in.readString();
        mDrinkThumb = in.readString();
        mDrinkId = in.readString();
    }

    public static final Creator<Cocktail> CREATOR = new Creator<Cocktail>() {
        @Override
        public Cocktail createFromParcel(Parcel in) {
            return new Cocktail(in);
        }

        @Override
        public Cocktail[] newArray(int size) {
            return new Cocktail[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mDrinkName);
        parcel.writeString(mDrinkThumb);
        parcel.writeString(mDrinkId);
    }



}
