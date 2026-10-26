package com.capstone.nik.mixology.di

import com.capstone.nik.mixology.billing.PlayStoreBilling
import com.capstone.nik.mixology.billing.StoreBilling
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BillingModule {
    @Binds
    @Singleton
    abstract fun bindStoreBilling(impl: PlayStoreBilling): StoreBilling
}
