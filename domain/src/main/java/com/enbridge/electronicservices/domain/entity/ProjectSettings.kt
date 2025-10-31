package com.enbridge.electronicservices.domain.entity

/**
 * @author Sathya Narayanan
 */
data class ProjectSettings(
    val contractor: String,
    val crewId: String,
    val supervisor: String,
    val fitterName: String,
    val welderName: String,
    val workOrderTypes: List<String>,
    val defaultDownloadDistance: Int
)
