package com.capstone.nik.mixology.Network;

import com.capstone.nik.mixology.Network.remoteModel.Cocktails;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by nik on 1/28/2018.
 */

public interface CocktailService {

  @GET("filter.php?a=Alcoholic")
  Call<Cocktails> getCocktails(@Query("a") String filter);

}
