package com.enbridge.gdsgpscollection.domain.usecase

/**
 * @author Sathya Narayanan
 */

import com.enbridge.gdsgpscollection.domain.entity.JobCard
import com.enbridge.gdsgpscollection.domain.entity.JobStatus
import com.enbridge.gdsgpscollection.domain.repository.ManageESRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetChangedDataUseCase
 */
class GetChangedDataUseCaseTest {

    private lateinit var repository: ManageESRepository
    private lateinit var useCase: GetChangedDataUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetChangedDataUseCase(repository)
    }

    @Test
    fun `invoke should return empty list when no changed data exists`() = runTest {
        // Given
        coEvery { repository.getChangedData() } returns Result.success(emptyList())

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)

        coVerify(exactly = 1) {
            repository.getChangedData()
        }
    }

    @Test
    fun `invoke should return list of changed job cards`() = runTest {
        // Given
        val jobCards = listOf(
            JobCard(
                id = "JC001",
                address = "123 Main St",
                municipality = "Toronto",
                status = JobStatus.IN_PROGRESS,
                serviceType = "Gas",
                connectionType = "Residential"
            ),
            JobCard(
                id = "JC002",
                address = "456 Oak Ave",
                municipality = "Mississauga",
                status = JobStatus.ASSIGNED,
                serviceType = "Gas",
                connectionType = "Commercial"
            )
        )
        coEvery { repository.getChangedData() } returns Result.success(jobCards)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("123 Main St", result.getOrNull()?.get(0)?.address)

        coVerify(exactly = 1) {
            repository.getChangedData()
        }
    }

    @Test
    fun `invoke should return failure when repository fails`() = runTest {
        // Given
        val exception = Exception("Database error")
        coEvery { repository.getChangedData() } returns Result.failure(exception)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Database error", result.exceptionOrNull()?.message)

        coVerify(exactly = 1) {
            repository.getChangedData()
        }
    }
}
