package com.capstone.nik.mixology.Adapters;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

  private Context mContext;
  private LayoutInflater inflater;
  private Activity mAct;
  private OnAdapterItemSelectedListener mAdapterCallback;
  private boolean isInDatabase;

  public DrinkCursorAdapter(Cursor cursor, Activity activity) {
    super(activity, cursor);
    this.mContext = activity;
    this.mAct = activity;
    inflater = LayoutInflater.from(mContext);
    mAdapterCallback = (OnAdapterItemSelectedListener) mAct;

  }

  @Override
  public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor) {

    DatabaseUtils.dumpCursor(cursor);

    final Cocktail currentCocktail = new Cocktail();

    currentCocktail.setmDrinkId(cursor.getString(cursor.getColumnIndex(_ID)));
    currentCocktail.setmDrinkName(cursor.getString(cursor.getColumnIndex(DRINK_NAME)));
    currentCocktail.setmDrinkThumb(cursor.getString(cursor.getColumnIndex(DRINK_THUMB)));

    viewHolder.textView.setText(cursor.getString(cursor.getColumnIndex(DRINK_NAME)));
    Picasso.with(mContext)
        .load(cursor.getString(cursor.getColumnIndex(DRINK_THUMB)))
        .error(R.drawable.empty_glass)
        .into(viewHolder.image);

    final int position = cursor.getPosition();

    isInDatabase = ContentProviderHelperMethods.isDrinkInDatabase(mAct, cursor.getString(cursor.getColumnIndex(_ID)), CONTENT_URI_DRINK_SAVED);
    if (isInDatabase) {
      viewHolder.imageView.setImageResource(R.drawable.ic_fav_filled);

    } else {
      viewHolder.imageView.setImageResource(R.drawable.ic_fav_unfilled_black);
    }

    viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        cursor.moveToPosition(position);
        isInDatabase = ContentProviderHelperMethods.isDrinkInDatabase(mAct, cursor.getString(cursor.getColumnIndex(_ID)), CONTENT_URI_DRINK_SAVED);
        if (isInDatabase) {

          Snackbar.make(viewHolder.imageView, mContext.getString(R.string.drink_deleted), Snackbar.LENGTH_LONG).show();

          String id = cursor.getString(cursor.getColumnIndex(_ID));
          ContentProviderHelperMethods.deleteData(mAct, id);

          viewHolder.imageView.setImageResource(R.drawable.ic_fav_unfilled_black);

        } else {

          Snackbar.make(viewHolder.imageView, mContext.getString(R.string.drink_added), Snackbar.LENGTH_LONG).show();

          ContentValues cv = new ContentValues();
          cv.put(_ID, cursor.getString(cursor.getColumnIndex(_ID)));
          cv.put(DRINK_NAME, cursor.getString(cursor.getColumnIndex(DRINK_NAME)));
          cv.put(DRINK_THUMB, cursor.getString(cursor.getColumnIndex(DRINK_THUMB)));

          String id = cursor.getString(cursor.getColumnIndex(_ID));
          ContentProviderHelperMethods.insertData(mAct, id, cv);


          viewHolder.imageView.setImageResource(R.drawable.ic_fav_filled);
        }

      }
    });

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
    final ImageView imageView;


    public ViewHolder(View itemView) {
      super(itemView);

      image = (ImageView) itemView.findViewById(R.id.cocktail_image);
      textView = (TextView) itemView.findViewById(R.id.cocktail_text);
      imageView = (ImageView) itemView.findViewById(R.id.cocktail_button);

    }
  }

  public interface OnAdapterItemSelectedListener {
    void onItemSelected(Cocktail id);
  }
}
