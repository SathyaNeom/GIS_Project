package com.enbridge.gdsgpscollection.data.dto

/**
 * @author Sathya Narayanan
 */
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String
)
