package com.capstone.nik.mixology.Fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.capstone.nik.mixology.Adapters.IngredientsAdapter;
import com.capstone.nik.mixology.Model.CocktailDetails;
import com.capstone.nik.mixology.Model.Measures;
import com.capstone.nik.mixology.Network.CocktailService;
import com.capstone.nik.mixology.Network.CocktailURLs;
import com.capstone.nik.mixology.Network.MyApplication;
import com.capstone.nik.mixology.Network.remoteModel.Cocktails;
import com.capstone.nik.mixology.Network.remoteModel.Drink;
import com.capstone.nik.mixology.R;
import com.capstone.nik.mixology.utils.ContentProviderHelperMethods;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.capstone.nik.mixology.Network.CocktailURLs.COCKTAIL_URL_RANDOM;
import static com.capstone.nik.mixology.data.AlcoholicColumn.DRINK_NAME;
import static com.capstone.nik.mixology.data.AlcoholicColumn.DRINK_THUMB;
import static com.capstone.nik.mixology.data.AlcoholicColumn._ID;
import static com.capstone.nik.mixology.data.DrinkProvider.SavedDrink.CONTENT_URI_DRINK_SAVED;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentRandomixer extends Fragment {

  public FragmentRandomixer() {
    // Required empty public constructor
  }


  private ArrayList<Measures> mMeasuresArrayList;
  private Activity mActivity;

  // Volley
  @Inject
  RequestQueue mRequestQueue;

  private SwipeRefreshLayout mSwipeToRefreshLayout;
  private IngredientsAdapter mIngredientsAdapter;

  private TextView mInstructionsText;
  private TextView mAlcoholicText;
  private TextView mInstruction;
  private TextView mIngredients;
  private ImageView mDrinkImage;
  private TextView mDrinkName;
  private ImageView mDetailIcon;

  private boolean isInDatabase;
  private CocktailService service;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mActivity = getActivity();
    if (mActivity != null) {
      ((MyApplication) mActivity.getApplication()).getApplicationComponent().inject(this);
    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.fragment_randomixer, container, false);
    setHasOptionsMenu(true);
    mSwipeToRefreshLayout = rootView.findViewById(R.id.swipeToRefreshLayout);

    mInstructionsText = rootView.findViewById(R.id.detail_instructions);
    mAlcoholicText = rootView.findViewById(R.id.detail_alcoholic);
    mDrinkImage = rootView.findViewById(R.id.detail_imageView);
    mInstruction = rootView.findViewById(R.id.detail_instructions_text);
    mIngredients = rootView.findViewById(R.id.detail_ingredients_text);
    mDrinkName = rootView.findViewById(R.id.detail_name);
    mDetailIcon = rootView.findViewById(R.id.detail_fav_button);

    mIngredientsAdapter = new IngredientsAdapter(mActivity);
    RecyclerView ingredientsRecyclerView = rootView.findViewById(R.id.recycler_ingredients);

    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);

    ingredientsRecyclerView.setLayoutManager(linearLayoutManager);
    ingredientsRecyclerView.setAdapter(mIngredientsAdapter);
    ingredientsRecyclerView.setNestedScrollingEnabled(false);

    final Retrofit.Builder builder = new Retrofit.Builder().baseUrl(CocktailURLs.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create());

    Retrofit retrofit = builder.build();
    service = retrofit.create(CocktailService.class);

    mSwipeToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        sendJsonRequest();
        mSwipeToRefreshLayout.setRefreshing(false);
      }
    });

    if (savedInstanceState == null) {
      sendJsonRequest();
    }
    return rootView;
  }


  private void sendJsonRequest() {

    service.getRandomixer().enqueue(new Callback<Cocktails>() {
      @Override
      public void onResponse(@NonNull Call<Cocktails> call, @NonNull retrofit2.Response<Cocktails> response) {
        Cocktails cocktails = response.body();

        if (cocktails != null) {
          List<Drink> drinks = cocktails.getDrinks();
          if (drinks != null && drinks.size() != 0) {
            setUIData(drinks);
          }
        }
      }

      @Override
      public void onFailure(@NonNull Call<Cocktails> call, @NonNull Throwable t) {
        t.printStackTrace();
      }
    });

  }

  public void setUIData(List<Drink> drinkList) {

    final Drink drink = drinkList.get(0);

    mInstructionsText.setText(drink.getStrInstructions());
    mAlcoholicText.setText(drink.getStrAlcoholic());

    String url = drink.getStrDrinkThumb();
    Picasso.with(mActivity).load(url).error(R.drawable.empty_glass).into(mDrinkImage);

    mInstruction.setText(getResources().getString(R.string.Instructions));
    mIngredients.setText(getResources().getString(R.string.Ingredients));

    mDrinkName.setText(drink.getStrDrink());
    isInDatabase = ContentProviderHelperMethods.isDrinkSavedInDb(mActivity, drink.getIdDrink());

    if (isInDatabase) {
      mDetailIcon.setImageResource(R.drawable.ic_fav_filled);
    } else {
      mDetailIcon.setImageResource(R.drawable.ic_fav_unfilled_black);
    }

    mDetailIcon.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        isInDatabase = ContentProviderHelperMethods.isDrinkSavedInDb(mActivity, drink.getIdDrink());
        if (isInDatabase) {
          mDetailIcon.setImageResource(R.drawable.ic_fav_filled);

          Snackbar.make(mDetailIcon, getString(R.string.drink_deleted), Snackbar.LENGTH_LONG).show();
          ContentProviderHelperMethods.deleteData(mActivity, drink.getIdDrink());
          mDetailIcon.setImageResource(R.drawable.ic_fav_unfilled_black);

        } else {
          mDetailIcon.setImageResource(R.drawable.ic_fav_unfilled_black);

          Snackbar.make(mDetailIcon, getString(R.string.drink_added), Snackbar.LENGTH_LONG).show();

          ContentValues cv = new ContentValues();
          cv.put(_ID, drink.getIdDrink());
          cv.put(DRINK_NAME, drink.getStrDrink());
          cv.put(DRINK_THUMB, drink.getStrDrinkThumb());
          ContentProviderHelperMethods.insertData(mActivity, drink.getIdDrink(), cv);

          mDetailIcon.setImageResource(R.drawable.ic_fav_filled);
        }
      }
    });

    ArrayList<Measures> mMeasures = new ArrayList<>();

    if (drink.getStrIngredient1() != null && !drink.getStrIngredient1().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient1());
      measure.setMeasure(drink.getStrMeasure1());
      mMeasures.add(measure);
    }

    if (drink.getStrIngredient2() != null && !drink.getStrIngredient2().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient2());
      measure.setMeasure(drink.getStrMeasure2());
      mMeasures.add(measure);
    }

    if (drink.getStrIngredient3() != null && !drink.getStrIngredient3().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient3());
      measure.setMeasure(drink.getStrMeasure3());
      mMeasures.add(measure);
    }

    if (drink.getStrIngredient4() != null && !drink.getStrIngredient4().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient4());
      measure.setMeasure(drink.getStrMeasure4());
      mMeasures.add(measure);
    }

    if (drink.getStrIngredient5() != null && !drink.getStrIngredient5().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient5());
      measure.setMeasure(drink.getStrMeasure5());
      mMeasures.add(measure);
    }


    if (drink.getStrIngredient6() != null && !drink.getStrIngredient6().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient6());
      measure.setMeasure(drink.getStrMeasure6());
      mMeasures.add(measure);
    }

    if (drink.getStrIngredient7() != null && !drink.getStrIngredient7().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient7());
      measure.setMeasure(drink.getStrMeasure7());
      mMeasures.add(measure);
    }

    if (drink.getStrIngredient8() != null && !drink.getStrIngredient8().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient8());
      measure.setMeasure(drink.getStrMeasure8());
      mMeasures.add(measure);
    }

    if (drink.getStrIngredient9() != null && !drink.getStrIngredient9().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient9());
      measure.setMeasure(drink.getStrMeasure9());
      mMeasures.add(measure);
    }

    if (drink.getStrIngredient10() != null && !drink.getStrIngredient10().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient10());
      measure.setMeasure(drink.getStrMeasure10());
      mMeasures.add(measure);
    }

    if (drink.getStrIngredient11() != null && !drink.getStrIngredient11().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient11());
      measure.setMeasure(drink.getStrMeasure11());
      mMeasures.add(measure);
    }


    if (drink.getStrIngredient12() != null && !drink.getStrIngredient12().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient12());
      measure.setMeasure(drink.getStrMeasure12());
      mMeasures.add(measure);
    }


    if (drink.getStrIngredient13() != null && !drink.getStrIngredient13().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient13());
      measure.setMeasure(drink.getStrMeasure13());
      mMeasures.add(measure);
    }


    if (drink.getStrIngredient14() != null && !drink.getStrIngredient14().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient14());
      measure.setMeasure(drink.getStrMeasure14());
      mMeasures.add(measure);
    }

    if (drink.getStrIngredient15() != null && !drink.getStrIngredient15().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient15());
      measure.setMeasure(drink.getStrMeasure15());
      mMeasures.add(measure);
    }


    mIngredientsAdapter.setMeasuresList(mMeasures);
  }

