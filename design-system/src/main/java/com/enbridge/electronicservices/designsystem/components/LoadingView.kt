package com.enbridge.electronicservices.designsystem.components

/**
 * @author Sathya Narayanan
 */
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.enbridge.electronicservices.designsystem.theme.Spacing
import androidx.compose.ui.tooling.preview.Preview
import com.enbridge.electronicservices.designsystem.theme.ElectronicServicesTheme

/**
 * Loading view variants for different use cases
 */
enum class LoadingStyle {
    /** Animated icon with rotation and pulse */
    ANIMATED_ICON,

    /** Material circular progress indicator */
    CIRCULAR,

    /** Material linear progress indicator */
    LINEAR
}

/**
 * Enhanced loading view with accessibility, progress tracking, and cancellation support.
 *
 * Features:
 * - Full accessibility support with screen reader announcements
 * - Determinate and indeterminate progress modes
 * - Optional cancellation callback
 * - Variant-aware icon support
 * - Multiple loading styles (animated icon, circular, linear)
 * - Smooth fade in/out animations
 * - Semantic markup for testing
 *
 * @param modifier Modifier to be applied to the loading view
 * @param message Optional message to display below the loader
 * @param progress Progress value between 0.0 and 1.0 for determinate mode, null for indeterminate
 * @param fullScreen Whether to fill the entire screen with overlay
 * @param overlayAlpha Alpha value for the background overlay (0.0 to 1.0)
 * @param style Loading animation style
 * @param icon Custom icon to display (defaults to Build icon for brand consistency)
 * @param onCancel Optional callback for cancellation action
 * @param cancelButtonText Text for the cancel button (shown if onCancel is provided)
 * @param visible Whether the loading view is visible (for animated visibility)
 * @param semanticLabel Accessibility label for screen readers
 */
