package com.enbridge.electronicservices.domain.entity

/**
 * @author Sathya Narayanan
 */
data class JobCard(
    val id: String,
    val address: String,
    val municipality: String,
    val status: JobStatus,
    val serviceType: String,
    val connectionType: String
)
