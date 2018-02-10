package com.capstone.nik.mixology.Adapters;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.Tag;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.capstone.nik.mixology.Model.Cocktail;
import com.capstone.nik.mixology.R;
import com.capstone.nik.mixology.utils.ContentProviderHelperMethods;
import com.capstone.nik.mixology.utils.CursorRecyclerViewAdapter;
import com.squareup.picasso.Picasso;

import static com.capstone.nik.mixology.data.AlcoholicColumn.DRINK_NAME;
import static com.capstone.nik.mixology.data.AlcoholicColumn.DRINK_THUMB;
import static com.capstone.nik.mixology.data.AlcoholicColumn._ID;
import static com.capstone.nik.mixology.data.DrinkProvider.SavedDrink.CONTENT_URI_DRINK_SAVED;

/**
 * Created by nik on 12/19/2016.
 */

public class DrinkCursorAdapter extends CursorRecyclerViewAdapter<DrinkCursorAdapter.ViewHolder> {
  private static final String TAG = "DrinkCursorAdapter";

  private LayoutInflater inflater;
  private Activity mAct;
  private OnAdapterItemSelectedListener mAdapterCallback;

  public DrinkCursorAdapter(Cursor cursor, Activity activity) {
    super(activity, cursor);
    this.mAct = activity;
    inflater = LayoutInflater.from(mAct);
    mAdapterCallback = (OnAdapterItemSelectedListener) mAct;
  }

  @Override
  public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor) {
    viewHolder.textView.setText(cursor.getString(cursor.getColumnIndex(DRINK_NAME)));
    String url = "http://" + cursor.getString(cursor.getColumnIndex(DRINK_THUMB)).trim();

    Picasso picasso = new Picasso.Builder(mAct)
        .listener(new Picasso.Listener() {
          @Override
          public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
            //Here your log
            Log.v(TAG, "Error ," + exception);
          }
        })
        .build();

    picasso.load(url).error(R.drawable.empty_glass).into(viewHolder.image);

    boolean isInDatabase = ContentProviderHelperMethods.isDrinkSavedInDb(mAct
        , cursor.getString(cursor.getColumnIndex(_ID)));
    if (isInDatabase) {
      viewHolder.imageView.setImageResource(R.drawable.ic_fav_filled);
    } else {
      viewHolder.imageView.setImageResource(R.drawable.ic_fav_unfilled_black);
    }
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = inflater.inflate(R.layout.recycler_item_main, parent, false);
    return new ViewHolder(v);
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    ImageView image;
    TextView textView;
    final ImageView imageView;

    public ViewHolder(View itemView) {
      super(itemView);

      image = itemView.findViewById(R.id.cocktail_image);
      textView = itemView.findViewById(R.id.cocktail_text);
      imageView = itemView.findViewById(R.id.cocktail_button);

      imageView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Cursor cursor = getCursor();
          cursor.moveToPosition(getAdapterPosition());

          String cocktailId = cursor.getString(cursor.getColumnIndex(_ID));

          boolean isInDatabase = ContentProviderHelperMethods.isDrinkSavedInDb(mAct, cocktailId);
          if (isInDatabase) {
            Snackbar.make(imageView, mAct.getString(R.string.drink_deleted), Snackbar.LENGTH_LONG).show();

            String id = cursor.getString(cursor.getColumnIndex(_ID));
            ContentProviderHelperMethods.deleteData(mAct, id);

            imageView.setImageResource(R.drawable.ic_fav_unfilled_black);
          } else {
            Snackbar.make(imageView, mAct.getString(R.string.drink_added), Snackbar.LENGTH_LONG).show();

            ContentValues cv = new ContentValues();
            cv.put(_ID, cursor.getString(cursor.getColumnIndex(_ID)));
            cv.put(DRINK_NAME, cursor.getString(cursor.getColumnIndex(DRINK_NAME)));
            cv.put(DRINK_THUMB, cursor.getString(cursor.getColumnIndex(DRINK_THUMB)));

            String id = cursor.getString(cursor.getColumnIndex(_ID));
            ContentProviderHelperMethods.insertData(mAct, id, cv);

            imageView.setImageResource(R.drawable.ic_fav_filled);
          }
        }
      });

      itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (mAdapterCallback != null) {
            Cursor cursor = getCursor();
            cursor.moveToPosition(getAdapterPosition());
            Cocktail cocktail = new Cocktail();
            cocktail.setmDrinkId(cursor.getString(cursor.getColumnIndex(_ID)));
            cocktail.setmDrinkName(cursor.getString(cursor.getColumnIndex(DRINK_NAME)));
            cocktail.setmDrinkThumb(cursor.getString(cursor.getColumnIndex(DRINK_THUMB)));
            mAdapterCallback.onItemSelected(cocktail);
          }
        }
      });
    }
  }

  public interface OnAdapterItemSelectedListener {
    void onItemSelected(Cocktail cocktail);
  }
}