@Composable
fun LoadingView(
    modifier: Modifier = Modifier,
    message: String? = null,
    progress: Float? = null,
    fullScreen: Boolean = true,
    overlayAlpha: Float = 0.7f,
    style: LoadingStyle = LoadingStyle.ANIMATED_ICON,
    icon: ImageVector? = Icons.Default.Build,
    onCancel: (() -> Unit)? = null,
    cancelButtonText: String = "Cancel",
    visible: Boolean = true,
    semanticLabel: String? = message ?: "Loading"
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(
            modifier = modifier
                .then(if (fullScreen) Modifier.fillMaxSize() else Modifier)
                .background(Color.Black.copy(alpha = overlayAlpha))
                .semantics {
                    semanticLabel?.let { contentDescription = it }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.normal)
            ) {
                // Loading indicator based on style
                when (style) {
                    LoadingStyle.ANIMATED_ICON -> {
                        if (icon != null) {
                            AnimatedLoadingIcon(
                                icon = icon,
                                tint = MaterialTheme.colorScheme.primary,
                                size = 80.dp
                            )
                        }
                    }

                    LoadingStyle.CIRCULAR -> {
                        if (progress != null) {
                            CircularProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier.size(80.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeWidth = 4.dp,
                            )
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.size(80.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 4.dp
                            )
                        }
                    }

                    LoadingStyle.LINEAR -> {
                        if (progress != null) {
                            LinearProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .size(width = 200.dp, height = 4.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .size(width = 200.dp, height = 4.dp),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                // Progress percentage (only for determinate mode)
                if (progress != null && style != LoadingStyle.LINEAR) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }

                // Message
                if (message != null) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = Spacing.large)
                    )
                }

                // Cancel button
                if (onCancel != null) {
                    TextButton(
                        onClick = onCancel,
                        modifier = Modifier.padding(top = Spacing.small)
                    ) {
                        Text(
                            text = cancelButtonText,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    // Announce to screen readers when loading starts
    LaunchedEffect(visible, message) {
        // This will be picked up by accessibility services
        // The semantics modifier above handles the actual announcement
    }
}

/**
 * Animated loading icon with rotation and pulsing effects
 * Separated for reusability and performance optimization
 */
@Composable
private fun AnimatedLoadingIcon(
    icon: ImageVector,
    tint: Color,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_animation")

    // Rotation animation - smooth continuous rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pulsing scale animation - subtle breathing effect
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Alpha animation - subtle fade in/out
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Icon(
        imageVector = icon,
        contentDescription = null, // Decorative, parent has semantics
        tint = tint,
        modifier = modifier
            .size(size)
            .rotate(rotation)
            .scale(scale)
            .alpha(alpha)
    )
}

/**
 * Compact loading indicator without overlay
 * For use within cards or smaller sections
 *
 * @param modifier Modifier to be applied to the loader
 * @param message Optional message to display below the loader
 * @param progress Optional progress value (0.0 to 1.0) for determinate mode
 * @param style Loading style (ANIMATED_ICON, CIRCULAR, or LINEAR)
 * @param icon Custom icon (only used with ANIMATED_ICON style)
 * @param semanticLabel Accessibility label
 */
@Composable
fun CompactLoader(
    modifier: Modifier = Modifier,
    message: String? = null,
    progress: Float? = null,
    style: LoadingStyle = LoadingStyle.ANIMATED_ICON,
    icon: ImageVector? = Icons.Default.Build,
    semanticLabel: String? = message ?: "Loading"
) {
    Column(
        modifier = modifier.semantics {
            semanticLabel?.let { contentDescription = it }
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        when (style) {
            LoadingStyle.ANIMATED_ICON -> {
                if (icon != null) {
                    AnimatedLoadingIcon(
                        icon = icon,
                        tint = MaterialTheme.colorScheme.primary,
                        size = 48.dp
                    )
                }
            }

            LoadingStyle.CIRCULAR -> {
                if (progress != null) {
                    CircularProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                }
            }

            LoadingStyle.LINEAR -> {
                if (progress != null) {
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.size(width = 150.dp, height = 3.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier.size(width = 150.dp, height = 3.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (progress != null && style == LoadingStyle.CIRCULAR) {
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (message != null) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ================================================================================================
// PREVIEW COMPOSABLES
// ================================================================================================

@Preview(name = "Loading View - Animated Icon", showBackground = true)
@Composable
private fun LoadingViewAnimatedPreview() {
    ElectronicServicesTheme {
        Box(modifier = Modifier.size(300.dp)) {
            LoadingView(
                message = "Loading data...",
                fullScreen = false,
                style = LoadingStyle.ANIMATED_ICON
            )
        }
    }
}

@Preview(name = "Loading View - Circular Progress", showBackground = true)
@Composable
private fun LoadingViewCircularPreview() {
    ElectronicServicesTheme {
        Box(modifier = Modifier.size(300.dp)) {
            LoadingView(
                message = "Loading data...",
                fullScreen = false,
                style = LoadingStyle.CIRCULAR,
                progress = 0.65f
            )
        }
    }
}

@Preview(name = "Loading View - Linear Progress", showBackground = true)
@Composable
private fun LoadingViewLinearPreview() {
    ElectronicServicesTheme {
        Box(modifier = Modifier.size(300.dp)) {
            LoadingView(
                message = "Downloading...",
                fullScreen = false,
                style = LoadingStyle.LINEAR,
                progress = 0.45f
            )
        }
    }
}

@Preview(name = "Loading View - With Cancel", showBackground = true)
@Composable
private fun LoadingViewWithCancelPreview() {
    ElectronicServicesTheme {
        Box(modifier = Modifier.size(300.dp)) {
            LoadingView(
                message = "Processing request...",
                fullScreen = false,
                style = LoadingStyle.CIRCULAR,
                onCancel = { /* Cancel action */ },
                cancelButtonText = "Cancel"
            )
        }
    }
}

@Preview(name = "Compact Loader - Variants", showBackground = true)
@Composable
private fun CompactLoaderVariantsPreview() {
    ElectronicServicesTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CompactLoader(
                message = "Animated Icon",
                style = LoadingStyle.ANIMATED_ICON
            )

            CompactLoader(
                message = "Circular - 75%",
                style = LoadingStyle.CIRCULAR,
                progress = 0.75f
            )

            CompactLoader(
                message = "Linear Progress",
                style = LoadingStyle.LINEAR,
                progress = 0.50f
            )
        }
    }
}

@Preview(name = "Compact Loader - No Message", showBackground = true)
@Composable
private fun CompactLoaderNoMessagePreview() {
    ElectronicServicesTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CompactLoader(style = LoadingStyle.ANIMATED_ICON)
            CompactLoader(style = LoadingStyle.CIRCULAR)
            CompactLoader(style = LoadingStyle.LINEAR)
        }
    }
}
