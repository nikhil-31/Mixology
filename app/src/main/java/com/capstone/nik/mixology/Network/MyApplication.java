package com.capstone.nik.mixology.Network;

import android.app.Application;
import android.content.Context;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

/**
 * Created by nik on 12/7/2016.
 */

public class MyApplication extends Application {
  private static MyApplication sInstance;

  @Override
  public void onCreate() {
    super.onCreate();
    sInstance = this;
    Fabric.with(this, new Crashlytics());
  }

  private static MyApplication getsInstance() {
    return sInstance;
  }

  public static Context getAppContext() {
    return sInstance.getApplicationContext();
  }
}
