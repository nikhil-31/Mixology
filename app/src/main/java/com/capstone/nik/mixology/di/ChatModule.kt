package com.capstone.nik.mixology.di

import com.capstone.nik.mixology.repository.ChatRepository
import com.capstone.nik.mixology.repository.NetworkChatRepository
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
}
