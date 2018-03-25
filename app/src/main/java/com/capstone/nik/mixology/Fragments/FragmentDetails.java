package com.capstone.nik.mixology.Fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.nik.mixology.Adapters.IngredientsAdapter;
import com.capstone.nik.mixology.Model.Cocktail;
import com.capstone.nik.mixology.Model.Measures;
import com.capstone.nik.mixology.Network.CocktailService;
import com.capstone.nik.mixology.Network.CocktailURLs;
import com.capstone.nik.mixology.Network.MyApplication;
import com.capstone.nik.mixology.Network.remoteModel.Cocktails;
import com.capstone.nik.mixology.Network.remoteModel.Drink;
import com.capstone.nik.mixology.R;
import com.capstone.nik.mixology.utils.ContentProviderHelperMethods;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.capstone.nik.mixology.Network.CocktailURLs.COCKTAIL_SEARCH_URL_BY_ID;
import static com.capstone.nik.mixology.data.AlcoholicColumn.DRINK_NAME;
import static com.capstone.nik.mixology.data.AlcoholicColumn.DRINK_THUMB;
import static com.capstone.nik.mixology.data.AlcoholicColumn._ID;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentDetails extends Fragment {

  @Inject
  Context applicationContext;

  private String mCocktailId;

  private TextView mInstructionsText;
  private TextView mAlcoholicText;
  private TextView mInstruction;
  private TextView mIngredients;
  private TextView mDrinkName;

  private Toolbar mToolbar;

  private IngredientsAdapter mIngredientsAdapter;

  private ImageView mDrinkImage;
  private ImageView mDetailIcon;

  private boolean isInDatabase;

  private Activity mActivity;
  private LinearLayout mLinearBottom;
  private CocktailService service;

  public FragmentDetails() {
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mActivity = getActivity();
    if (mActivity != null) {
      ((MyApplication) mActivity.getApplication()).getApplicationComponent().inject(this);
    }
  }

  @Override
  public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_activity_details, container, false);
    Cocktail cocktail = mActivity.getIntent().getParcelableExtra(getString(R.string.details_intent_cocktail));

    setHasOptionsMenu(true);

