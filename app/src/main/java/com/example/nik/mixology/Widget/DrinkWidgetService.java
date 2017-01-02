package com.example.nik.mixology.Widget;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Binder;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.nik.mixology.R;
import com.example.nik.mixology.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Objects;

import static com.example.nik.mixology.data.AlcoholicColumn.DRINK_NAME;
import static com.example.nik.mixology.data.AlcoholicColumn.DRINK_THUMB;
import static com.example.nik.mixology.data.AlcoholicColumn._ID;
import static com.example.nik.mixology.data.DrinkProvider.SavedDrink.CONTENT_URI_DRINK_SAVED;

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
        cursor.close();
        return new WidgetDataProvider(this, intent, cursor);
    }


    public class WidgetDataProvider implements RemoteViewsFactory {

        private Context context;
        private Intent intent;
        private Cursor mCursor;


        public WidgetDataProvider(Context context, Intent intent, Cursor cursor) {
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
                    null
            );


            Binder.restoreCallingIdentity(identityToken);

        }

        @Override
        public void onDestroy() {
            mCursor.close();
        }

        @Override
        public int getCount() {
            return mCursor.getCount();
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public RemoteViews getViewAt(int position) {

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.list_item_widget);
            mCursor.moveToPosition(position);

            remoteViews.setTextViewText(R.id.list_widget_text, mCursor.getString(mCursor.getColumnIndex(DRINK_NAME)));

            String thumbUrl = mCursor.getString(mCursor.getColumnIndex(DRINK_THUMB));
            String State_Null = "null";

            if (Objects.equals(thumbUrl, State_Null)) {
                Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.empty_glass);
                remoteViews.setImageViewBitmap(R.id.list_widget_icon, icon);
            } else {
                Bitmap bitmap = null;
                try {
                    bitmap = Picasso.with(context).load(thumbUrl).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                remoteViews.setImageViewBitmap(R.id.list_widget_icon, bitmap);

            }


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
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
