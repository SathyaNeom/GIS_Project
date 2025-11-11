package com.enbridge.gdsgpscollection.di

import com.enbridge.gdsgpscollection.domain.facade.ManageESFacade
import com.enbridge.gdsgpscollection.domain.facade.ManageESFacadeImpl
import com.enbridge.gdsgpscollection.domain.facade.ProjectSettingsFacade
import com.enbridge.gdsgpscollection.domain.facade.ProjectSettingsFacadeImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for providing facade implementations.
 *
 * Facades group related use cases together to simplify ViewModel dependencies
 * and improve cohesion following the Facade pattern from SOLID principles.
 *
 * @author Sathya Narayanan
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FacadeModule {

    /**
     * Binds the ManageESFacade implementation.
     *
     * This facade groups all ES data management operations (download, post,
     * changed data, delete, distance preferences).
     */
    @Binds
    abstract fun bindManageESFacade(
        impl: ManageESFacadeImpl
    ): ManageESFacade

    /**
     * Binds the ProjectSettingsFacade implementation.
     *
     * This facade groups all project settings operations (work orders,
     * project settings retrieval and saving).
     */
    @Binds
    abstract fun bindProjectSettingsFacade(
        impl: ProjectSettingsFacadeImpl
    ): ProjectSettingsFacade
}