//  public CocktailDetails parseJSONResponse(JSONObject response) throws JSONException {
//    final String DRINKS = "drinks";
//    final String ID = "idDrink";
//    final String NAME = "strDrink";
//    final String CATEGORY = "strCategory";
//    final String ALCOHOLIC = "strAlcoholic";
//    final String GLASS = "strGlass";
//    final String INSTRUCTIONS = "strInstructions";
//    final String THUMB = "strDrinkThumb";
//
//    CocktailDetails details = new CocktailDetails();
//
//    if (response == null || response.length() == 0) {
//      return details;
//    }
//    JSONArray results = response.getJSONArray(DRINKS);
//
//    for (int i = 0; i < results.length(); i++) {
//      JSONObject drink = results.getJSONObject(i);
//      if (drink.getString(NAME).length() != 0 && !drink.isNull(NAME)) {
//        details.setmName(drink.getString(NAME));
//      }
//      if (drink.getString(CATEGORY).length() != 0 && !drink.isNull(CATEGORY)) {
//        details.setmCategory(drink.getString(CATEGORY));
//      }
//      if (drink.getString(ALCOHOLIC).length() != 0 && !drink.isNull(ALCOHOLIC)) {
//        details.setmAlcoholic(drink.getString(ALCOHOLIC));
//      }
//      if (drink.getString(GLASS).length() != 0 && !drink.isNull(GLASS)) {
//        details.setmGlass(drink.getString(GLASS));
//      }
//      if (drink.getString(INSTRUCTIONS).length() != 0 && !drink.isNull(INSTRUCTIONS)) {
//        details.setmInstructions(drink.getString(INSTRUCTIONS));
//      }
//      if (drink.getString(THUMB).length() != 0 && !drink.isNull(THUMB)) {
//        details.setmThumb(drink.getString(THUMB));
//      }
//      if (drink.getString(ID).length() != 0 && !drink.isNull(ID)) {
//        details.setmId(drink.getString(ID));
//      }
//    }
//    return details;
//  }

  public ArrayList<Measures> parseJSONResponseMeasure(JSONObject response) throws JSONException {

    final String DRINKS = "drinks";
    final String INGREDIENT_1 = "strIngredient1";
    final String INGREDIENT_2 = "strIngredient2";
    final String INGREDIENT_3 = "strIngredient3";
    final String INGREDIENT_4 = "strIngredient4";
    final String INGREDIENT_5 = "strIngredient5";
    final String INGREDIENT_6 = "strIngredient6";
    final String INGREDIENT_7 = "strIngredient7";
    final String INGREDIENT_8 = "strIngredient8";
    final String INGREDIENT_9 = "strIngredient9";
    final String INGREDIENT_10 = "strIngredient10";
    final String INGREDIENT_11 = "strIngredient11";
    final String INGREDIENT_12 = "strIngredient12";
    final String INGREDIENT_13 = "strIngredient13";
    final String INGREDIENT_14 = "strIngredient14";
    final String INGREDIENT_15 = "strIngredient15";
    final String MEASURE_1 = "strMeasure1";
    final String MEASURE_2 = "strMeasure2";
    final String MEASURE_3 = "strMeasure3";
    final String MEASURE_4 = "strMeasure4";
    final String MEASURE_5 = "strMeasure5";
    final String MEASURE_6 = "strMeasure6";
    final String MEASURE_7 = "strMeasure7";
    final String MEASURE_8 = "strMeasure8";
    final String MEASURE_9 = "strMeasure9";
    final String MEASURE_10 = "strMeasure10";
    final String MEASURE_11 = "strMeasure11";
    final String MEASURE_12 = "strMeasure12";
    final String MEASURE_13 = "strMeasure13";
    final String MEASURE_14 = "strMeasure14";
    final String MEASURE_15 = "strMeasure15";

    ArrayList<Measures> mMeasures = new ArrayList<>();

    JSONArray results = response.getJSONArray(DRINKS);

    for (int i = 0; i < results.length(); i++) {

      JSONObject drink = results.getJSONObject(i);
      if (drink.getString(INGREDIENT_1).length() != 0 && !drink.isNull(INGREDIENT_1)) {
        Measures measure = new Measures();
        measure.setIngredient(drink.getString(INGREDIENT_1));
        measure.setMeasure(drink.getString(MEASURE_1));
        mMeasures.add(measure);
      }

      if (drink.getString(INGREDIENT_2).length() != 0 && !drink.isNull(INGREDIENT_2)) {
        Measures measure = new Measures();
        measure.setIngredient(drink.getString(INGREDIENT_2));
        measure.setMeasure(drink.getString(MEASURE_2));
        mMeasures.add(measure);
      }

      if (drink.getString(INGREDIENT_3).length() != 0 && !drink.isNull(INGREDIENT_3)) {
        Measures measure = new Measures();
        measure.setIngredient(drink.getString(INGREDIENT_3));
        measure.setMeasure(drink.getString(MEASURE_3));
        mMeasures.add(measure);
      }

      if (drink.getString(INGREDIENT_4).length() != 0 && !drink.isNull(INGREDIENT_4)) {
        Measures measure = new Measures();
        measure.setIngredient(drink.getString(INGREDIENT_4));
        measure.setMeasure(drink.getString(MEASURE_4));
        mMeasures.add(measure);
      }

      if (drink.getString(INGREDIENT_5).length() != 0 && !drink.isNull(INGREDIENT_5)) {
        Measures measure = new Measures();
        measure.setIngredient(drink.getString(INGREDIENT_5));
        measure.setMeasure(drink.getString(MEASURE_5));
        mMeasures.add(measure);
      }

      if (drink.getString(INGREDIENT_6).length() != 0 && !drink.isNull(INGREDIENT_6)) {
        Measures measure = new Measures();
        measure.setIngredient(drink.getString(INGREDIENT_6));
        measure.setMeasure(drink.getString(MEASURE_6));
        mMeasures.add(measure);
      }

      if (drink.getString(INGREDIENT_7).length() != 0 && !drink.isNull(INGREDIENT_7)) {
        Measures measure = new Measures();
        measure.setIngredient(drink.getString(INGREDIENT_7));
        measure.setMeasure(drink.getString(MEASURE_7));
        mMeasures.add(measure);
      }

      if (drink.getString(INGREDIENT_8).length() != 0 && !drink.isNull(INGREDIENT_8)) {
        Measures measure = new Measures();
        measure.setIngredient(drink.getString(INGREDIENT_8));
        measure.setMeasure(drink.getString(MEASURE_8));
        mMeasures.add(measure);
      }

      if (drink.getString(INGREDIENT_9).length() != 0 && !drink.isNull(INGREDIENT_9)) {
        Measures measure = new Measures();
        measure.setIngredient(drink.getString(INGREDIENT_9));
        measure.setMeasure(drink.getString(MEASURE_9));
        mMeasures.add(measure);
      }

      if (drink.getString(INGREDIENT_10).length() != 0 && !drink.isNull(INGREDIENT_10)) {
        Measures measure = new Measures();
        measure.setIngredient(drink.getString(INGREDIENT_10));
        measure.setMeasure(drink.getString(MEASURE_10));
        mMeasures.add(measure);
      }

      if (drink.getString(INGREDIENT_11).length() != 0 && !drink.isNull(INGREDIENT_11)) {
        Measures measure = new Measures();
        measure.setIngredient(drink.getString(INGREDIENT_11));
        measure.setMeasure(drink.getString(MEASURE_11));
        mMeasures.add(measure);
      }

      if (drink.getString(INGREDIENT_12).length() != 0 && !drink.isNull(INGREDIENT_12)) {
        Measures measure = new Measures();
        measure.setIngredient(drink.getString(INGREDIENT_12));
        measure.setMeasure(drink.getString(MEASURE_12));
        mMeasures.add(measure);
      }

      if (drink.getString(INGREDIENT_13).length() != 0 && !drink.isNull(INGREDIENT_13)) {
        Measures measure = new Measures();
        measure.setIngredient(drink.getString(INGREDIENT_13));
        measure.setMeasure(drink.getString(MEASURE_13));
        mMeasures.add(measure);
      }

      if (drink.getString(INGREDIENT_14).length() != 0 && !drink.isNull(INGREDIENT_14)) {
        Measures measure = new Measures();
        measure.setIngredient(drink.getString(INGREDIENT_14));
        measure.setMeasure(drink.getString(MEASURE_14));
        mMeasures.add(measure);
      }

      if (drink.getString(INGREDIENT_15).length() != 0 && !drink.isNull(INGREDIENT_15)) {
        Measures measure = new Measures();
        measure.setIngredient(drink.getString(INGREDIENT_15));
        measure.setMeasure(drink.getString(MEASURE_15));
        mMeasures.add(measure);
      }
    }
    return mMeasures;
  }


}
