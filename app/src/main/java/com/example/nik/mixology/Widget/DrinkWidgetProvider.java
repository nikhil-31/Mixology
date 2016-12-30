package com.example.nik.mixology.Widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.example.nik.mixology.R;

/**
 * Created by nik on 12/30/2016.
 */

public class DrinkWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);


        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);


            appWidgetManager.updateAppWidget(appWidgetId, views);
        }


    }

}
