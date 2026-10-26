package com.capstone.nik.mixology.di

import android.content.Context
import com.capstone.nik.mixology.BuildConfig
import com.capstone.nik.mixology.Network.AuthService
import com.capstone.nik.mixology.Network.ChatService
import com.capstone.nik.mixology.Network.CocktailService
import com.capstone.nik.mixology.Network.CocktailURLs
import com.capstone.nik.mixology.data.SessionStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CACHE_SIZE_BYTES = 10L * 1024 * 1024
    private const val TIMEOUT_SECONDS = 15L
    private const val CHAT_READ_TIMEOUT_MINUTES = 3L

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .cache(Cache(File(context.cacheDir, "http_cache"), CACHE_SIZE_BYTES))
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(loggingInterceptor())
        }
        return builder.build()
    }

    @Provides
    @Singleton
    @AuthenticatedClient
    fun provideAuthenticatedClient(sessionStore: SessionStore): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(CHAT_READ_TIMEOUT_MINUTES, TimeUnit.MINUTES)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(bearerInterceptor(sessionStore))
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(loggingInterceptor())
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideCocktailService(client: OkHttpClient): CocktailService {
        return retrofit(client).create(CocktailService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthService(client: OkHttpClient): AuthService {
        return retrofit(client).create(AuthService::class.java)
    }

    @Provides
    @Singleton
    fun provideChatService(@AuthenticatedClient client: OkHttpClient): ChatService {
        return retrofit(client).create(ChatService::class.java)
    }

    private fun retrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(CocktailURLs.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun bearerInterceptor(sessionStore: SessionStore): Interceptor {
        return Interceptor { chain ->
            val token = sessionStore.current()?.idToken
            val request = if (token.isNullOrBlank()) {
                chain.request()
            } else {
                chain.request().newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            }
            chain.proceed(request)
        }
    }

    private fun loggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
    }
}
