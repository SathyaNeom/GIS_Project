package com.enbridge.electronicservices.data.dto

/**
 * @author Sathya Narayanan
 */
import kotlinx.serialization.Serializable

@Serializable
data class ProjectSettingsDto(
    val contractor: String,
    val crewId: String,
    val supervisor: String,
    val fitterName: String,
    val welderName: String,
    val workOrderTypes: List<String>,
    val defaultDownloadDistance: Int
)
