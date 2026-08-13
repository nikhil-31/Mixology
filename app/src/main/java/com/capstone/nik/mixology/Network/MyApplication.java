package com.capstone.nik.mixology.Network;

import android.app.Application;

import com.capstone.nik.mixology.BuildConfig;
import com.capstone.nik.mixology.di.applicationComponent.ApplicationComponent;
import com.capstone.nik.mixology.di.applicationComponent.DaggerApplicationComponent;
import com.capstone.nik.mixology.di.module.ApplicationModule;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class MyApplication extends Application {
    private static MyApplication sInstance;

    private ApplicationComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.setCrashlyticsCollectionEnabled(true);
        crashlytics.setCustomKey("version_name", BuildConfig.VERSION_NAME);
        crashlytics.setCustomKey("version_code", BuildConfig.VERSION_CODE);

        component = DaggerApplicationComponent
                .builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    public ApplicationComponent getApplicationComponent() {
        return component;
    }
}
