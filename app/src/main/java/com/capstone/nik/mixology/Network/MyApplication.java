package com.capstone.nik.mixology.Network;

import android.app.Application;

import com.capstone.nik.mixology.di.applicationComponent.ApplicationComponent;
import com.capstone.nik.mixology.di.applicationComponent.DaggerApplicationComponent;
import com.capstone.nik.mixology.di.module.ApplicationModule;
//import com.crashlytics.android.Crashlytics;
//import com.squareup.leakcanary.LeakCanary;

//import io.fabric.sdk.android.Fabric;

/**
 * Created by nik on 12/7/2016.
 */

public class MyApplication extends Application {
    private static MyApplication sInstance;

    private ApplicationComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
//    Fabric.with(this, new Crashlytics());

        component = DaggerApplicationComponent
                .builder()
                .applicationModule(new ApplicationModule(this))
                .build();

//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            return;
//        }
//        LeakCanary.install(this);
    }

    public ApplicationComponent getApplicationComponent() {
        return component;
    }
}
