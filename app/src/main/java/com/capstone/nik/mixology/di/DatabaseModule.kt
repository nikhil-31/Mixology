package com.capstone.nik.mixology.di

import android.content.Context
import com.capstone.nik.mixology.data.BarDao
import com.capstone.nik.mixology.data.DrinkDao
import com.capstone.nik.mixology.data.DrinkNoteDao
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.data.ShoppingDao
import com.capstone.nik.mixology.data.ShoppingTripDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MixologyDatabase {
        return MixologyDatabase.create(context)
    }

    @Provides
    fun provideDrinkDao(database: MixologyDatabase): DrinkDao = database.drinkDao()

    @Provides
    fun provideShoppingDao(database: MixologyDatabase): ShoppingDao = database.shoppingDao()

    @Provides
    fun provideBarDao(database: MixologyDatabase): BarDao = database.barDao()

    @Provides
    fun provideShoppingTripDao(database: MixologyDatabase): ShoppingTripDao = database.shoppingTripDao()

    @Provides
    fun provideDrinkNoteDao(database: MixologyDatabase): DrinkNoteDao = database.drinkNoteDao()
}
