package com.enbridge.electronicservices.domain.repository

/**
 * @author Sathya Narayanan
 */
import com.enbridge.electronicservices.domain.entity.User

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
}