//    MobileAds.initialize(applicationContext, "ca-app-pub-3940256099942544~3347511713");
    //TODO - Uncomment original Ad
    MobileAds.initialize(applicationContext, "ca-app-pub-3940256099942544/6300978111");

    AdView adView = view.findViewById(R.id.adViewDetails);
    AdRequest adRequest = new AdRequest.Builder().build();
    adView.loadAd(adRequest);

    mToolbar = view.findViewById(R.id.toolbar);

    mToolbar.setNavigationIcon(ContextCompat.getDrawable(mActivity, R.drawable.ic_back_black));
    mToolbar.setNavigationContentDescription(getString(R.string.content_desc_up_navigation));
    mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mActivity.finish();
      }
    });

    mToolbar.inflateMenu(R.menu.menu_activity_details);

    mInstructionsText = view.findViewById(R.id.detail_instructions);
    mAlcoholicText = view.findViewById(R.id.detail_alcoholic);
    mDrinkImage = view.findViewById(R.id.detail_imageView);
    mInstruction = view.findViewById(R.id.detail_instructions_text);
    mIngredients = view.findViewById(R.id.detail_ingredients_text);
    mDrinkName = view.findViewById(R.id.detail_name);
    mDetailIcon = view.findViewById(R.id.detail_fav_button);
    mLinearBottom = view.findViewById(R.id.linear_bottom);

    mIngredientsAdapter = new IngredientsAdapter(mActivity);
    RecyclerView ingredientsRecyclerView = view.findViewById(R.id.recycler_ingredients);
    ingredientsRecyclerView.setNestedScrollingEnabled(false);

    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
    ingredientsRecyclerView.setLayoutManager(linearLayoutManager);
    ingredientsRecyclerView.setAdapter(mIngredientsAdapter);


    final Retrofit.Builder builder = new Retrofit.Builder().baseUrl(CocktailURLs.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create());

    Retrofit retrofit = builder.build();
    service = retrofit.create(CocktailService.class);

    if (cocktail != null) {
      startNetworkRequest(cocktail);
    }
    return view;
  }

  public void updateContent(Cocktail cocktail) {
    if (isAdded()) {
      startNetworkRequest(cocktail);
    }
  }

  private void startNetworkRequest(final Cocktail cocktail) {
    mCocktailId = cocktail.getmDrinkId();

    // Sends request to get data from the network
    sendJsonRequest(mCocktailId);

    mToolbar.setTitle(cocktail.getmDrinkName());
    Picasso.with(mActivity).load(cocktail.getmDrinkThumb()).error(R.drawable.empty_glass).into(mDrinkImage);

  }

  private void shareRecipe(ArrayList<Measures> measuresArrayList, Drink drink) {
    final StringBuilder builder = new StringBuilder();

    builder.append(getString(R.string.detail_share_sent_from_mixology)).append(" \n");
    builder.append(getString(R.string.detail_share_name)).append(" ").append(drink.getStrDrink()).append("\n");
    builder.append(getString(R.string.detail_share_alcoholic)).append(" ").append(drink.getStrAlcoholic()).append("\n");
    builder.append(getString(R.string.detail_share_instructions)).append(" ").append("\n").append(drink.getStrInstructions()).append("\n");
    builder.append(getString(R.string.detail_share_ingredients)).append("\n");

    for (int i = 0; i < measuresArrayList.size(); i++) {
      Measures measures = measuresArrayList.get(i);
      builder.append(measures.getIngredient()).append(" -- ").append(measures.getMeasure()).append("\n");
    }

    mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
          startActivity(Intent.createChooser(shareIntent(builder.toString()), getString(R.string.detail_share_via)));
        }
        return false;
      }
    });
  }

  public Intent shareIntent(String data) {
    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
    sharingIntent.setType("text/plain");
    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.detail_share_sent_from_mixology));
    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, data);
    return sharingIntent;
  }

  private void sendJsonRequest(String id) {

    service.getDrinkById(id).enqueue(new Callback<Cocktails>() {
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

      }
    });
  }

  public void setUIData(List<Drink> drinkList) {

    Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_up);

    mLinearBottom.startAnimation(bottomUp);
    mLinearBottom.setVisibility(View.VISIBLE);

    final Drink drink = drinkList.get(0);

    mInstructionsText.setText(drink.getStrInstructions());
    mAlcoholicText.setText(drink.getStrAlcoholic());

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

    ArrayList<Measures> measuresArrayList = new ArrayList<>();

    if (drink.getStrIngredient1() != null && !drink.getStrIngredient1().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient1());
      measure.setMeasure(drink.getStrMeasure1());
      measuresArrayList.add(measure);
    }

    if (drink.getStrIngredient2() != null && !drink.getStrIngredient2().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient2());
      measure.setMeasure(drink.getStrMeasure2());
      measuresArrayList.add(measure);
    }

    if (drink.getStrIngredient3() != null && !drink.getStrIngredient3().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient3());
      measure.setMeasure(drink.getStrMeasure3());
      measuresArrayList.add(measure);
    }

    if (drink.getStrIngredient4() != null && !drink.getStrIngredient4().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient4());
      measure.setMeasure(drink.getStrMeasure4());
      measuresArrayList.add(measure);
    }

    if (drink.getStrIngredient5() != null && !drink.getStrIngredient5().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient5());
      measure.setMeasure(drink.getStrMeasure5());
      measuresArrayList.add(measure);
    }

    if (drink.getStrIngredient6() != null && !drink.getStrIngredient6().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient6());
      measure.setMeasure(drink.getStrMeasure6());
      measuresArrayList.add(measure);
    }

    if (drink.getStrIngredient7() != null && !drink.getStrIngredient7().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient7());
      measure.setMeasure(drink.getStrMeasure7());
      measuresArrayList.add(measure);
    }

    if (drink.getStrIngredient8() != null && !drink.getStrIngredient8().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient8());
      measure.setMeasure(drink.getStrMeasure8());
      measuresArrayList.add(measure);
    }

    if (drink.getStrIngredient9() != null && !drink.getStrIngredient9().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient9());
      measure.setMeasure(drink.getStrMeasure9());
      measuresArrayList.add(measure);
    }

    if (drink.getStrIngredient10() != null && !drink.getStrIngredient10().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient10());
      measure.setMeasure(drink.getStrMeasure10());
      measuresArrayList.add(measure);
    }

    if (drink.getStrIngredient11() != null && !drink.getStrIngredient11().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient11());
      measure.setMeasure(drink.getStrMeasure11());
      measuresArrayList.add(measure);
    }

    if (drink.getStrIngredient12() != null && !drink.getStrIngredient12().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient12());
      measure.setMeasure(drink.getStrMeasure12());
      measuresArrayList.add(measure);
    }

    if (drink.getStrIngredient13() != null && !drink.getStrIngredient13().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient13());
      measure.setMeasure(drink.getStrMeasure13());
      measuresArrayList.add(measure);
    }

    if (drink.getStrIngredient14() != null && !drink.getStrIngredient14().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient14());
      measure.setMeasure(drink.getStrMeasure14());
      measuresArrayList.add(measure);
    }

    if (drink.getStrIngredient15() != null && !drink.getStrIngredient15().equals("")) {
      Measures measure = new Measures();
      measure.setIngredient(drink.getStrIngredient15());
      measure.setMeasure(drink.getStrMeasure15());
      measuresArrayList.add(measure);
    }

    if (isAdded() && mActivity != null) {
      shareRecipe(measuresArrayList, drink);
    }
    mIngredientsAdapter.setMeasuresList(measuresArrayList);
  }

}
