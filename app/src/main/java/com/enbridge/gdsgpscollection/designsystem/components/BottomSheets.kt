package com.enbridge.gdsgpscollection.designsystem.components

/**
 * @author Sathya Narayanan
 */
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.enbridge.gdsgpscollection.R
import com.enbridge.gdsgpscollection.designsystem.theme.AnimationConstants
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing
import androidx.compose.ui.tooling.preview.Preview
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme
import com.enbridge.gdsgpscollection.util.Logger
import com.enbridge.gdsgpscollection.util.rememberShouldAnimate
import kotlinx.coroutines.delay

/**
 * Custom Modal Bottom Sheet with app theme styling and enhanced animations.
 *
 * **Animations:**
 * - Drag handle: Subtle pulse on first appearance (attracts user attention)
 * - Content: Staggered fade-in for professional polish
 * - Sheet: Spring-based slide animation (Material 3 default enhanced)
 *
 * **Accessibility:**
 * - Respects system animation settings
 * - Proper content descriptions for screen readers
 * - Fallback to instant appearance if animations disabled
 *
 * Ensures content uses defined typography and components.
 * Drag handle is clearly visible with animation cue.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shouldAnimate by rememberShouldAnimate()

    // Animation state for content
    var contentVisible by remember { mutableStateOf(false) }

    // Trigger content fade-in with delay
    LaunchedEffect(Unit) {
        Logger.d("AppBottomSheet", "Bottom sheet animation triggered")
        if (shouldAnimate) {
            delay(AnimationConstants.DURATION_FAST.toLong()) // Wait for sheet to appear
        }
        contentVisible = true
    }

    // Content fade animation
    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) AnimationConstants.ALPHA_VISIBLE else AnimationConstants.ALPHA_INVISIBLE,
        animationSpec = if (shouldAnimate) {
            AnimationConstants.tweenNormal()
        } else {
            AnimationConstants.tweenInstant()
        },
        label = "Bottom sheet content alpha"
    )

    // Drag handle pulse animation (subtle)
    var handlePulseComplete by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (shouldAnimate) {
            delay(300L) // Initial delay
            handlePulseComplete = true
        } else {
            handlePulseComplete = true
        }
    }

    val handleScale by animateFloatAsState(
        targetValue = if (handlePulseComplete) AnimationConstants.SCALE_NORMAL else AnimationConstants.SCALE_HOVERED,
        animationSpec = if (shouldAnimate) {
            AnimationConstants.SPRING_SMOOTH
        } else {
            AnimationConstants.tweenInstant()
        },
        label = "Drag handle scale"
    )

    // Content description for accessibility
    val contentDesc = stringResource(R.string.cd_bottom_sheet_expanding)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier.semantics { this.contentDescription = contentDesc },
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.extraLarge,
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(Spacing.small))
                // Custom drag handle with subtle pulse animation
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp)
                        .padding(horizontal = Spacing.extraSmall)
                        .scale(handleScale),
                    contentAlignment = Alignment.Center
                ) {
                    HorizontalDivider(
                        modifier = Modifier
                            .width(32.dp)
                            .height(4.dp),
                        thickness = 4.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.small))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.large)
                .padding(bottom = Spacing.large)
                .alpha(contentAlpha)
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Spacing.normal))
            }

            content()
        }
    }
}

/**
 * Bottom Sheet with action buttons
 * Convenience wrapper for common use case with confirm/dismiss actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionBottomSheet(
    onDismissRequest: () -> Unit,
    title: String,
    confirmButtonText: String,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    dismissButtonText: String? = "Cancel",
    onDismissClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    AppBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        title = title
    ) {
        content()

        Spacer(modifier = Modifier.height(Spacing.large))

        // Action buttons
        PrimaryButton(
            text = confirmButtonText,
            onClick = onConfirmClick,
            modifier = Modifier.fillMaxWidth()
        )

        if (dismissButtonText != null) {
            Spacer(modifier = Modifier.height(Spacing.small))
            SecondaryButton(
                text = dismissButtonText,
                onClick = onDismissClick ?: onDismissRequest,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun AppBottomSheetPreview() {
    GdsGpsCollectionTheme {
        Surface {
            Text(
                text = "Preview: Bottom sheet would appear here",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun ActionBottomSheetPreview() {
    GdsGpsCollectionTheme {
        Surface {
            Text(
                text = "Preview: Action bottom sheet would appear here",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
