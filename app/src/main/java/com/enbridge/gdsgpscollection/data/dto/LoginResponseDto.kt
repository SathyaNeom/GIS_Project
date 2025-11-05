package com.enbridge.gdsgpscollection.data.dto

/**
 * @author Sathya Narayanan
 */
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseDto(
    val token: String,
    val userId: String,
    val username: String,
    val expires: Long
)
