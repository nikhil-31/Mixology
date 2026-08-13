package com.capstone.nik.mixology.Adapters;

import android.app.Activity;

import com.google.android.material.snackbar.Snackbar;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.capstone.nik.mixology.Model.Cocktail;
import com.capstone.nik.mixology.Network.remoteModel.Drink;
import com.capstone.nik.mixology.R;
import com.capstone.nik.mixology.utils.ContentProviderHelperMethods;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nik on 12/28/2016.
 */

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private LayoutInflater mInflater;
    private List<Drink> mCocktailDetails = new ArrayList<>();
    private boolean isInDatabase;
    private Activity mAct;
    private OnAdapterItemSelectedListener mAdapterCallback;

    public SearchAdapter(Activity activity) {
        mAct = activity;
        mInflater = LayoutInflater.from(activity);
        mAdapterCallback = (OnAdapterItemSelectedListener) mAct;
    }

    public void setCocktailList(List<Drink> cocktailList) {
        mCocktailDetails = cocktailList;
        notifyDataSetChanged();
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.recycler_item_search, parent, false);
        return new SearchViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final SearchViewHolder holder, int position) {

        Drink currentCocktail = mCocktailDetails.get(position);
        holder.textView.setText(currentCocktail.getStrDrink());
        Picasso.get()
                .load(currentCocktail.getStrDrinkThumb())
                .error(R.drawable.empty_glass)
                .into(holder.image);

        isInDatabase = ContentProviderHelperMethods.isDrinkSavedInDb(mAct, currentCocktail.getIdDrink());

        if (isInDatabase) {
            holder.imageButton.setImageResource(R.drawable.ic_fav_filled);
        } else {
            holder.imageButton.setImageResource(R.drawable.ic_fav_unfilled_black);
        }
    }

    @Override
    public int getItemCount() {
        return mCocktailDetails.size();
    }

    class SearchViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView textView;
        ImageView imageButton;

        SearchViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.list_search_icon);
            textView = itemView.findViewById(R.id.list_search_text);
            imageButton = itemView.findViewById(R.id.list_search_fav);

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Drink currentCocktail = mCocktailDetails.get(getAdapterPosition());

                    isInDatabase = ContentProviderHelperMethods.isDrinkSavedInDb(mAct, currentCocktail.getIdDrink());

                    if (isInDatabase) {
                        imageButton.setImageResource(R.drawable.ic_fav_filled);

                        Snackbar.make(imageButton, mAct.getString(R.string.drink_deleted), Snackbar.LENGTH_LONG).show();
                        ContentProviderHelperMethods.deleteData(mAct, currentCocktail.getIdDrink());
                        imageButton.setImageResource(R.drawable.ic_fav_unfilled_black);

                    } else {
                        imageButton.setImageResource(R.drawable.ic_fav_unfilled_black);

                        Snackbar.make(imageButton, mAct.getString(R.string.drink_added), Snackbar.LENGTH_LONG).show();
                        ContentProviderHelperMethods.insertData(
                                mAct,
                                new Cocktail(
                                        currentCocktail.getIdDrink(),
                                        currentCocktail.getStrDrink(),
                                        currentCocktail.getStrDrinkThumb()
                                )
                        );
                        imageButton.setImageResource(R.drawable.ic_fav_filled);
                    }
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Drink currentCocktail = mCocktailDetails.get(getAdapterPosition());

                    final Cocktail cocktail = new Cocktail();
                    cocktail.setmDrinkName(currentCocktail.getStrDrink());
                    cocktail.setmDrinkId(currentCocktail.getIdDrink());
                    cocktail.setmDrinkThumb(currentCocktail.getStrDrinkThumb());

                    if (mAdapterCallback != null) {
                        mAdapterCallback.onItemSelected(cocktail);
                    }
                }
            });
        }
    }

    public interface OnAdapterItemSelectedListener {
        void onItemSelected(Cocktail id);
    }
}
