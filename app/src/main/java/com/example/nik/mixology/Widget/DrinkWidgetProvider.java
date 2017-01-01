package com.example.nik.mixology.Widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.nik.mixology.Activities.MainActivity;
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

//            Intent intent = new Intent(context, MainActivity.class);
//            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
//            views.setOnClickPendingIntent(R.id.textWidget, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }


    }

}
