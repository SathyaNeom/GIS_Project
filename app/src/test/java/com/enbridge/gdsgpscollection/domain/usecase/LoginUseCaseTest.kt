package com.enbridge.gdsgpscollection.domain.usecase

/**
 * @author Sathya Narayanan
 */

import com.enbridge.gdsgpscollection.domain.entity.User
import com.enbridge.gdsgpscollection.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for LoginUseCase
 *
 * Tests the authentication business logic ensuring proper handling of:
 * - Successful login with valid credentials
 * - Failed login with invalid credentials
 * - Network errors during authentication
 * - Repository interaction verification
 */
class LoginUseCaseTest {

    private lateinit var repository: AuthRepository
    private lateinit var useCase: LoginUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = LoginUseCase(repository)
    }

    @Test
    fun `invoke should return success with user when login succeeds`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val expectedUser = User(
            id = "123",
            username = username,
            token = "auth_token_xyz"
        )
        coEvery { repository.login(username, password) } returns Result.success(expectedUser)

        // When
        val result = useCase(username, password)

        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertEquals(expectedUser.id, result.getOrNull()?.id)
        assertEquals(expectedUser.username, result.getOrNull()?.username)
        assertEquals(expectedUser.token, result.getOrNull()?.token)

        coVerify(exactly = 1) {
            repository.login(username, password)
        }
    }

    @Test
    fun `invoke should return failure when login fails with invalid credentials`() = runTest {
        // Given
        val username = "invaliduser"
        val password = "wrongpass"
        val exception = Exception("Invalid credentials")
        coEvery { repository.login(username, password) } returns Result.failure(exception)

        // When
        val result = useCase(username, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Invalid credentials", result.exceptionOrNull()?.message)

        coVerify(exactly = 1) {
            repository.login(username, password)
        }
    }

    @Test
    fun `invoke should return failure when network error occurs`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val exception = Exception("Network connection failed")
        coEvery { repository.login(username, password) } returns Result.failure(exception)

        // When
        val result = useCase(username, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network connection failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should return failure when server error occurs`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val exception = Exception("Server error: 500")
        coEvery { repository.login(username, password) } returns Result.failure(exception)

        // When
        val result = useCase(username, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Server error: 500", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should handle empty username`() = runTest {
        // Given
        val username = ""
        val password = "testpass"
        val exception = Exception("Username cannot be empty")
        coEvery { repository.login(username, password) } returns Result.failure(exception)

        // When
        val result = useCase(username, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Username cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should handle empty password`() = runTest {
        // Given
        val username = "testuser"
        val password = ""
        val exception = Exception("Password cannot be empty")
        coEvery { repository.login(username, password) } returns Result.failure(exception)

        // When
        val result = useCase(username, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Password cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should propagate repository result correctly`() = runTest {
        // Given
        val username = "admin"
        val password = "admin123"
        val user = User("456", "admin", "admin_token")
        coEvery { repository.login(username, password) } returns Result.success(user)

        // When
        val result = useCase(username, password)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())

        coVerify(exactly = 1) {
            repository.login(username, password)
        }
    }
}
