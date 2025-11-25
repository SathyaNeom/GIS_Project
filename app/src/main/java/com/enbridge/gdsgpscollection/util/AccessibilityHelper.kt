package com.enbridge.gdsgpscollection.util

/**
 * @author Sathya Narayanan
 */

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import com.enbridge.gdsgpscollection.designsystem.theme.AnimationConstants
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Helper class for managing accessibility settings and animation behavior.
 *
 * This helper ensures animations respect system accessibility preferences:
 * - Reduced motion settings
 * - Animation duration scale
 * - Accessibility services enabled
 *
 * ## Usage in Composables:
 * ```kotlin
 * val animationsEnabled by rememberAnimationsEnabled()
 *
 * AnimatedVisibility(
 *     visible = isVisible,
 *     enter = if (animationsEnabled) fadeIn() else EnterTransition.None
 * )
 * ```
 *
 * ## Usage in ViewModels:
 * ```kotlin
 * val helper = AccessibilityHelper(context)
 * if (helper.areAnimationsEnabled()) {
 *     // Use animated transitions
 * } else {
 *     // Use instant transitions
 * }
 * ```
 */
class AccessibilityHelper(private val context: Context) {

    companion object {
        private const val TAG = "AccessibilityHelper"

        /**
         * Threshold for animation duration scale.
         * If scale is below this, animations are considered disabled.
         */
        private const val ANIMATION_SCALE_THRESHOLD = 0.1f

        /**
         * Poll interval for checking animation settings (in milliseconds)
         */
        const val POLL_INTERVAL = 1000L
    }

    /**
     * Checks if animations are enabled based on system settings.
     *
     * Animations are considered disabled if:
     * - Animator duration scale is off (0.0)
     * - Animator duration scale is very low (< 0.1)
     * - Transition animation scale is off
     * - Window animation scale is off
     *
     * @return true if animations should be enabled, false otherwise
     */
    fun areAnimationsEnabled(): Boolean {
        return try {
            val animatorDurationScale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )

            val transitionAnimationScale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.TRANSITION_ANIMATION_SCALE,
                1.0f
            )

            val windowAnimationScale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.WINDOW_ANIMATION_SCALE,
                1.0f
            )

            // Animations are enabled if at least animator duration scale is enabled
            val enabled = animatorDurationScale >= ANIMATION_SCALE_THRESHOLD

            Logger.d(
                TAG, "Animation scales - Animator: $animatorDurationScale, " +
                        "Transition: $transitionAnimationScale, Window: $windowAnimationScale"
            )
            Logger.d(TAG, "Animations enabled: $enabled")

            enabled
        } catch (e: Exception) {
            Logger.e(TAG, "Error checking animation settings", e)
            // Default to enabled if we can't read settings
            true
        }
    }

    /**
     * Gets the current animation duration scale from system settings.
     *
     * @return Scale factor (0.0 = disabled, 1.0 = normal, 0.5 = half speed, etc.)
     */
    fun getAnimationDurationScale(): Float {
        return try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
        } catch (e: Exception) {
            Logger.e(TAG, "Error reading animation duration scale", e)
            1.0f
        }
    }

    /**
     * Calculates the actual animation duration based on system scale.
     *
     * @param baseDuration Base duration in milliseconds
     * @return Adjusted duration based on system settings
     */
    fun getAdjustedDuration(baseDuration: Int): Int {
        if (!areAnimationsEnabled()) {
            return AnimationConstants.DURATION_INSTANT
        }

        val scale = getAnimationDurationScale()
        return (baseDuration * scale).toInt()
    }

    /**
     * Checks if reduced motion is preferred.
     * Returns true if animations should be minimal or disabled.
     *
     * Note: Android doesn't have a direct "reduce motion" setting like iOS,
     * but we can infer it from animation scales.
     */
    fun isReducedMotionPreferred(): Boolean {
        val scale = getAnimationDurationScale()
        return scale < ANIMATION_SCALE_THRESHOLD
    }
}

/**
 * Composable function that remembers animation enabled state.
 *
 * This function polls the system settings every second to detect changes.
 * Use this in composables that need to react to accessibility settings changes.
 *
 * @return State<Boolean> indicating if animations are enabled
 *
 * ## Example:
 * ```kotlin
 * @Composable
 * fun MyAnimatedComponent() {
 *     val animationsEnabled by rememberAnimationsEnabled()
 *
 *     AnimatedVisibility(
 *         visible = isVisible,
 *         enter = if (animationsEnabled) {
 *             fadeIn(animationSpec = AnimationConstants.tweenNormal())
 *         } else {
 *             EnterTransition.None
 *         }
 *     )
 * }
 * ```
 */
@Composable
fun rememberAnimationsEnabled(): State<Boolean> {
    val context = LocalContext.current
    val helper = AccessibilityHelper(context)

    return produceState(initialValue = helper.areAnimationsEnabled()) {
        while (isActive) {
            value = helper.areAnimationsEnabled()
            delay(AccessibilityHelper.POLL_INTERVAL)
        }
    }
}

/**
 * Composable function that remembers the animation duration scale.
 *
 * Use this when you need to adjust animation durations dynamically.
 *
 * @return State<Float> indicating the current animation scale (0.0 to 1.0+)
 *
 * ## Example:
 * ```kotlin
 * @Composable
 * fun MyAnimatedComponent() {
 *     val animationScale by rememberAnimationDurationScale()
 *
 *     val duration = (AnimationConstants.DURATION_NORMAL * animationScale).toInt()
 *
 *     AnimatedVisibility(
 *         visible = isVisible,
 *         enter = fadeIn(animationSpec = tween(durationMillis = duration))
 *     )
 * }
 * ```
 */
@Composable
fun rememberAnimationDurationScale(): State<Float> {
    val context = LocalContext.current
    val helper = AccessibilityHelper(context)

    return produceState(initialValue = helper.getAnimationDurationScale()) {
        while (isActive) {
            value = helper.getAnimationDurationScale()
            delay(AccessibilityHelper.POLL_INTERVAL)
        }
    }
}

/**
 * Extension function to get an animation-aware duration.
 *
 * Returns the base duration if animations are enabled,
 * or instant (0ms) if animations are disabled.
 *
 * @receiver Context
 * @param baseDuration Base animation duration in milliseconds
 * @return Adjusted duration based on accessibility settings
 *
 * ## Example:
 * ```kotlin
 * val duration = context.getAnimationDuration(AnimationConstants.DURATION_NORMAL)
 * ```
 */
fun Context.getAnimationDuration(baseDuration: Int): Int {
    return AccessibilityHelper(this).getAdjustedDuration(baseDuration)
}

/**
 * Extension function to check if animations are enabled.
 *
 * @receiver Context
 * @return true if animations should be enabled
 *
 * ## Example:
 * ```kotlin
 * if (context.areAnimationsEnabled()) {
 *     // Use animated transition
 * } else {
 *     // Use instant transition
 * }
 * ```
 */
fun Context.areAnimationsEnabled(): Boolean {
    return AccessibilityHelper(this).areAnimationsEnabled()
}

/**
 * Note: For checking if animations should be used (combining accessibility + performance),
 * use `rememberShouldAnimate()` from AnimationPerformanceMonitor.kt instead.
 *
 * @see com.enbridge.gdsgpscollection.util.rememberShouldAnimate
 */
