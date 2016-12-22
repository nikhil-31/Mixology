package com.example.nik.mixology.Adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.nik.mixology.Model.Cocktail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by nik on 12/22/2016.
 */

public class RandomixerAdapter extends RecyclerView.Adapter<RandomixerAdapter.RandomHolder>{


    private ArrayList<Cocktail> mCocktail = new ArrayList<>();
    private Context context;

    private LayoutInflater inflater;

    public RandomixerAdapter(Context context) {
        this.context = context;

    }
    public void setCocktailList(ArrayList<Cocktail> cocktailList) {
        long seed = System.nanoTime();


        mCocktail = cocktailList;

        notifyDataSetChanged();
    }


    @Override
    public RandomHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RandomHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class RandomHolder extends RecyclerView.ViewHolder {
        public RandomHolder(View itemView) {
            super(itemView);
        }
    }
}
