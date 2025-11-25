package com.enbridge.gdsgpscollection.designsystem.theme

/**
 * @author Sathya Narayanan
 */

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec

/**
 * Centralized animation constants for consistent motion design throughout the app.
 *
 * All animations follow Material Design motion principles:
 * - Fast: 150ms - For small UI elements (buttons, icons)
 * - Normal: 300ms - For standard transitions (dialogs, sheets)
 * - Slow: 500ms - For complex animations (screen transitions)
 *
 * Usage:
 * ```kotlin
 * animateFloatAsState(
 *     targetValue = target,
 *     animationSpec = AnimationConstants.tweenNormal()
 * )
 * ```
 */
object AnimationConstants {

    // ============================================================================
    // Duration Constants
    // ============================================================================

    /**
     * Fast animations - Used for immediate feedback
     * Examples: Button press, checkbox toggle, ripple effects
     */
    const val DURATION_FAST = 150

    /**
     * Normal animations - Standard transition speed
     * Examples: Dialog entrance, bottom sheet expansion, tab switching
     */
    const val DURATION_NORMAL = 300

    /**
     * Slow animations - Complex multi-step animations
     * Examples: Screen transitions, large content animations
     */
    const val DURATION_SLOW = 500

    /**
     * Instant animations - No animation (accessibility fallback)
     */
    const val DURATION_INSTANT = 0

    /**
     * Stagger delay for list items
     * Used when animating multiple items sequentially
     */
    const val STAGGER_DELAY = 50

    /**
     * Stagger delay for shorter sequences
     */
    const val STAGGER_DELAY_SHORT = 30

    // ============================================================================
    // Easing Curves
    // ============================================================================

    /**
     * Standard easing - Smooth acceleration and deceleration
     * Best for: Most animations
     */
    val EASING_STANDARD: Easing = FastOutSlowInEasing

    /**
     * Emphasized easing - More pronounced curves for important animations
     * Best for: Dialogs, important state changes
     */
    val EASING_EMPHASIZED: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)

    /**
     * Decelerated easing - Starts fast, slows down (entrance animations)
     * Best for: Elements entering the screen
     */
    val EASING_DECELERATE: Easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)

    /**
     * Accelerated easing - Starts slow, speeds up (exit animations)
     * Best for: Elements leaving the screen
     */
    val EASING_ACCELERATE: Easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)

    /**
     * Linear easing - Constant speed throughout
     * Best for: Progress indicators, loading states
     */
    val EASING_LINEAR: Easing = LinearEasing

    // ============================================================================
    // Spring Specifications
    // ============================================================================

    /**
     * Bouncy spring - Visible bounce effect
     * Best for: Success states, playful interactions
     */
    val SPRING_BOUNCY: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /**
     * Smooth spring - Subtle bounce
     * Best for: Professional UI, subtle feedback
     */
    val SPRING_SMOOTH: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /**
     * Stiff spring - Quick, snappy motion with minimal bounce
     * Best for: Button presses, toggles
     */
    val SPRING_STIFF: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )

    // ============================================================================
    // Tween Animation Specs (Pre-configured)
    // ============================================================================

    /**
     * Fast tween with standard easing
     */
    fun <T> tweenFast(): TweenSpec<T> = tween(
        durationMillis = DURATION_FAST,
        easing = EASING_STANDARD
    )

    /**
     * Normal tween with standard easing
     */
    fun <T> tweenNormal(): TweenSpec<T> = tween(
        durationMillis = DURATION_NORMAL,
        easing = EASING_STANDARD
    )

    /**
     * Slow tween with standard easing
     */
    fun <T> tweenSlow(): TweenSpec<T> = tween(
        durationMillis = DURATION_SLOW,
        easing = EASING_STANDARD
    )

    /**
     * Fast tween with emphasized easing (for important quick actions)
     */
    fun <T> tweenFastEmphasized(): TweenSpec<T> = tween(
        durationMillis = DURATION_FAST,
        easing = EASING_EMPHASIZED
    )

    /**
     * Normal tween with emphasized easing (for important transitions)
     */
    fun <T> tweenNormalEmphasized(): TweenSpec<T> = tween(
        durationMillis = DURATION_NORMAL,
        easing = EASING_EMPHASIZED
    )

    /**
     * Entrance animation spec - Decelerated easing
     */
    fun <T> tweenEnter(): TweenSpec<T> = tween(
        durationMillis = DURATION_NORMAL,
        easing = EASING_DECELERATE
    )

    /**
     * Exit animation spec - Accelerated easing
     */
    fun <T> tweenExit(): TweenSpec<T> = tween(
        durationMillis = DURATION_FAST,
        easing = EASING_ACCELERATE
    )

    /**
     * Instant animation (accessibility fallback)
     */
    fun <T> tweenInstant(): TweenSpec<T> = tween(
        durationMillis = DURATION_INSTANT,
        easing = EASING_LINEAR
    )

    // ============================================================================
    // Scale Values
    // ============================================================================

    /**
     * Scale for button press state (slightly smaller)
     */
    const val SCALE_PRESSED = 0.95f

    /**
     * Scale for button hover/focus state (slightly larger)
     */
    const val SCALE_HOVERED = 1.05f

    /**
     * Scale for bounce effect (larger)
     */
    const val SCALE_BOUNCE = 1.1f

    /**
     * Normal scale (no transformation)
     */
    const val SCALE_NORMAL = 1.0f

    /**
     * Scale for initial entrance (starts smaller)
     */
    const val SCALE_INITIAL = 0.8f

    // ============================================================================
    // Alpha Values
    // ============================================================================

    /**
     * Fully visible
     */
    const val ALPHA_VISIBLE = 1.0f

    /**
     * Partially visible (disabled state)
     */
    const val ALPHA_DISABLED = 0.38f

    /**
     * Slightly transparent (loading state)
     */
    const val ALPHA_LOADING = 0.6f

    /**
     * Completely invisible
     */
    const val ALPHA_INVISIBLE = 0.0f

    // ============================================================================
    // Offset Values (for slide animations)
    // ============================================================================

    /**
     * Offset for slide animations (in pixels)
     */
    const val SLIDE_OFFSET = 50

    /**
     * Small offset for subtle movements
     */
    const val SLIDE_OFFSET_SMALL = 20

    /**
     * Large offset for screen transitions
     */
    const val SLIDE_OFFSET_LARGE = 100

    // ============================================================================
    // Rotation Values
    // ============================================================================

    /**
     * Slight rotation for shake effect
     */
    const val ROTATION_SHAKE = 5f

    /**
     * Quarter turn
     */
    const val ROTATION_QUARTER = 90f

    /**
     * Half turn
     */
    const val ROTATION_HALF = 180f

    /**
     * Full turn
     */
    const val ROTATION_FULL = 360f
}
