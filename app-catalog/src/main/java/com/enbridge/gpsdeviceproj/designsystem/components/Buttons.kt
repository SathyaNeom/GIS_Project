package com.enbridge.gpsdeviceproj.designsystem.components

/**
 * @author Sathya Narayanan
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enbridge.gpsdeviceproj.designsystem.theme.ElectronicServicesTheme
import com.enbridge.gpsdeviceproj.designsystem.theme.MinTouchTargetSize
import com.enbridge.gpsdeviceproj.designsystem.theme.Spacing
import com.enbridge.gpsdeviceproj.designsystem.theme.md_theme_light_progressUnfilled
import com.enbridge.gpsdeviceproj.designsystem.theme.md_theme_dark_progressUnfilled

/**
 * Primary button with filled style using the primary theme color
 * Uses medium corner radius (8.dp) for consistent brand identity
 * Minimum touch target of 48dp for accessibility
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(MinTouchTargetSize),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f)
        ),
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(horizontal = Spacing.normal, vertical = Spacing.medium)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(Spacing.small))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Secondary button with outlined style using the secondary theme color
 * Provides a less prominent alternative to the primary button
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(MinTouchTargetSize),
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.secondary,
            disabledContentColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.38f)
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 2.dp
        ),
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(horizontal = Spacing.normal, vertical = Spacing.medium)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(Spacing.small))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Text button for low-emphasis actions like "Cancel" in dialogs
 * Provides minimal visual weight while remaining accessible
 */
@Composable
fun AppTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Icon button with circular or square background for toolbar actions
 * Ensures ripple effect for user feedback and meets accessibility standards
 */
@Composable
fun AppIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(MinTouchTargetSize)
            .semantics { this.contentDescription = contentDescription },
        enabled = enabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            }
        )
    }
}

/**
 * Floating Action Button with primary or secondary color
 * Features subtle elevation shadow for prominence
 * Used for the primary action on a screen
 */
@Composable
fun AppFloatingActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    useSecondaryColor: Boolean = false
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = if (useSecondaryColor) {
            MaterialTheme.colorScheme.secondary
        } else {
            MaterialTheme.colorScheme.primary
        },
        contentColor = if (useSecondaryColor) {
            MaterialTheme.colorScheme.onSecondary
        } else {
            MaterialTheme.colorScheme.onPrimary
        },
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 12.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )
    }
}

/**
 * Direction for progress fill animation
 */
enum class ProgressDirection {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT
}

/**
 * Progress button that visually fills with color based on progress state
 * Shows loading progress with a horizontal fill from left to right (or right to left)
 *
 * @param text Button text to display
 * @param onClick Action to perform when button is clicked
 * @param modifier Modifier to be applied to the button
 * @param enabled Whether the button is enabled (interactive)
 * @param isLoading Whether the button is in loading/progress state
 * @param progress Current progress value between 0.0 and 1.0
 * @param icon Optional icon to display alongside text
 * @param showPercentage Whether to show percentage text during loading
 * @param direction Direction of progress fill (LEFT_TO_RIGHT or RIGHT_TO_LEFT)
 */
