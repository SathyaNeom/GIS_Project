package com.enbridge.gpsdeviceproj.domain.entity

/**
 * @author Sathya Narayanan
 */
data class LocalEdit(
    val id: Long = 0,
    val entityId: String,
    val entityType: String,
    val isSynced: Boolean = false
)
