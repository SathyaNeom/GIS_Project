package com.enbridge.gdsgpscollection.util

/**
 * @author Sathya Narayanan
 */

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Monitors animation performance and device capabilities to determine
 * whether animations should be enabled, simplified, or disabled.
 *
 * This helper ensures smooth user experience by:
 * - Detecting low-end devices
 * - Monitoring memory pressure
 * - Providing performance-based animation recommendations
 *
 * ## Usage:
 * ```kotlin
 * val performanceLevel by rememberAnimationPerformanceLevel()
 *
 * when (performanceLevel) {
 *     AnimationPerformanceLevel.HIGH -> {
 *         // Use all animations with spring physics
 *     }
 *     AnimationPerformanceLevel.MEDIUM -> {
 *         // Use standard animations, avoid complex ones
 *     }
 *     AnimationPerformanceLevel.LOW -> {
 *         // Use minimal or no animations
 *     }
 * }
 * ```
 */
class AnimationPerformanceMonitor(private val context: Context) {

    companion object {
        private const val TAG = "AnimationPerformanceMonitor"

        /**
         * Minimum RAM (in MB) for full animation support
         */
        private const val RAM_THRESHOLD_HIGH = 4096 // 4GB

        /**
         * Minimum RAM (in MB) for medium animation support
         */
        private const val RAM_THRESHOLD_MEDIUM = 2048 // 2GB

        /**
         * Poll interval for performance checks (in milliseconds)
         */
        const val PERFORMANCE_POLL_INTERVAL = 5000L
    }

    /**
     * Performance level categories for animation decisions
     */
    enum class PerformanceLevel {
        /**
         * High-end device - Full animation support
         * - All animations enabled
         * - Complex spring animations allowed
         * - Multiple concurrent animations supported
         */
        HIGH,

        /**
         * Mid-range device - Standard animation support
         * - Standard animations enabled
         * - Simple tweens preferred over springs
         * - Limited concurrent animations
         */
        MEDIUM,

        /**
         * Low-end device - Minimal animation support
         * - Only essential animations
         * - Fast or instant animations
         * - Avoid complex animations
         */
        LOW
    }

    /**
     * Determines the performance level based on device capabilities.
     *
     * Factors considered:
     * - Device RAM
     * - Android version
     * - Processor cores
     *
     * @return PerformanceLevel indicating animation capability
     */
    fun getPerformanceLevel(): PerformanceLevel {
        val totalRam = getTotalRamMB()
        val processorCores = Runtime.getRuntime().availableProcessors()

        Logger.d(
            TAG, "Device specs - RAM: ${totalRam}MB, CPU cores: $processorCores, " +
                    "Android version: ${Build.VERSION.SDK_INT}"
        )

        // Determine performance level
        val level = when {
            // High-end: 4GB+ RAM or 8+ cores
            totalRam >= RAM_THRESHOLD_HIGH || processorCores >= 8 -> {
                PerformanceLevel.HIGH
            }
            // Medium: 2GB+ RAM or 4+ cores
            totalRam >= RAM_THRESHOLD_MEDIUM || processorCores >= 4 -> {
                PerformanceLevel.MEDIUM
            }
            // Low-end: Less than 2GB RAM and fewer cores
            else -> {
                PerformanceLevel.LOW
            }
        }

        Logger.i(TAG, "Animation performance level: $level")
        return level
    }

    /**
     * Gets total device RAM in megabytes.
     *
     * @return Total RAM in MB, or -1 if unable to determine
     */
    private fun getTotalRamMB(): Long {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE)
                    as? android.app.ActivityManager

