package com.enbridge.gdsgpscollection.data.repository

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gdsgpscollection.data.api.ElectronicServicesApi
import com.enbridge.gdsgpscollection.data.dto.LoginRequestDto
import com.enbridge.gdsgpscollection.data.mapper.toDomain
import com.enbridge.gdsgpscollection.domain.entity.User
import com.enbridge.gdsgpscollection.domain.repository.AuthRepository
import com.enbridge.gdsgpscollection.util.Logger
import javax.inject.Inject

/**
 * Implementation of AuthRepository that handles authentication operations.
 *
 * This repository acts as the data layer for authentication, coordinating
 * between the API service and the domain layer. It handles network requests,
 * error handling, and data transformation.
 *
 * @property api The API service for making authentication requests
 */
class AuthRepositoryImpl @Inject constructor(
    private val api: ElectronicServicesApi
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
    }

    /**
     * Authenticates a user with the provided credentials.
     *
     * This method performs the following operations:
     * - Validates and sends login credentials to the server
     * - Transforms the response DTO to a domain entity
     * - Returns a Result object containing either the authenticated user or an error
     *
     * @param username The user's username for authentication
     * @param password The user's password for authentication
     * @return Result<User> containing the authenticated user on success, or an exception on failure
     */
    override suspend fun login(username: String, password: String): Result<User> {
        Logger.i(TAG, "Attempting login for username: $username")
        return try {
            val request = LoginRequestDto(username, password)
            Logger.d(TAG, "Sending login request to API")

            val result = api.login(request).map { it.toDomain() }

            result.onSuccess { user ->
                Logger.i(TAG, "Login successful - User ID: ${user.id}, Username: ${user.username}")
            }.onFailure { error ->
                Logger.e(TAG, "Login failed for username: $username", error)
            }

            result
        } catch (e: Exception) {
            Logger.e(TAG, "Exception during login for username: $username", e)
            Result.failure(e)
        }
    }
}
