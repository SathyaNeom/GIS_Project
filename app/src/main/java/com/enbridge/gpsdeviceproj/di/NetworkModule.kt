package com.enbridge.gpsdeviceproj.di

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gpsdeviceproj.network.KtorClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return KtorClient.create()
    }
}