            activityManager?.let {
                val memoryInfo = android.app.ActivityManager.MemoryInfo()
                it.getMemoryInfo(memoryInfo)
                // Convert bytes to megabytes
                memoryInfo.totalMem / (1024 * 1024)
            } ?: -1L
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting total RAM", e)
            -1L
        }
    }

    /**
     * Checks if device is considered low-end for animation purposes.
     *
     * @return true if device should use minimal animations
     */
    fun isLowEndDevice(): Boolean {
        return getPerformanceLevel() == PerformanceLevel.LOW
    }

    /**
     * Checks if device can handle complex animations (springs, multiple concurrent animations).
     *
     * @return true if complex animations are recommended
     */
    fun canHandleComplexAnimations(): Boolean {
        return getPerformanceLevel() == PerformanceLevel.HIGH
    }

    /**
     * Gets recommended animation duration based on performance level.
     *
     * @param baseDuration Base duration in milliseconds
     * @return Adjusted duration based on device performance
     */
    fun getRecommendedDuration(baseDuration: Int): Int {
        return when (getPerformanceLevel()) {
            PerformanceLevel.HIGH -> baseDuration
            PerformanceLevel.MEDIUM -> (baseDuration * 0.8f).toInt() // Slightly faster
            PerformanceLevel.LOW -> (baseDuration * 0.5f).toInt() // Much faster
        }
    }

    /**
     * Gets current available memory percentage.
     *
     * @return Percentage of available memory (0-100), or -1 if unable to determine
     */
    fun getAvailableMemoryPercent(): Int {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE)
                    as? android.app.ActivityManager

            activityManager?.let {
                val memoryInfo = android.app.ActivityManager.MemoryInfo()
                it.getMemoryInfo(memoryInfo)
                val availablePercent = ((memoryInfo.availMem.toDouble() /
                        memoryInfo.totalMem.toDouble()) * 100).toInt()

                Logger.d(TAG, "Available memory: $availablePercent%")
                availablePercent
            } ?: -1
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting available memory", e)
            -1
        }
    }

    /**
     * Checks if device is under memory pressure.
     *
     * @return true if available memory is critically low
     */
    fun isUnderMemoryPressure(): Boolean {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE)
                    as? android.app.ActivityManager

            activityManager?.let {
                val memoryInfo = android.app.ActivityManager.MemoryInfo()
                it.getMemoryInfo(memoryInfo)

                val isLowMemory = memoryInfo.lowMemory
                if (isLowMemory) {
                    Logger.w(TAG, "Device is under memory pressure - consider disabling animations")
                }
                isLowMemory
            } ?: false
        } catch (e: Exception) {
            Logger.e(TAG, "Error checking memory pressure", e)
            false
        }
    }
}

/**
 * Composable function that remembers the device's animation performance level.
 *
 * This function checks performance every 5 seconds to detect memory pressure changes.
 *
 * @return State<PerformanceLevel> indicating device animation capability
 *
 * ## Example:
 * ```kotlin
 * @Composable
 * fun MyAnimatedComponent() {
 *     val performanceLevel by rememberAnimationPerformanceLevel()
 *
 *     val animationSpec = when (performanceLevel) {
 *         PerformanceLevel.HIGH -> AnimationConstants.SPRING_BOUNCY
 *         PerformanceLevel.MEDIUM -> AnimationConstants.tweenNormal()
 *         PerformanceLevel.LOW -> AnimationConstants.tweenFast()
 *     }
 *
 *     // Use animationSpec in animations
 * }
 * ```
 */
@Composable
fun rememberAnimationPerformanceLevel(): State<AnimationPerformanceMonitor.PerformanceLevel> {
    val context = LocalContext.current
    val monitor = AnimationPerformanceMonitor(context)

    return produceState(initialValue = monitor.getPerformanceLevel()) {
        while (isActive) {
            // Check for memory pressure periodically
            if (monitor.isUnderMemoryPressure()) {
                // Downgrade to LOW if under memory pressure
                value = AnimationPerformanceMonitor.PerformanceLevel.LOW
            } else {
                value = monitor.getPerformanceLevel()
            }
            delay(AnimationPerformanceMonitor.PERFORMANCE_POLL_INTERVAL)
        }
    }
}

/**
 * Composable function that checks if animations should be enabled
 * based on both accessibility settings and device performance.
 *
 * This combines accessibility preferences with performance capabilities.
 *
 * @return State<Boolean> indicating if animations should be enabled
 *
 * ## Example:
 * ```kotlin
 * @Composable
 * fun MyComponent() {
 *     val shouldAnimate by rememberShouldAnimate()
 *
 *     AnimatedVisibility(
 *         visible = isVisible,
 *         enter = if (shouldAnimate) fadeIn() else EnterTransition.None
 *     )
 * }
 * ```
 */
@Composable
fun rememberShouldAnimate(): State<Boolean> {
    val accessibilityEnabled = rememberAnimationsEnabled()
    val performanceLevel = rememberAnimationPerformanceLevel()

    return produceState(
        initialValue = accessibilityEnabled.value &&
                performanceLevel.value != AnimationPerformanceMonitor.PerformanceLevel.LOW
    ) {
        value = accessibilityEnabled.value &&
                performanceLevel.value != AnimationPerformanceMonitor.PerformanceLevel.LOW
    }
}

/**
 * Extension function to check if device can handle complex animations.
 *
 * @receiver Context
 * @return true if springs and complex animations are recommended
 *
 * ## Example:
 * ```kotlin
 * if (context.canHandleComplexAnimations()) {
 *     // Use spring animations
 * } else {
 *     // Use simple tweens
 * }
 * ```
 */
fun Context.canHandleComplexAnimations(): Boolean {
    return AnimationPerformanceMonitor(this).canHandleComplexAnimations()
}

/**
 * Extension function to check if device is low-end.
 *
 * @receiver Context
 * @return true if device should use minimal animations
 */
fun Context.isLowEndDevice(): Boolean {
    return AnimationPerformanceMonitor(this).isLowEndDevice()
}
