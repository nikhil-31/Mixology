package com.example.nik.mixology.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nik.mixology.Model.Cocktail;
import com.example.nik.mixology.Model.CocktailDetails;
import com.example.nik.mixology.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by nik on 12/28/2016.
 */

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<CocktailDetails> mCocktailDetails = new ArrayList<>();

    public SearchAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setCocktailList(ArrayList<CocktailDetails> cocktailList) {
        mCocktailDetails = cocktailList;
        notifyDataSetChanged();
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.recycler_search_item, parent, false);
        SearchViewHolder myViewHolder = new SearchViewHolder(v);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(SearchViewHolder holder, int position) {

        final CocktailDetails currentCocktail = mCocktailDetails.get(position);
        holder.textView.setText(currentCocktail.getmName());
        Picasso.with(mContext)
                .load(currentCocktail.getmThumb())
                .error(R.drawable.empty_glass)
                .into(holder.image);

    }

    @Override
    public int getItemCount() {
        return mCocktailDetails.size();
    }

    public class SearchViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView textView;
        ImageView favImage;

        public SearchViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.list_search_icon);
            textView = (TextView) itemView.findViewById(R.id.list_search_text);
            favImage = (ImageView) itemView.findViewById(R.id.list_search_fav);
        }
    }
}
