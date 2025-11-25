package com.enbridge.gdsgpscollection.designsystem.components

/**
 * @author Sathya Narayanan
 */

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.enbridge.gdsgpscollection.R
import com.enbridge.gdsgpscollection.designsystem.theme.AnimationConstants
import com.enbridge.gdsgpscollection.util.Logger
import com.enbridge.gdsgpscollection.util.rememberShouldAnimate

/**
 * Success checkmark animation component.
 *
 * **Animations:**
 * - Scale entrance with bounce (0 → 1.2 → 1.0)
 * - Rotation from -45° to 0° for dynamic entrance
 * - Bouncy spring for celebratory feel
 *
 * **Use Cases:**
 * - Download completion
 * - Form submission success
 * - Sync completion
 * - Data save confirmation
 *
 * @param isVisible Whether the checkmark should be visible
 * @param size Size of the icon
 * @param tint Color of the checkmark (defaults to success green)
 * @param modifier Optional modifier
 *
 * ## Example:
 * ```kotlin
 * var showSuccess by remember { mutableStateOf(false) }
 *
 * Button(onClick = {
 *     // Perform action
 *     showSuccess = true
 * }) {
 *     Text("Save")
 * }
 *
 * if (showSuccess) {
 *     SuccessCheckmarkAnimation(isVisible = true)
 * }
 * ```
 */
@Composable
fun SuccessCheckmarkAnimation(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    val shouldAnimate by rememberShouldAnimate()

    // Log animation trigger
    LaunchedEffect(isVisible) {
        if (isVisible) {
            Logger.d("SuccessCheckmark", "Success animation triggered")
        }
    }

    // Scale animation with bounce
    val scale by animateFloatAsState(
        targetValue = if (isVisible) AnimationConstants.SCALE_NORMAL else 0f,
        animationSpec = if (shouldAnimate) {
            AnimationConstants.SPRING_BOUNCY
        } else {
            AnimationConstants.tweenInstant()
        },
        label = "Success checkmark scale"
    )

    // Rotation animation for dynamic entrance
    val rotation by animateFloatAsState(
        targetValue = if (isVisible) 0f else -45f,
        animationSpec = if (shouldAnimate) {
            AnimationConstants.SPRING_SMOOTH
        } else {
            AnimationConstants.tweenInstant()
        },
        label = "Success checkmark rotation"
    )

    // Content description for accessibility
    val contentDesc = stringResource(R.string.cd_success_animation)

    Box(
        modifier = modifier
            .size(size)
            .semantics { this.contentDescription = contentDesc },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = contentDesc,
            tint = tint,
            modifier = Modifier
                .size(size)
                .scale(scale)
                .rotate(rotation)
        )
    }
}

/**
 * Error animation component with shake effect.
 *
 * **Animations:**
 * - Scale pulse (0 → 1.1 → 1.0)
 * - Horizontal shake (3 oscillations)
 * - Fast animation for urgency
 *
 * **Use Cases:**
 * - Download failure
 * - Form validation errors
 * - Sync failures
 * - Connection errors
 *
 * @param isVisible Whether the error icon should be visible
 * @param size Size of the icon
 * @param tint Color of the error icon (defaults to error red)
 * @param modifier Optional modifier
 *
 * ## Example:
 * ```kotlin
 * var showError by remember { mutableStateOf(false) }
 *
 * if (errorOccurred) {
 *     ErrorAnimation(isVisible = true)
 * }
 * ```
 */
@Composable
fun ErrorAnimation(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    tint: Color = MaterialTheme.colorScheme.error
) {
    val shouldAnimate by rememberShouldAnimate()
    var shakePhase by remember { mutableStateOf(0) }

    // Log animation trigger
    LaunchedEffect(isVisible) {
        if (isVisible) {
            Logger.d("ErrorAnimation", "Error animation triggered")
            // Reset shake phase
            shakePhase = 0
        }
    }

    // Scale animation
    val scale by animateFloatAsState(
        targetValue = if (isVisible) AnimationConstants.SCALE_NORMAL else 0f,
        animationSpec = if (shouldAnimate) {
            tween(
                durationMillis = AnimationConstants.DURATION_FAST,
                easing = AnimationConstants.EASING_EMPHASIZED
            )
        } else {
            AnimationConstants.tweenInstant()
        },
        label = "Error icon scale"
    )

    // Shake animation (horizontal oscillation)
    val shakeOffset by animateFloatAsState(
        targetValue = when {
            !isVisible -> 0f
            shakePhase % 6 == 0 -> 0f
            shakePhase % 6 == 1 -> AnimationConstants.ROTATION_SHAKE
            shakePhase % 6 == 2 -> -AnimationConstants.ROTATION_SHAKE
            shakePhase % 6 == 3 -> AnimationConstants.ROTATION_SHAKE * 0.7f
            shakePhase % 6 == 4 -> -AnimationConstants.ROTATION_SHAKE * 0.7f
            else -> 0f
        },
        animationSpec = if (shouldAnimate) {
            tween(
                durationMillis = 100,
                easing = AnimationConstants.EASING_LINEAR
            )
        } else {
            AnimationConstants.tweenInstant()
        },
        finishedListener = {
            if (shakePhase < 6 && isVisible) {
                shakePhase++
            }
        },
        label = "Error shake"
    )

    // Trigger shake sequence
    LaunchedEffect(isVisible) {
        if (isVisible && shouldAnimate) {
            shakePhase = 1
        }
    }

    // Content description for accessibility
    val contentDesc = stringResource(R.string.cd_error_animation)

    Box(
        modifier = modifier
            .size(size)
            .semantics { this.contentDescription = contentDesc },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = contentDesc,
            tint = tint,
            modifier = Modifier
                .size(size)
                .scale(scale)
                .rotate(shakeOffset)
        )
    }
}

