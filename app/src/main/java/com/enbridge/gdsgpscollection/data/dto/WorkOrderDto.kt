package com.enbridge.gdsgpscollection.data.dto

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for Work Order API responses
 */
@Serializable
data class WorkOrderDto(
    val id: String,
    val workOrderNumber: String,
    val address: String,
    val poleType: String,
    val distance: Int? = null,
    val displayText: String
)
