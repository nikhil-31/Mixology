package com.capstone.nik.mixology.di

import android.content.Context
import com.capstone.nik.mixology.play.GooglePlayReviewClient
import com.capstone.nik.mixology.play.PlayReviewClient
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayProvidesModule {
    @Provides
    @Singleton
    fun provideAppUpdateManager(@ApplicationContext context: Context): AppUpdateManager {
        return AppUpdateManagerFactory.create(context)
    }

    @Provides
    @Singleton
    fun provideReviewManager(@ApplicationContext context: Context): ReviewManager {
        return ReviewManagerFactory.create(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PlayBindsModule {
    @Binds
    @Singleton
    abstract fun bindPlayReviewClient(impl: GooglePlayReviewClient): PlayReviewClient
}
