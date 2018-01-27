package com.capstone.nik.mixology.Widget;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.capstone.nik.mixology.Model.Cocktail;
import com.capstone.nik.mixology.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Objects;

import static com.capstone.nik.mixology.data.AlcoholicColumn.DRINK_NAME;
import static com.capstone.nik.mixology.data.AlcoholicColumn.DRINK_THUMB;
import static com.capstone.nik.mixology.data.AlcoholicColumn._ID;
import static com.capstone.nik.mixology.data.DrinkProvider.SavedDrink.CONTENT_URI_DRINK_SAVED;

/**
 * Created by nik on 12/31/2016.
 */

public class DrinkWidgetService extends RemoteViewsService {

  @Override
  public RemoteViewsFactory onGetViewFactory(Intent intent) {

    String[] projection = {
        _ID,
        DRINK_NAME,
        DRINK_THUMB
    };

    Cursor cursor = this.getContentResolver().query(
        CONTENT_URI_DRINK_SAVED,
        projection,
        null,
        null,
        null
    );
    assert cursor != null;
    cursor.close();
    return new WidgetDataProvider(this, intent, cursor);
  }


  public class WidgetDataProvider implements RemoteViewsFactory {

    private Context context;
    private Intent intent;
    private Cursor mCursor = null;

    WidgetDataProvider(Context context, Intent intent, Cursor cursor) {
      this.context = context;
      this.intent = intent;
      mCursor = cursor;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {

      if (mCursor != null) {
        mCursor.close();
      }

      final long identityToken = Binder.clearCallingIdentity();

      String[] projection = {
          _ID,
          DRINK_NAME,
          DRINK_THUMB
      };

      mCursor = DrinkWidgetService.this.getContentResolver().query(
          CONTENT_URI_DRINK_SAVED,
          projection,
          null,
          null,
          null);

      Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {
      if (mCursor != null) {
        mCursor.close();
        mCursor = null;
      }
    }

    @Override
    public int getCount() {
      return mCursor == null ? 0 : mCursor.getCount();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public RemoteViews getViewAt(int position) {

      if (position == AdapterView.INVALID_POSITION ||
          mCursor == null || !mCursor.moveToPosition(position)) {
        return null;
      }

      RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.list_item_widget);
      mCursor.moveToPosition(position);

      remoteViews.setTextViewText(R.id.list_widget_text, mCursor.getString(mCursor.getColumnIndex(DRINK_NAME)));

      String thumbUrl = mCursor.getString(mCursor.getColumnIndex(DRINK_THUMB));

      if (thumbUrl.equals("null")) {
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.empty_glass);
        remoteViews.setImageViewBitmap(R.id.list_widget_icon, icon);
      } else {
        try {
          Bitmap bitmap = Picasso.with(context).load(thumbUrl).get();
          remoteViews.setImageViewBitmap(R.id.list_widget_icon, bitmap);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      final Intent fillInIntent = new Intent();

      Cocktail cocktail = new Cocktail();
      cocktail.setmDrinkId(mCursor.getString(mCursor.getColumnIndex(_ID)));
      cocktail.setmDrinkName(mCursor.getString(mCursor.getColumnIndex(DRINK_NAME)));
      cocktail.setmDrinkThumb(mCursor.getString(mCursor.getColumnIndex(DRINK_THUMB)));

      fillInIntent.putExtra(getString(R.string.details_intent_cocktail), cocktail);
      remoteViews.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

      return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
      return null;
    }

    @Override
    public int getViewTypeCount() {
      return 1;
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public boolean hasStableIds() {
      return true;
    }
  }
}
