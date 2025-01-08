package com.capstone.nik.mixology.Network;

import com.capstone.nik.mixology.Model.Cocktail;
import com.capstone.nik.mixology.Network.remoteModel.Cocktails;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by nik on 1/28/2018.
 */

public interface CocktailService {

    // Has Alcohol Filter
    @GET("filter.php")
    Call<Cocktails> getAlcoholFilter(@Query("a") String filter);

    // Glass Type Filter
    @GET("filter.php")
    Call<Cocktails> getGlassFilter(@Query("g") String filter);

    // Ingredient Filter
    @GET("filter.php")
    Call<Cocktails> getIngredientFilter(@Query("i") String filter);

    // Drink Category (Type) Filter
    @GET("filter.php")
    Call<Cocktails> getDrinkTypeFilter(@Query("c") String filter);

    // Search
    @GET("search.php")
    Call<Cocktails> getSearchResults(@Query("s") String search);

    //Randomixer
    @GET("random.php")
    Call<Cocktails> getRandomixer();

    // Search by id
    @GET("lookup.php")
    Call<Cocktails> getDrinkById(@Query("i") String id);
}
