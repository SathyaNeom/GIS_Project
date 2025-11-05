package com.enbridge.gpsdeviceproj.data.repository

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gpsdeviceproj.data.api.ElectronicServicesApi
import com.enbridge.gpsdeviceproj.data.dto.LoginRequestDto
import com.enbridge.gpsdeviceproj.data.mapper.toDomain
import com.enbridge.gpsdeviceproj.domain.entity.User
import com.enbridge.gpsdeviceproj.domain.repository.AuthRepository
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
        return try {
            val request = LoginRequestDto(username, password)
            api.login(request).map { it.toDomain() }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
