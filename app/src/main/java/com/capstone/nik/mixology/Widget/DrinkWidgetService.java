package com.capstone.nik.mixology.Widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.capstone.nik.mixology.Model.Cocktail;
import com.capstone.nik.mixology.Network.MyApplication;
import com.capstone.nik.mixology.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DrinkWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetDataProvider(this, intent);
    }

    public class WidgetDataProvider implements RemoteViewsFactory {

        private final Context context;
        private List<Cocktail> drinks = new ArrayList<>();

        WidgetDataProvider(Context context, Intent intent) {
            this.context = context;
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            final long identityToken = Binder.clearCallingIdentity();
            drinks = ((MyApplication) getApplication()).getApplicationComponent()
                    .drinkRepository()
                    .getSavedSync();
            Binder.restoreCallingIdentity(identityToken);
        }

        @Override
        public void onDestroy() {
            drinks = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return drinks.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position == AdapterView.INVALID_POSITION || position >= drinks.size()) {
                return null;
            }

            Cocktail cocktail = drinks.get(position);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_item_list);
            remoteViews.setTextViewText(R.id.list_widget_text, cocktail.getmDrinkName());

            String thumbUrl = cocktail.getmDrinkThumb();
            if (thumbUrl == null || thumbUrl.equals("null") || thumbUrl.isEmpty()) {
                Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.empty_glass);
                remoteViews.setImageViewBitmap(R.id.list_widget_icon, icon);
            } else {
                try {
                    Bitmap bitmap = Picasso.get().load(thumbUrl).get();
                    remoteViews.setImageViewBitmap(R.id.list_widget_icon, bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Intent fillInIntent = new Intent();
            fillInIntent.putExtra(getString(R.string.intent_details_intent_cocktail), cocktail);
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
