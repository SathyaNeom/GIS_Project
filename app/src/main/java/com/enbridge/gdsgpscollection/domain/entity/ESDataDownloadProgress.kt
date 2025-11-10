package com.enbridge.gdsgpscollection.domain.entity

import com.arcgismaps.data.Geodatabase

/**
 * @author Sathya Narayanan
 * Represents the progress of ES data download
 * Used to track the status of geodatabase sync operations
 */
data class ESDataDownloadProgress(
    val progress: Float, // 0.0 to 1.0
    val message: String,
    val isComplete: Boolean = false,
    val error: String? = null,
    val geodatabase: Geodatabase? = null
)
