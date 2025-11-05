package com.enbridge.gdsgpscollection.domain.repository

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gdsgpscollection.domain.entity.User

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
}
