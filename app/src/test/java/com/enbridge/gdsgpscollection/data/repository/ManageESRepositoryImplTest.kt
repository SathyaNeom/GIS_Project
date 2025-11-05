package com.enbridge.gdsgpscollection.data.repository

/**
 * @author Sathya Narayanan
 */

import com.enbridge.gdsgpscollection.data.local.dao.LocalEditDao
import com.enbridge.gdsgpscollection.data.local.preferences.PreferencesManager
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
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
import org.junit.Test

/**
 * Unit tests for ManageESRepositoryImpl
 */
class ManageESRepositoryImplTest {

    private lateinit var localEditDao: LocalEditDao
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var repository: ManageESRepositoryImpl

    @Before
    fun setup() {
        localEditDao = mockk()
        preferencesManager = mockk()
        repository = ManageESRepositoryImpl(localEditDao, preferencesManager)
    }

    @Test
    fun `downloadESData should emit progress updates`() = runTest {
        // When
        val progressFlow = repository.downloadESData(
            ESDataDistance.HUNDRED_METERS,
            43.6532,
            -79.3832
        )
        val progressList = progressFlow.toList()

        // Then
        assertTrue(progressList.isNotEmpty())
        assertEquals(0.0f, progressList.first().progress)
        assertEquals(1.0f, progressList.last().progress)
        assertTrue(progressList.last().isComplete)
    }

    @Test
    fun `postESData should return success`() = runTest {
        // When
        val result = repository.postESData()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
    }

    @Test
    fun `getChangedData should return empty list by default`() = runTest {
        // When
        val result = repository.getChangedData()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun `deleteJobCards should return zero by default`() = runTest {
        // When
        val result = repository.deleteJobCards()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())
    }

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

    @Test
    fun `getSelectedDistance should return default when invalid value stored`() = runTest {
        // Given
        every { preferencesManager.getESDataDistance() } returns 999

        // When
        val result = repository.getSelectedDistance()

        // Then
        assertEquals(ESDataDistance.HUNDRED_METERS, result)
    }

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
}
