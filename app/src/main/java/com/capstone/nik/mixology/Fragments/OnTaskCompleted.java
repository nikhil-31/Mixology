package com.capstone.nik.mixology.Fragments;

import com.capstone.nik.mixology.Model.CocktailDetails;

import java.util.ArrayList;

/**
 * Created by nikhil on 10-07-2016.
 */

interface OnTaskCompleted {
    void onMySearchTaskCompleted(ArrayList<CocktailDetails> cocktailDetailsArrayList);
}