/**
 * Animated status text with crossfade transitions.
 *
 * **Animations:**
 * - Crossfade between status messages (300ms)
 * - Smooth fade-out → fade-in
 * - Standard easing for professional feel
 *
 * **Use Cases:**
 * - Download status updates
 * - Progress messages
 * - Loading states
 * - Operation feedback
 *
 * @param statusText Current status message to display
 * @param modifier Optional modifier
 * @param textStyle Text style to apply
 *
 * ## Example:
 * ```kotlin
 * var status by remember { mutableStateOf("Initializing...") }
 *
 * AnimatedStatusText(statusText = status)
 *
 * LaunchedEffect(Unit) {
 *     delay(1000)
 *     status = "Connecting..."
 *     delay(1000)
 *     status = "Downloading..."
 * }
 * ```
 */
@Composable
fun AnimatedStatusText(
    statusText: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium
) {
    val shouldAnimate by rememberShouldAnimate()

    // Log status changes
    LaunchedEffect(statusText) {
        Logger.d("AnimatedStatusText", "Status changed: $statusText")
    }

    AnimatedContent(
        targetState = statusText,
        transitionSpec = {
            if (shouldAnimate) {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = AnimationConstants.DURATION_NORMAL,
                        easing = AnimationConstants.EASING_STANDARD
                    )
                ) togetherWith fadeOut(
                    animationSpec = tween(
                        durationMillis = AnimationConstants.DURATION_NORMAL,
                        easing = AnimationConstants.EASING_STANDARD
                    )
                )
            } else {
                fadeIn(
                    animationSpec = tween(durationMillis = 0)
                ) togetherWith fadeOut(
                    animationSpec = tween(durationMillis = 0)
                )
            }
        },
        label = "Status text crossfade",
        modifier = modifier
    ) { text ->
        Text(
            text = text,
            style = textStyle,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Pulsing indicator for active/loading states.
 *
 * **Animations:**
 * - Infinite scale pulse (1.0 ↔ 1.2)
 * - Alpha pulse (0.6 ↔ 1.0)
 * - Smooth continuous animation
 *
 * **Use Cases:**
 * - Active downloads
 * - Location tracking active
 * - Syncing in progress
 * - Waiting for GPS
 *
 * @param isActive Whether the pulsing should be active
 * @param content The composable content to pulse
 * @param modifier Optional modifier
 *
 * ## Example:
 * ```kotlin
 * PulsingIndicator(isActive = isDownloading) {
 *     Icon(Icons.Default.Download, contentDescription = "Downloading")
 * }
 * ```
 */
@Composable
fun PulsingIndicator(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shouldAnimate by rememberShouldAnimate()

    // Pulsing scale animation
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(
        label = "pulse transition"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive && shouldAnimate) 1.2f else 1f,
        animationSpec = if (isActive && shouldAnimate) {
            androidx.compose.animation.core.infiniteRepeatable(
                animation = tween(
                    durationMillis = 1000,
                    easing = AnimationConstants.EASING_STANDARD
                ),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
            )
        } else {
            androidx.compose.animation.core.infiniteRepeatable(
                animation = tween(durationMillis = 0)
            )
        },
        label = "pulse scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive && shouldAnimate) 0.6f else 1f,
        animationSpec = if (isActive && shouldAnimate) {
            androidx.compose.animation.core.infiniteRepeatable(
                animation = tween(
                    durationMillis = 1000,
                    easing = AnimationConstants.EASING_STANDARD
                ),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
            )
        } else {
            androidx.compose.animation.core.infiniteRepeatable(
                animation = tween(durationMillis = 0)
            )
        },
        label = "pulse alpha"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .then(
                if (isActive) Modifier.semantics {
                    contentDescription = "Active indicator pulsing"
                } else Modifier
            )
    ) {
        content()
    }
}
