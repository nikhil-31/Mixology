package com.capstone.nik.mixology.di

import com.capstone.nik.mixology.repository.DrinkRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun drinkRepository(): DrinkRepository
}
