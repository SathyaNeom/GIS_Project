package com.enbridge.gpsdeviceproj.domain.usecase

/**
 * @author Sathya Narayanan
 */

import com.enbridge.gpsdeviceproj.domain.repository.ManageESRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for DeleteJobCardsUseCase
 */
class DeleteJobCardsUseCaseTest {

    private lateinit var repository: ManageESRepository
    private lateinit var useCase: DeleteJobCardsUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = DeleteJobCardsUseCase(repository)
    }

    @Test
    fun `invoke should return zero when no job cards exist`() = runTest {
        // Given
        coEvery { repository.deleteJobCards() } returns Result.success(0)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())

        coVerify(exactly = 1) {
            repository.deleteJobCards()
        }
    }

    @Test
    fun `invoke should return count of deleted job cards`() = runTest {
        // Given
        coEvery { repository.deleteJobCards() } returns Result.success(5)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(5, result.getOrNull())

        coVerify(exactly = 1) {
            repository.deleteJobCards()
        }
    }

    @Test
    fun `invoke should return failure when repository fails`() = runTest {
        // Given
        val exception = Exception("Delete operation failed")
        coEvery { repository.deleteJobCards() } returns Result.failure(exception)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Delete operation failed", result.exceptionOrNull()?.message)

        coVerify(exactly = 1) {
            repository.deleteJobCards()
        }
    }
}
