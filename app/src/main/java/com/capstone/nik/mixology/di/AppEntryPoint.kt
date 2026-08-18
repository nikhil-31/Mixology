package com.capstone.nik.mixology.di

import com.capstone.nik.mixology.Network.NetworkMonitor
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppEntryPoint {
    fun networkMonitor(): NetworkMonitor
}
