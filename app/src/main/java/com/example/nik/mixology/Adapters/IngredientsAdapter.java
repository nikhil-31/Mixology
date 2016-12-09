package com.example.nik.mixology.Adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nik.mixology.Model.Measures;
import com.example.nik.mixology.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.example.nik.mixology.Network.CocktailURLs.COCKTAIL_INGREDIENTS_URL;
import static com.example.nik.mixology.Network.CocktailURLs.COCKTAIL_INGREDIENT_PNG_SMALL;

/**
 * Created by nik on 12/8/2016.
 */

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.MyViewHolder> {

    ArrayList<Measures> mMeasuresArrayList = new ArrayList<>();
    private Context context;

    private LayoutInflater inflater;


    public IngredientsAdapter(Context context) {
        this.context = context;


        inflater = LayoutInflater.from(context);
    }

    public void setMeasuresList(ArrayList<Measures> measuresList) {
        this.mMeasuresArrayList = measuresList;
        notifyDataSetChanged();
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.recycler_ingredients_item, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(v);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        final Measures measures = mMeasuresArrayList.get(position);
        holder.mIngredientText.setText(measures.getIngredient());
        holder.mMeasureText.setText(measures.getMeasure());
        String ingredient = measures.getIngredient();
        String in = ingredient.replaceAll(" ","%20");

        Picasso.with(context)
                .load(COCKTAIL_INGREDIENTS_URL + in + COCKTAIL_INGREDIENT_PNG_SMALL )
                .error(R.drawable.vodka)
                .into(holder.mIngredientsImage);

    }

    @Override
    public int getItemCount() {
        return mMeasuresArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView mIngredientsImage;
        TextView mIngredientText;
        TextView mMeasureText;

        public MyViewHolder(View itemView) {
            super(itemView);

            mIngredientsImage = (ImageView) itemView.findViewById(R.id.list_ingredients_icon);
            mIngredientText = (TextView) itemView.findViewById(R.id.list_ingredient_text);
            mMeasureText = (TextView) itemView.findViewById(R.id.list_measure_text);

        }
    }
}
