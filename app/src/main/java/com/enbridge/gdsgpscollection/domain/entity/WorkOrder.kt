package com.enbridge.gdsgpscollection.domain.entity

/**
 * @author Sathya Narayanan
 */

/**
 * Domain entity representing a Work Order
 *
 * @property id Unique identifier for the work order
 * @property workOrderNumber The work order number (e.g., "22910778")
 * @property address The address associated with the work order (e.g., "33 BRIAN DR NORTH YORK")
 * @property poleType The type of pole (e.g., "6 Foot Pole", "8 Foot Pole", "Handheld")
 * @property distance Distance from current location in meters (optional)
 * @property displayText The formatted display text for UI (e.g., "||33 BRIAN DR NORTH YORK||22910778")
 */
data class WorkOrder(
    val id: String,
    val workOrderNumber: String,
    val address: String,
    val poleType: String,
    val distance: Int? = null,
    val displayText: String
)
