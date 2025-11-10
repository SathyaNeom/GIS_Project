package com.enbridge.gdsgpscollection.util.storage

import android.content.Context
import android.os.StatFs
import com.enbridge.gdsgpscollection.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

/**
 * Utility class for storage space validation and monitoring.
 *
 * Provides methods to check available storage space, validate if sufficient space exists,
 * and format file sizes in human-readable format.
 *
 * @property context Application context for accessing file directories
 */
@Singleton
class StorageUtil @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "StorageUtil"

        // Minimum free space to keep as buffer (10 MB)
        const val MINIMUM_FREE_SPACE_BYTES = 10 * 1024 * 1024L

        // Safety margin multiplier (1.5x required size)
        const val SAFETY_MARGIN = 1.5
    }

    /**
     * Gets available storage space in bytes.
     *
     * @return Available space in bytes, or 0 if error
     */
    fun getAvailableSpaceBytes(): Long {
        return try {
            val filesDir = context.filesDir
            val stat = StatFs(filesDir.path)
            stat.availableBytes
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting available space", e)
            0L
        }
    }

    /**
     * Gets total storage space in bytes.
     *
     * @return Total space in bytes, or 0 if error
     */
    fun getTotalSpaceBytes(): Long {
        return try {
            val filesDir = context.filesDir
            val stat = StatFs(filesDir.path)
            stat.totalBytes
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting total space", e)
            0L
        }
    }

    /**
     * Checks if sufficient storage space is available for a file of given size.
     *
     * Applies safety margin and minimum free space buffer.
     *
     * @param requiredBytes Size of file to download in bytes
     * @return StorageCheckResult with detailed information
     */
    fun checkStorageAvailability(requiredBytes: Long): StorageCheckResult {
        val availableBytes = getAvailableSpaceBytes()
        val requiredWithMargin = (requiredBytes * SAFETY_MARGIN).toLong()
        val totalRequired = requiredWithMargin + MINIMUM_FREE_SPACE_BYTES

        Logger.d(
            TAG, "Storage check: Required=${formatBytes(requiredBytes)}, " +
                    "Available=${formatBytes(availableBytes)}, " +
                    "With margin=${formatBytes(totalRequired)}"
        )

        return when {
            availableBytes < MINIMUM_FREE_SPACE_BYTES -> {
                StorageCheckResult.CriticallyLow(
                    availableBytes = availableBytes,
                    requiredBytes = totalRequired
                )
            }

            availableBytes < totalRequired -> {
                StorageCheckResult.Insufficient(
                    availableBytes = availableBytes,
                    requiredBytes = totalRequired
                )
            }

            availableBytes < totalRequired * 1.2 -> {
                StorageCheckResult.Low(
                    availableBytes = availableBytes,
                    requiredBytes = totalRequired
                )
            }

            else -> {
                StorageCheckResult.Sufficient(availableBytes = availableBytes)
            }
        }
    }

    /**
     * Formats bytes into human-readable string (KB, MB, GB).
     *
     * @param bytes Size in bytes
     * @return Formatted string (e.g., "45.2 MB")
     */
    fun formatBytes(bytes: Long): String {
        if (bytes < 0) return "Unknown"
        if (bytes == 0L) return "0 B"

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val unitIndex = digitGroups.coerceIn(0, units.size - 1)

        val value = bytes / 1024.0.pow(unitIndex.toDouble())
        return String.format("%.1f %s", value, units[unitIndex])
    }

    /**
     * Gets the size of a file in bytes.
     *
     * @param file The file to check
     * @return File size in bytes, or 0 if file doesn't exist
     */
    fun getFileSize(file: File): Long {
        return if (file.exists()) file.length() else 0L
    }

    /**
     * Estimates geodatabase size based on extent.
     *
     * This is a rough estimate - actual size depends on data density.
     *
     * @param extentArea Area covered by extent (optional)
     * @return Estimated size in bytes
     */
    fun estimateGeodatabaseSize(extentArea: Double? = null): Long {
        // Default estimate: 30 MB
        // TODO: Implement more sophisticated estimation based on extent and layer count
        return 30 * 1024 * 1024L
    }
}

/**
 * Sealed class representing storage availability status.
 */
sealed class StorageCheckResult {
    abstract val availableBytes: Long

    /**
     * Sufficient storage available.
     */
    data class Sufficient(override val availableBytes: Long) : StorageCheckResult()

    /**
     * Low storage but download may proceed.
     */
    data class Low(
        override val availableBytes: Long,
        val requiredBytes: Long
    ) : StorageCheckResult()

    /**
     * Insufficient storage - download not recommended.
     */
    data class Insufficient(
        override val availableBytes: Long,
        val requiredBytes: Long
    ) : StorageCheckResult()

    /**
     * Critically low - storage almost full.
     */
    data class CriticallyLow(
        override val availableBytes: Long,
        val requiredBytes: Long
    ) : StorageCheckResult()
}
