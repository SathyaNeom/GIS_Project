package com.enbridge.gpsdeviceproj.domain.repository

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gpsdeviceproj.domain.entity.User

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
}
