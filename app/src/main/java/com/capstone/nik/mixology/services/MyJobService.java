package com.capstone.nik.mixology.services;

import android.support.annotation.NonNull;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.scheduling.FrameworkJobSchedulerService;
import com.capstone.nik.mixology.Network.MyApplication;

import javax.inject.Inject;

/**
 * Created by nik on 1/28/2018.
 */

public class MyJobService extends FrameworkJobSchedulerService {

  @Inject
  JobManager mJobManager;

  @NonNull
  @Override
  protected JobManager getJobManager() {
    ((MyApplication) getApplication()).getApplicationComponent().inject(this);
    return mJobManager;
  }
}