package com.enbridge.electronicservices.data.repository

/**
 * @author Sathya Narayanan
 */

import com.enbridge.electronicservices.data.api.ElectronicServicesApi
import com.enbridge.electronicservices.data.dto.LoginRequestDto
import com.enbridge.electronicservices.data.dto.LoginResponseDto
import com.enbridge.electronicservices.domain.entity.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for AuthRepositoryImpl
 *
 * Tests authentication repository operations ensuring proper handling of:
 * - Successful login with valid credentials
 * - Failed login attempts
 * - Network errors
 * - DTO to domain entity mapping
 */
class AuthRepositoryImplTest {

    private lateinit var api: ElectronicServicesApi
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setup() {
        api = mockk()
        repository = AuthRepositoryImpl(api)
    }

    @Test
    fun `login should return success with user when API call succeeds`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val request = LoginRequestDto(username, password)
        val responseDto = LoginResponseDto(
            token = "auth_token_123",
            userId = "user_456",
            username = username,
            expires = System.currentTimeMillis() + 3600000
        )
        coEvery { api.login(request) } returns Result.success(responseDto)

        // When
        val result = repository.login(username, password)

        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        val user = result.getOrNull()
        assertEquals("user_456", user?.id)
        assertEquals(username, user?.username)
        assertEquals("auth_token_123", user?.token)

        coVerify(exactly = 1) {
            api.login(request)
        }
    }

    @Test
    fun `login should return failure when API call fails with invalid credentials`() = runTest {
        // Given
        val username = "invaliduser"
        val password = "wrongpass"
        val request = LoginRequestDto(username, password)
        val exception = Exception("Invalid credentials")
        coEvery { api.login(request) } returns Result.failure(exception)

        // When
        val result = repository.login(username, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Invalid credentials", result.exceptionOrNull()?.message)

        coVerify(exactly = 1) {
            api.login(request)
        }
    }

    @Test
    fun `login should return failure when network error occurs`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val request = LoginRequestDto(username, password)
        val exception = Exception("Network connection failed")
        coEvery { api.login(request) } throws exception

        // When
        val result = repository.login(username, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network connection failed", result.exceptionOrNull()?.message)

        coVerify(exactly = 1) {
            api.login(request)
        }
    }

    @Test
    fun `login should return failure when API throws exception`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val request = LoginRequestDto(username, password)
        val exception = RuntimeException("Server error")
        coEvery { api.login(request) } throws exception

        // When
        val result = repository.login(username, password)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
        assertEquals("Server error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login should correctly map DTO to domain entity`() = runTest {
        // Given
        val username = "mapper_test"
        val password = "test123"
        val request = LoginRequestDto(username, password)
        val responseDto = LoginResponseDto(
            token = "mapped_token",
            userId = "mapped_id",
            username = "mapper_test",
            expires = 1234567890L
        )
        coEvery { api.login(request) } returns Result.success(responseDto)

        // When
        val result = repository.login(username, password)

        // Then
        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals(responseDto.userId, user?.id)
        assertEquals(responseDto.username, user?.username)
        assertEquals(responseDto.token, user?.token)
    }

    @Test
    fun `login should handle empty username and password`() = runTest {
        // Given
        val username = ""
        val password = ""
        val request = LoginRequestDto(username, password)
        val exception = Exception("Username and password cannot be empty")
        coEvery { api.login(request) } returns Result.failure(exception)

        // When
        val result = repository.login(username, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Username and password cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login should handle special characters in credentials`() = runTest {
        // Given
        val username = "user@example.com"
        val password = "P@ssw0rd!#$"
        val request = LoginRequestDto(username, password)
        val responseDto = LoginResponseDto(
            token = "special_token",
            userId = "special_id",
            username = username,
            expires = System.currentTimeMillis()
        )
        coEvery { api.login(request) } returns Result.success(responseDto)

        // When
        val result = repository.login(username, password)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(username, result.getOrNull()?.username)
    }
}
