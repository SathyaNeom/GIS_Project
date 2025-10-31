package com.enbridge.electronicservices.core.di

/**
 * @author Sathya Narayanan
 */
import com.enbridge.electronicservices.core.network.KtorClient
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
