package com.example.nik.mixology.Adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nik.mixology.Model.Cocktail;
import com.example.nik.mixology.R;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;

/**
 * Created by nik on 12/7/2016.
 */

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MyViewHolder> {

    private ArrayList<Cocktail> mCocktail = new ArrayList<>();
    private Context context;

    private LayoutInflater inflater;

    private Activity mAct;
    private OnAdapterItemSelectedListener mAdapterCallback;

    public MainAdapter(Context context, Activity activity) {
        this.context = context;
        this.mAct = activity;
        mAdapterCallback = (OnAdapterItemSelectedListener) mAct;
        inflater = LayoutInflater.from(context);
    }

    public void setCocktailList(ArrayList<Cocktail> cocktailList) {
        mCocktail = cocktailList;

        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.recycler_main_item, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(v);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        final Cocktail currentCocktail = mCocktail.get(position);
        holder.textView.setText(currentCocktail.getmDrinkName());
        Picasso.with(context)
                .load(currentCocktail.getmDrinkThumb())
                .error(R.drawable.empty_glass)
                .into(holder.image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAdapterCallback != null){
                    mAdapterCallback.onItemSelected(currentCocktail);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mCocktail.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView textView;


        public MyViewHolder(View itemView) {
            super(itemView);

            image = (ImageView) itemView.findViewById(R.id.cocktail_image);
            textView = (TextView) itemView.findViewById(R.id.cocktail_text);
        }
    }

    public interface OnAdapterItemSelectedListener{
        void onItemSelected(Cocktail id);
    }
}
