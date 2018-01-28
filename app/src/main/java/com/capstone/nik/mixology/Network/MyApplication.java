package com.capstone.nik.mixology.Network;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.log.CustomLogger;
import com.birbit.android.jobqueue.scheduling.FrameworkJobSchedulerService;
import com.birbit.android.jobqueue.scheduling.GcmJobSchedulerService;
import com.capstone.nik.mixology.di.applicationComponent.ApplicationComponent;
import com.capstone.nik.mixology.di.applicationComponent.DaggerApplicationComponent;
import com.capstone.nik.mixology.di.module.ApplicationModule;
import com.capstone.nik.mixology.services.MyGcmJobService;
import com.capstone.nik.mixology.services.MyJobService;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.BuildConfig;
import com.facebook.stetho.Stetho;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.squareup.leakcanary.LeakCanary;

import io.fabric.sdk.android.Fabric;

/**
 * Created by nik on 12/7/2016.
 */

public class MyApplication extends Application {
  private static MyApplication sInstance;

  private ApplicationComponent component;
//  private JobManager mJobManager;

  @Override
  public void onCreate() {
    super.onCreate(); 
    sInstance = this;
    Fabric.with(this, new Crashlytics());

    component = DaggerApplicationComponent
        .builder()
        .applicationModule(new ApplicationModule(this))
        .build();

//    if (BuildConfig.DEBUG) {
    Stetho.initializeWithDefaults(this);
//    }

    if (LeakCanary.isInAnalyzerProcess(this)) {
      return;
    }
    LeakCanary.install(this);
  }

  public static MyApplication getInstance() {
    return sInstance;
  }

  public ApplicationComponent getApplicationComponent() {
    return component;
  }

// Initializes the job Manager only once
//  public synchronized JobManager getJobManager() {
//    if (mJobManager == null) {
//      configureJobManager();
//    }
//    return mJobManager;
//  }
//
//  private void configureJobManager() {
//    Configuration.Builder builder = new Configuration.Builder(this)
//        .customLogger(new CustomLogger() {
//          private static final String TAG = "JOBS";
//
//          @Override
//          public boolean isDebugEnabled() {
//            return true;
//          }
//
//          @Override
//          public void d(String text, Object... args) {
//            Log.d(TAG, String.format(text, args));
//          }
//
//          @Override
//          public void e(Throwable t, String text, Object... args) {
//            Log.e(TAG, String.format(text, args), t);
//          }
//
//          @Override
//          public void e(String text, Object... args) {
//            Log.e(TAG, String.format(text, args));
//          }
//
//          @Override
//          public void v(String text, Object... args) {
//
//          }
//        })
//        .minConsumerCount(1)        // always keep at least one consumer alive
//        .maxConsumerCount(3)        // up to 3 consumers at a time
//        .loadFactor(3)              // 3 jobs per consumer
//        .consumerKeepAlive(120);    // wait 2 minute
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//      builder.scheduler(FrameworkJobSchedulerService.createSchedulerFor(this,
//          MyJobService.class), false);
//    } else {
//      int enableGcm = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
//      if (enableGcm == ConnectionResult.SUCCESS) {
//        builder.scheduler(GcmJobSchedulerService.createSchedulerFor(this,
//            MyGcmJobService.class), false);
//      }
//    }
//    mJobManager = new JobManager(builder.build());
//  }

}
