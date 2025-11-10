package com.enbridge.gdsgpscollection.util.error

import com.enbridge.gdsgpscollection.util.Logger
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Maps exceptions and error messages to user-friendly error types.
 *
 * Analyzes exceptions from ArcGIS SDK and network operations to determine
 * the appropriate error type and user-facing message.
 */
object ErrorMapper {
    private const val TAG = "ErrorMapper"

    /**
     * Maps a throwable to a GeodatabaseError type.
     *
     * @param error The exception/throwable to analyze
     * @param context Additional context from error message
     * @return GeodatabaseError enum representing the error category
     */
    fun mapError(error: Throwable?, context: String? = null): GeodatabaseError {
        Logger.d(TAG, "Mapping error: ${error?.javaClass?.simpleName}, message: ${error?.message}")

        return when {
            // Network connectivity errors
            error is UnknownHostException -> GeodatabaseError.NO_INTERNET
            error is SocketTimeoutException -> GeodatabaseError.SERVER_TIMEOUT
            error is IOException && isConnectionError(error.message) -> GeodatabaseError.CONNECTION_LOST

            // Server errors (detect from message)
            isServerUnavailable(error?.message, context) -> GeodatabaseError.SERVER_UNAVAILABLE
            isAuthenticationError(error?.message, context) -> GeodatabaseError.AUTHENTICATION_FAILED
            isAuthorizationError(error?.message, context) -> GeodatabaseError.AUTHORIZATION_FAILED

            // Storage errors
            isStorageError(error?.message) -> GeodatabaseError.STORAGE_FULL

            // File errors
            isFileCorrupted(error?.message) -> GeodatabaseError.FILE_CORRUPTED
            isFileLocked(error?.message) -> GeodatabaseError.FILE_LOCKED

            // Generic errors
            error != null -> GeodatabaseError.UNKNOWN

            else -> GeodatabaseError.UNKNOWN
        }
    }

    /**
     * Checks if error message indicates a connection error.
     */
    private fun isConnectionError(message: String?): Boolean {
        val indicators = listOf(
            "connection",
            "network",
            "no route to host",
            "connection refused",
            "connection reset"
        )
        return message?.lowercase()?.let { msg ->
            indicators.any { msg.contains(it) }
        } ?: false
    }

    /**
     * Checks if error indicates server unavailability (503, 504).
     */
    private fun isServerUnavailable(errorMessage: String?, context: String?): Boolean {
        val indicators = listOf("503", "504", "service unavailable", "gateway timeout")
        val combinedMessage = "$errorMessage $context".lowercase()
        return indicators.any { combinedMessage.contains(it) }
    }

    /**
     * Checks if error indicates authentication failure (401).
     */
    private fun isAuthenticationError(errorMessage: String?, context: String?): Boolean {
        val indicators = listOf("401", "unauthorized", "authentication", "invalid token")
        val combinedMessage = "$errorMessage $context".lowercase()
        return indicators.any { combinedMessage.contains(it) }
    }

    /**
     * Checks if error indicates authorization failure (403).
     */
    private fun isAuthorizationError(errorMessage: String?, context: String?): Boolean {
        val indicators = listOf("403", "forbidden", "access denied", "permission")
        val combinedMessage = "$errorMessage $context".lowercase()
        return indicators.any { combinedMessage.contains(it) }
    }

    /**
     * Checks if error indicates storage issue.
     */
    private fun isStorageError(message: String?): Boolean {
        val indicators = listOf("no space", "disk full", "storage", "enospc")
        return message?.lowercase()?.let { msg ->
            indicators.any { msg.contains(it) }
        } ?: false
    }

    /**
     * Checks if error indicates file corruption.
     */
    private fun isFileCorrupted(message: String?): Boolean {
        val indicators = listOf("corrupt", "invalid", "malformed", "damaged")
        return message?.lowercase()?.let { msg ->
            indicators.any { msg.contains(it) }
        } ?: false
    }

    /**
     * Checks if error indicates file is locked.
     */
    private fun isFileLocked(message: String?): Boolean {
        val indicators = listOf("locked", "in use", "access denied", "busy")
        return message?.lowercase()?.let { msg ->
            indicators.any { msg.contains(it) }
        } ?: false
    }
}

/**
 * Enum representing different types of geodatabase-related errors.
 */
enum class GeodatabaseError {
    /** No internet connection available */
    NO_INTERNET,

    /** Internet connection was lost during operation */
    CONNECTION_LOST,

    /** Server is temporarily unavailable (503/504) */
    SERVER_UNAVAILABLE,

    /** Server timeout - took too long to respond */
    SERVER_TIMEOUT,

    /** Authentication failed (401) */
    AUTHENTICATION_FAILED,

    /** Authorization failed - insufficient permissions (403) */
    AUTHORIZATION_FAILED,

    /** Storage space is full */
    STORAGE_FULL,

    /** Geodatabase file is corrupted */
    FILE_CORRUPTED,

    /** Geodatabase file is locked by another process */
    FILE_LOCKED,

    /** Unknown or unclassified error */
    UNKNOWN
}
