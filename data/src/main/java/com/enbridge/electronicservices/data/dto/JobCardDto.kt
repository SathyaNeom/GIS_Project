package com.enbridge.electronicservices.data.dto

/**
 * @author Sathya Narayanan
 */
import kotlinx.serialization.Serializable

@Serializable
data class JobCardDto(
    val id: String,
    val address: String,
    val municipality: String,
    val status: String,
    val serviceType: String,
    val connectionType: String
)
