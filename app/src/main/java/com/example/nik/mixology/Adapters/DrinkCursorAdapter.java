package com.example.nik.mixology.Adapters;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.nik.mixology.Model.Cocktail;
import com.example.nik.mixology.R;
import com.example.nik.mixology.utils.ContentProviderHelperMethods;
import com.example.nik.mixology.utils.CursorRecyclerViewAdapter;
import com.squareup.picasso.Picasso;

import static com.example.nik.mixology.data.AlcoholicColumn.DRINK_NAME;
import static com.example.nik.mixology.data.AlcoholicColumn.DRINK_THUMB;
import static com.example.nik.mixology.data.AlcoholicColumn._ID;

/**
 * Created by nik on 12/19/2016.
 */

public class DrinkCursorAdapter extends CursorRecyclerViewAdapter<DrinkCursorAdapter.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private Activity mAct;
    private OnAdapterItemSelectedListener mAdapterCallback;
    private Uri contentUri;
    private boolean isInDatabase;

    public DrinkCursorAdapter(Context context, Cursor cursor, Activity activity, Uri contentUri) {
        super(context, cursor);
        this.contentUri = contentUri;
        this.context = context;
        this.mAct = activity;
        inflater = LayoutInflater.from(context);
        mAdapterCallback = (OnAdapterItemSelectedListener) mAct;

    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        DatabaseUtils.dumpCursor(cursor);

        final Cocktail currentCocktail = new Cocktail();

        currentCocktail.setmDrinkId(cursor.getString(cursor.getColumnIndex(_ID)));
        currentCocktail.setmDrinkName(cursor.getString(cursor.getColumnIndex(DRINK_NAME)));
        currentCocktail.setmDrinkThumb(cursor.getString(cursor.getColumnIndex(DRINK_THUMB)));

        viewHolder.textView.setText(cursor.getString(cursor.getColumnIndex(DRINK_NAME)));
        Picasso.with(context)
                .load(cursor.getString(cursor.getColumnIndex(DRINK_THUMB)))
                .error(R.drawable.empty_glass)
                .into(viewHolder.image);

        isInDatabase = ContentProviderHelperMethods.isDrinkInDatabase(mAct,cursor.getString(cursor.getColumnIndex(_ID)),contentUri);
        if(isInDatabase){
            viewHolder.imageButton.setImageResource(R.drawable.ic_favourite_filled_red);
        }
        else {
            viewHolder.imageButton.setImageResource(R.drawable.ic_favourite_outline_red);
        }




        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAdapterCallback != null) {
                    mAdapterCallback.onItemSelected(currentCocktail);
                }
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = inflater.inflate(R.layout.recycler_main_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;

    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView textView;
        ImageView imageButton;


        public ViewHolder(View itemView) {
            super(itemView);

            image = (ImageView) itemView.findViewById(R.id.cocktail_image);
            textView = (TextView) itemView.findViewById(R.id.cocktail_text);
            imageButton = (ImageView) itemView.findViewById(R.id.cocktail_button);

        }
    }

    public interface OnAdapterItemSelectedListener {
        void onItemSelected(Cocktail id);
    }
}
