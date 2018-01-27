package com.capstone.nik.mixology.di.module;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.log.CustomLogger;
import com.birbit.android.jobqueue.scheduling.FrameworkJobSchedulerService;
import com.birbit.android.jobqueue.scheduling.GcmJobSchedulerService;
import com.capstone.nik.mixology.services.MyGcmJobService;
import com.capstone.nik.mixology.services.MyJobService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by nik on 1/27/2018.
 */
@Module
public class ApplicationModule {

  private Application application;

  public ApplicationModule(Application application) {
    this.application = application;
  }

  @Provides
  @Singleton
  Context provideApplication() {
    return application;
  }

  @Provides
  @Singleton
  RequestQueue provideRequestQueue() {
    return Volley.newRequestQueue(application);
  }

  @Provides
  @Singleton
  JobManager provideJobManager() {
    Configuration.Builder builder = new Configuration.Builder(application)
        .customLogger(new CustomLogger() {
          private static final String TAG = "JOBS";

          @Override
          public boolean isDebugEnabled() {
            return true;
          }

          @Override
          public void d(String text, Object... args) {
            Log.d(TAG, String.format(text, args));
          }

          @Override
          public void e(Throwable t, String text, Object... args) {
            Log.e(TAG, String.format(text, args), t);
          }

          @Override
          public void e(String text, Object... args) {
            Log.e(TAG, String.format(text, args));
          }

          @Override
          public void v(String text, Object... args) {

          }
        })
        .minConsumerCount(1)        // always keep at least one consumer alive
        .maxConsumerCount(3)        // up to 3 consumers at a time
        .loadFactor(3)              // 3 jobs per consumer
        .consumerKeepAlive(120);    // wait 2 minute
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      builder.scheduler(FrameworkJobSchedulerService.createSchedulerFor(application,
          MyJobService.class), false);
    } else {
      int enableGcm = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(application);
      if (enableGcm == ConnectionResult.SUCCESS) {
        builder.scheduler(GcmJobSchedulerService.createSchedulerFor(application,
            MyGcmJobService.class), false);
      }
    }
    return new JobManager(builder.build());
  }

}
