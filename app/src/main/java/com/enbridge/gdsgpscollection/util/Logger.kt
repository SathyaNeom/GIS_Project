package com.enbridge.gdsgpscollection.util

/**
 * @author Sathya Narayanan
 */

import android.util.Log
import com.enbridge.gdsgpscollection.BuildConfig

/**
 * Centralized Logger utility that respects build variants.
 *
 * This logger only outputs logs when the build type is 'debug'.
 * In release builds, all log statements are no-ops to improve performance
 * and prevent sensitive information leakage.
 *
 * Features:
 * - Automatic tag generation from class name
 * - Build-variant aware (only logs in debug builds)
 * - Support for all log levels (VERBOSE, DEBUG, INFO, WARN, ERROR)
 * - Exception logging support
 * - Performance optimized for release builds
 *
 * Usage:
 * ```
 * Logger.d("MyScreen", "User tapped button")
 * Logger.e("NetworkRepo", "Failed to fetch data", exception)
 * Logger.i("LoginViewModel", "Login successful for user: $username")
 * ```
 *
 * Best Practices:
 * - Use appropriate log levels (d for debug, i for info, w for warnings, e for errors)
 * - Include relevant context in log messages
 * - Use consistent tag naming (typically class name)
 * - Log state transitions for easier debugging
 * - Log user actions for behavior tracking
 * - Always log errors with exceptions
 */
object Logger {

    /**
     * Determines if logging is enabled based on build configuration.
     * Logs are only enabled in debug builds.
     */
    private val isLoggingEnabled: Boolean
        get() = BuildConfig.DEBUG

    /**
     * Log a VERBOSE message.
     * Use for detailed tracing information during development.
     *
     * @param tag Identifier for the log message source (typically class name)
     * @param message The message to log
     * @param throwable Optional exception to log
     */
    @JvmStatic
    fun v(tag: String, message: String, throwable: Throwable? = null) {
        if (isLoggingEnabled) {
            if (throwable != null) {
                Log.v(tag, message, throwable)
            } else {
                Log.v(tag, message)
            }
        }
    }

    /**
     * Log a DEBUG message.
     * Use for general debugging information, state changes, and flow tracking.
     *
     * @param tag Identifier for the log message source (typically class name)
     * @param message The message to log
     * @param throwable Optional exception to log
     */
    @JvmStatic
    fun d(tag: String, message: String, throwable: Throwable? = null) {
        if (isLoggingEnabled) {
            if (throwable != null) {
                Log.d(tag, message, throwable)
            } else {
                Log.d(tag, message)
            }
        }
    }

    /**
     * Log an INFO message.
     * Use for important informational messages about app state and user actions.
     *
     * @param tag Identifier for the log message source (typically class name)
     * @param message The message to log
     * @param throwable Optional exception to log
     */
    @JvmStatic
    fun i(tag: String, message: String, throwable: Throwable? = null) {
        if (isLoggingEnabled) {
            if (throwable != null) {
                Log.i(tag, message, throwable)
            } else {
                Log.i(tag, message)
            }
        }
    }

    /**
     * Log a WARNING message.
     * Use for potentially problematic situations that aren't errors.
     *
     * @param tag Identifier for the log message source (typically class name)
     * @param message The message to log
     * @param throwable Optional exception to log
     */
    @JvmStatic
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (isLoggingEnabled) {
            if (throwable != null) {
                Log.w(tag, message, throwable)
            } else {
                Log.w(tag, message)
            }
        }
    }

    /**
     * Log an ERROR message.
     * Use for error conditions and exceptions.
     *
     * @param tag Identifier for the log message source (typically class name)
     * @param message The message to log
     * @param throwable Optional exception to log
     */
    @JvmStatic
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (isLoggingEnabled) {
            if (throwable != null) {
                Log.e(tag, message, throwable)
            } else {
                Log.e(tag, message)
            }
        }
    }

    /**
     * Log a What a Terrible Failure (WTF) message.
     * Use for conditions that should never happen.
     *
     * @param tag Identifier for the log message source (typically class name)
     * @param message The message to log
     * @param throwable Optional exception to log
     */
    @JvmStatic
    fun wtf(tag: String, message: String, throwable: Throwable? = null) {
        if (isLoggingEnabled) {
            if (throwable != null) {
                Log.wtf(tag, message, throwable)
            } else {
                Log.wtf(tag, message)
            }
        }
    }

    /**
     * Creates a formatted log message with contextual information.
     *
     * @param action The action being performed
     * @param details Additional details about the action
     * @return Formatted log message
     */
    @JvmStatic
    fun formatMessage(action: String, details: String): String {
        return "$action | $details"
    }

    /**
     * Logs method entry for tracing execution flow.
     *
     * @param tag Identifier for the log message source
     * @param methodName Name of the method being entered
     * @param params Optional parameters to log
     */
    @JvmStatic
    fun entering(tag: String, methodName: String, params: String = "") {
        if (isLoggingEnabled) {
            val message = if (params.isNotEmpty()) {
                "→ Entering $methodName($params)"
            } else {
                "→ Entering $methodName()"
            }
            Log.d(tag, message)
        }
    }

    /**
     * Logs method exit for tracing execution flow.
     *
     * @param tag Identifier for the log message source
     * @param methodName Name of the method being exited
     * @param result Optional result to log
     */
    @JvmStatic
    fun exiting(tag: String, methodName: String, result: String = "") {
        if (isLoggingEnabled) {
            val message = if (result.isNotEmpty()) {
                "← Exiting $methodName | Result: $result"
            } else {
                "← Exiting $methodName"
            }
            Log.d(tag, message)
        }
    }
}
