package com.capstone.nik.mixology.di

import com.capstone.nik.mixology.data.AppleAuth
import com.capstone.nik.mixology.data.FirebaseAppleAuth
import com.capstone.nik.mixology.data.FirebaseGoogleAuth
import com.capstone.nik.mixology.data.GoogleAuth
import com.capstone.nik.mixology.repository.ChatRepository
import com.capstone.nik.mixology.repository.NetworkChatRepository
import com.capstone.nik.mixology.repository.NetworkRatingRepository
import com.capstone.nik.mixology.repository.RatingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatModule {
    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: NetworkChatRepository): ChatRepository

    @Binds
    @Singleton
    abstract fun bindRatingRepository(impl: NetworkRatingRepository): RatingRepository

    @Binds
    @Singleton
    abstract fun bindGoogleAuth(impl: FirebaseGoogleAuth): GoogleAuth

    @Binds
    @Singleton
    abstract fun bindAppleAuth(impl: FirebaseAppleAuth): AppleAuth
}
