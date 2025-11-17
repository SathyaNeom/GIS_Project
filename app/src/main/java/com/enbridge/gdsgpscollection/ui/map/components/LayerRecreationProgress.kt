package com.enbridge.gdsgpscollection.ui.map.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.enbridge.gdsgpscollection.R
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing
import kotlinx.coroutines.delay

/**
 * Progress indicator for layer recreation operations.
 *
 * Only shown if operation takes longer than 500ms to prevent
 * flickering for fast operations (cached layers).
 *
 * Architecture Pattern: Delayed Visibility Pattern
 * - Prevents UI flashing for fast operations (<500ms)
 * - Smooth fade-in/fade-out animations
 * - Centered overlay with semi-transparent background
 *
 * Performance Considerations:
 * - Cache hit (fast): Progress never shows (~50ms operation)
 * - Cache miss (slow): Progress shows after 500ms delay
 * - Provides visual feedback only when needed
 *
 * @param isRecreating Whether layer recreation is in progress
 * @param modifier Modifier for the composable
 *
 * @author Sathya Narayanan
 * @since 1.0.0
 */
@Composable
fun LayerRecreationProgress(
    isRecreating: Boolean,
    modifier: Modifier = Modifier
) {
    var showProgress by remember { mutableStateOf(false) }

    LaunchedEffect(isRecreating) {
        if (isRecreating) {
            // Delay 500ms before showing indicator
            delay(500)
            showProgress = true
        } else {
            showProgress = false
        }
    }

    AnimatedVisibility(
        visible = showProgress,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(Spacing.large),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(Spacing.small))

                Text(
                    text = stringResource(R.string.msg_updating_map_layers),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
