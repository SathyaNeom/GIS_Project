package com.enbridge.electronicservices.domain.usecase

/**
 * @author Sathya Narayanan
 */
import com.enbridge.electronicservices.domain.repository.ManageESRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for PostESDataUseCase
 */
class PostESDataUseCaseTest {

    private lateinit var repository: ManageESRepository
    private lateinit var useCase: PostESDataUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = PostESDataUseCase(repository)
    }

    @Test
    fun `invoke should return success when repository posts data successfully`() = runTest {
        // Given
        coEvery { repository.postESData() } returns Result.success(true)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())

        coVerify(exactly = 1) {
            repository.postESData()
        }
    }

    @Test
    fun `invoke should return failure when repository fails`() = runTest {
        // Given
        val exception = Exception("Network error")
        coEvery { repository.postESData() } returns Result.failure(exception)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)

        coVerify(exactly = 1) {
            repository.postESData()
        }
    }
}
