package com.enbridge.gdsgpscollection.data.repository

/**
 * @author Sathya Narayanan
 */

import android.content.Context
import com.arcgismaps.geometry.Envelope
import com.arcgismaps.geometry.SpatialReference
import com.enbridge.gdsgpscollection.data.local.dao.LocalEditDao
import com.enbridge.gdsgpscollection.data.local.preferences.PreferencesManager
import com.enbridge.gdsgpscollection.domain.config.FeatureServiceConfiguration
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
import com.enbridge.gdsgpscollection.util.network.NetworkMonitor
import com.enbridge.gdsgpscollection.util.storage.StorageUtil
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

/**
 * Unit tests for ManageESRepositoryImpl
 *
 * Updated for multi-service geodatabase architecture.
 *
 * Note: Tests requiring Android Context or ArcGIS native libraries are marked @Ignore.
 * These tests require the Android runtime and should be run as instrumented tests.
 */
class ManageESRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var localEditDao: LocalEditDao
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var storageUtil: StorageUtil
    private lateinit var configuration: FeatureServiceConfiguration
    private lateinit var repository: ManageESRepositoryImpl

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        localEditDao = mockk()
        preferencesManager = mockk()
        networkMonitor = mockk()
        storageUtil = mockk()
        configuration = mockk()

        repository = ManageESRepositoryImpl(
            context = context,
            localEditDao = localEditDao,
            preferencesManager = preferencesManager,
            networkMonitor = networkMonitor,
            storageUtil = storageUtil,
            configuration = configuration
        )
    }

    @Ignore("Requires Android Context and file system - run as instrumented test instead")
    @Test
    fun `getChangedData should return empty list by default`() = runTest {
        // When
        val result = repository.getChangedData()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Ignore("Requires Android Context - run as instrumented test instead")
    @Test
    fun `deleteJobCards should return zero by default`() = runTest {
        // When
        val result = repository.deleteJobCards()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())
    }

    @Ignore("Requires Android Context - run as instrumented test instead")
    @Test
    fun `getSelectedDistance should return saved distance from preferences`() = runTest {
        // Given
        every { preferencesManager.getESDataDistance() } returns 500

        // When
        val result = repository.getSelectedDistance()

        // Then
        assertEquals(ESDataDistance.FIVE_HUNDRED_METERS, result)

        verify(exactly = 1) {
            preferencesManager.getESDataDistance()
        }
    }

    @Ignore("Requires Android Context - run as instrumented test instead")
    @Test
    fun `getSelectedDistance should return default when invalid value stored`() = runTest {
        // Given
        every { preferencesManager.getESDataDistance() } returns 999

        // When
        val result = repository.getSelectedDistance()

        // Then
        assertEquals(ESDataDistance.HUNDRED_METERS, result)
    }

    @Ignore("Requires Android Context - run as instrumented test instead")
    @Test
    fun `saveSelectedDistance should save to preferences`() = runTest {
        // Given
        every { preferencesManager.saveESDataDistance(any()) } just runs

        // When
        repository.saveSelectedDistance(ESDataDistance.FIFTY_METERS)

        // Then
        verify(exactly = 1) {
            preferencesManager.saveESDataDistance(50)
        }
    }

    @Ignore("Requires Android Context - run as instrumented test instead")
    @Test
    fun `saveSelectedDistance should save all distance values correctly`() = runTest {
        // Given
        every { preferencesManager.saveESDataDistance(any()) } just runs

        // When & Then
        repository.saveSelectedDistance(ESDataDistance.FIFTY_METERS)
        verify { preferencesManager.saveESDataDistance(50) }

        repository.saveSelectedDistance(ESDataDistance.HUNDRED_METERS)
        verify { preferencesManager.saveESDataDistance(100) }

        repository.saveSelectedDistance(ESDataDistance.FIVE_HUNDRED_METERS)
        verify { preferencesManager.saveESDataDistance(500) }
    }

    @Ignore("Requires Android Context and file system - run as instrumented test instead")
    @Test
    fun `loadExistingGeodatabase should return null when no file exists`() = runTest {
        // Given
        every { context.filesDir } returns mockk {
            every { absolutePath } returns "/mock/path"
        }

        // When
        val result = repository.loadExistingGeodatabase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrNull())
    }
}
