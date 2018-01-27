package com.capstone.nik.mixology.di.module;

import android.app.Application;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

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
  public Context provideApplication() {
    return application;
  }

  @Provides
  @Singleton
  public RequestQueue provideRequestQueue() {
    return Volley.newRequestQueue(application);
  }

}
