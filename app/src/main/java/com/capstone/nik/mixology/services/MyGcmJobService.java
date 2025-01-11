package com.capstone.nik.mixology.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.scheduling.GcmJobSchedulerService;
import com.capstone.nik.mixology.Network.MyApplication;

import javax.inject.Inject;


/**
 * Created by nikhil on 30-08-2017.
 */

public class MyGcmJobService extends GcmJobSchedulerService {

    @Inject
    JobManager mJobManager;

    @NonNull
    @Override
    protected JobManager getJobManager() {
        ((MyApplication) getApplication()).getApplicationComponent().inject(this);
        return mJobManager;
    }
}
