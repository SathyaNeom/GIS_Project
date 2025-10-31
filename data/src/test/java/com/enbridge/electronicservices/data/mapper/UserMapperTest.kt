package com.enbridge.electronicservices.data.mapper

/**
 * @author Sathya Narayanan
 */

import com.enbridge.electronicservices.data.dto.LoginResponseDto
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for UserMapper
 *
 * Tests DTO to domain entity mapping for User ensuring:
 * - Correct field mapping
 * - Preservation of all data
 * - Handling of various data types
 */
class UserMapperTest {

    @Test
    fun `toDomain should correctly map all fields from LoginResponseDto to User`() {
        // Given
        val dto = LoginResponseDto(
            token = "auth_token_123",
            userId = "user_456",
            username = "testuser",
            expires = 1234567890L
        )

        // When
        val user = dto.toDomain()

        // Then
        assertEquals("user_456", user.id)
        assertEquals("testuser", user.username)
        assertEquals("auth_token_123", user.token)
    }

    @Test
    fun `toDomain should handle empty strings`() {
        // Given
        val dto = LoginResponseDto(
            token = "",
            userId = "",
            username = "",
            expires = 0L
        )

        // When
        val user = dto.toDomain()

        // Then
        assertEquals("", user.id)
        assertEquals("", user.username)
        assertEquals("", user.token)
    }

    @Test
    fun `toDomain should handle special characters in fields`() {
        // Given
        val dto = LoginResponseDto(
            token = "bearer_eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9",
            userId = "user-123-abc",
            username = "user@example.com",
            expires = System.currentTimeMillis()
        )

        // When
        val user = dto.toDomain()

        // Then
        assertEquals("user-123-abc", user.id)
        assertEquals("user@example.com", user.username)
        assertEquals("bearer_eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9", user.token)
    }

    @Test
    fun `toDomain should handle long user IDs`() {
        // Given
        val dto = LoginResponseDto(
            token = "token",
            userId = "very_long_user_id_1234567890_abcdefghijklmnopqrstuvwxyz",
            username = "user",
            expires = 0L
        )

        // When
        val user = dto.toDomain()

        // Then
        assertEquals("very_long_user_id_1234567890_abcdefghijklmnopqrstuvwxyz", user.id)
    }

    @Test
    fun `toDomain should correctly map numeric and alphanumeric tokens`() {
        // Given
        val dto = LoginResponseDto(
            token = "1234567890abcdef",
            userId = "user_001",
            username = "numeric_user",
            expires = 9999999999L
        )

        // When
        val user = dto.toDomain()

        // Then
        assertEquals("user_001", user.id)
        assertEquals("numeric_user", user.username)
        assertEquals("1234567890abcdef", user.token)
    }
}
