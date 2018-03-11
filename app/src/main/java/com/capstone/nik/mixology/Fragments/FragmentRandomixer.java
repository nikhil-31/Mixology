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

import com.capstone.nik.mixology.Adapters.IngredientsAdapter;
import com.capstone.nik.mixology.Model.Measures;
import com.capstone.nik.mixology.Network.CocktailService;
import com.capstone.nik.mixology.Network.CocktailURLs;
import com.capstone.nik.mixology.Network.MyApplication;
import com.capstone.nik.mixology.Network.remoteModel.Cocktails;
import com.capstone.nik.mixology.Network.remoteModel.Drink;
import com.capstone.nik.mixology.R;
import com.capstone.nik.mixology.utils.ContentProviderHelperMethods;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.capstone.nik.mixology.data.AlcoholicColumn.DRINK_NAME;
import static com.capstone.nik.mixology.data.AlcoholicColumn.DRINK_THUMB;
import static com.capstone.nik.mixology.data.AlcoholicColumn._ID;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentRandomixer extends Fragment {

  public FragmentRandomixer() {
    // Required empty public constructor
  }

  private Activity mActivity;
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

}