@Composable
fun ProgressButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    progress: Float = 0f,
    icon: ImageVector? = null,
    showPercentage: Boolean = true,
    direction: ProgressDirection = ProgressDirection.LEFT_TO_RIGHT
) {
    val progressUnfilledColor = if (MaterialTheme.colorScheme.background == Color.White ||
        MaterialTheme.colorScheme.background.red > 0.5f
    ) {
        md_theme_light_progressUnfilled
    } else {
        md_theme_dark_progressUnfilled
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(MinTouchTargetSize)
            .clip(MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center
    ) {
        // Background layer showing progress fill
        if (isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                val filledWeight = progress.coerceIn(0f, 1f)
                val unfilledWeight = 1f - filledWeight

                when (direction) {
                    ProgressDirection.LEFT_TO_RIGHT -> {
                        // Filled portion (left side)
                        if (filledWeight > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(filledWeight)
                                    .height(MinTouchTargetSize)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                        // Unfilled portion (right side)
                        if (unfilledWeight > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(unfilledWeight)
                                    .height(MinTouchTargetSize)
                                    .background(progressUnfilledColor)
                            )
                        }
                    }

                    ProgressDirection.RIGHT_TO_LEFT -> {
                        // Unfilled portion (left side)
                        if (unfilledWeight > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(unfilledWeight)
                                    .height(MinTouchTargetSize)
                                    .background(progressUnfilledColor)
                            )
                        }
                        // Filled portion (right side)
                        if (filledWeight > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(filledWeight)
                                    .height(MinTouchTargetSize)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }
        } else {
            // Normal state background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MinTouchTargetSize)
                    .background(
                        if (enabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
                    )
            )
        }

        // Button content layer (text, icon, percentage)
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(MinTouchTargetSize),
            enabled = enabled && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f)
            ),
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(horizontal = Spacing.normal, vertical = Spacing.medium),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp,
                hoveredElevation = 0.dp,
                focusedElevation = 0.dp
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(Spacing.small))
                }

                if (isLoading && showPercentage) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge
                    )
                } else {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryButtonPreview() {
    ElectronicServicesTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PrimaryButton(
                text = "Primary Button",
                onClick = { }
            )
            PrimaryButton(
                text = "With Icon",
                onClick = { },
                icon = Icons.Filled.Check
            )
            PrimaryButton(
                text = "Disabled",
                onClick = { },
                enabled = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SecondaryButtonPreview() {
    ElectronicServicesTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SecondaryButton(
                text = "Secondary Button",
                onClick = { }
            )
            SecondaryButton(
                text = "With Icon",
                onClick = { },
                icon = Icons.Filled.Info
            )
            SecondaryButton(
                text = "Disabled",
                onClick = { },
                enabled = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppTextButtonPreview() {
    ElectronicServicesTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppTextButton(
                text = "Cancel",
                onClick = { }
            )
            AppTextButton(
                text = "Disabled",
                onClick = { },
                enabled = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppIconButtonPreview() {
    ElectronicServicesTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppIconButton(
                icon = Icons.Filled.Favorite,
                contentDescription = "Favorite",
                onClick = { }
            )
            AppIconButton(
                icon = Icons.Filled.Share,
                contentDescription = "Share",
                onClick = { }
            )
            AppIconButton(
                icon = Icons.Filled.Delete,
                contentDescription = "Delete",
                onClick = { },
                enabled = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppFloatingActionButtonPreview() {
    ElectronicServicesTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppFloatingActionButton(
                icon = Icons.Filled.Add,
                contentDescription = "Add",
                onClick = { }
            )
            AppFloatingActionButton(
                icon = Icons.Filled.Edit,
                contentDescription = "Edit",
                onClick = { },
                useSecondaryColor = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgressButtonPreview() {
    ElectronicServicesTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Normal state
            ProgressButton(
                text = "Get Data",
                onClick = { },
                icon = Icons.Filled.Check
            )

            // Loading at 0%
            ProgressButton(
                text = "Get Data",
                onClick = { },
                icon = Icons.Filled.Check,
                isLoading = true,
                progress = 0f
            )

            // Loading at 35%
            ProgressButton(
                text = "Get Data",
                onClick = { },
                icon = Icons.Filled.Check,
                isLoading = true,
                progress = 0.35f
            )

            // Loading at 65%
            ProgressButton(
                text = "Get Data",
                onClick = { },
                icon = Icons.Filled.Check,
                isLoading = true,
                progress = 0.65f
            )

            // Loading at 100%
            ProgressButton(
                text = "Get Data",
                onClick = { },
                icon = Icons.Filled.Check,
                isLoading = true,
                progress = 1f
            )

            // Right to left direction at 50%
            ProgressButton(
                text = "Get Data",
                onClick = { },
                icon = Icons.Filled.Check,
                isLoading = true,
                progress = 0.5f,
                direction = ProgressDirection.RIGHT_TO_LEFT
            )

            // Disabled state
            ProgressButton(
                text = "Get Data",
                onClick = { },
                icon = Icons.Filled.Check,
                enabled = false
            )
        }
    }
}
