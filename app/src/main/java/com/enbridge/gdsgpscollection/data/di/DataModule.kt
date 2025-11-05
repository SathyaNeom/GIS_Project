package com.enbridge.gdsgpscollection.data.di

/**
 * @author Sathya Narayanan
 */

import android.content.Context
import androidx.room.Room
import com.enbridge.gdsgpscollection.data.api.ElectronicServicesApi
import com.enbridge.gdsgpscollection.data.api.MockElectronicServicesApi
import com.enbridge.gdsgpscollection.data.local.AppDatabase
import com.enbridge.gdsgpscollection.data.local.dao.LocalEditDao
import com.enbridge.gdsgpscollection.data.repository.AuthRepositoryImpl
import com.enbridge.gdsgpscollection.data.repository.FeatureRepositoryImpl
import com.enbridge.gdsgpscollection.data.repository.JobCardEntryRepositoryImpl
import com.enbridge.gdsgpscollection.data.repository.ManageESRepositoryImpl
import com.enbridge.gdsgpscollection.data.repository.ProjectSettingsRepositoryImpl
import com.enbridge.gdsgpscollection.domain.repository.AuthRepository
import com.enbridge.gdsgpscollection.domain.repository.FeatureRepository
import com.enbridge.gdsgpscollection.domain.repository.JobCardEntryRepository
import com.enbridge.gdsgpscollection.domain.repository.ManageESRepository
import com.enbridge.gdsgpscollection.domain.repository.ProjectSettingsRepository
import dagger.Binds
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "electronic_services_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideLocalEditDao(database: AppDatabase): LocalEditDao {
        return database.localEditDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideElectronicServicesApi(
        @ApplicationContext context: Context
    ): ElectronicServicesApi {
        return MockElectronicServicesApi(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindFeatureRepository(
        featureRepositoryImpl: FeatureRepositoryImpl
    ): FeatureRepository

    @Binds
    @Singleton
    abstract fun bindManageESRepository(
        manageESRepositoryImpl: ManageESRepositoryImpl
    ): ManageESRepository

    @Binds
    @Singleton
    abstract fun bindJobCardEntryRepository(
        jobCardEntryRepositoryImpl: JobCardEntryRepositoryImpl
    ): JobCardEntryRepository

    @Binds
    @Singleton
    abstract fun bindProjectSettingsRepository(
        projectSettingsRepositoryImpl: ProjectSettingsRepositoryImpl
    ): ProjectSettingsRepository
}
