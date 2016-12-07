package com.example.nik.mixology.Network;

import android.app.Application;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by nik on 12/7/2016.
 */

public class MyApplication extends Application {
    private static MyApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    private static MyApplication getsInstance(){
        return sInstance;
    }

    public static Context getAppContext(){
        return sInstance.getApplicationContext();
    }
}
