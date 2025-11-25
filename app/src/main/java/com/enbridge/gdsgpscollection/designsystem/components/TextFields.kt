package com.enbridge.gdsgpscollection.designsystem.components

/**
 * @author Sathya Narayanan
 */

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enbridge.gdsgpscollection.R
import com.enbridge.gdsgpscollection.designsystem.theme.AnimationConstants
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing
import com.enbridge.gdsgpscollection.util.Logger
import com.enbridge.gdsgpscollection.util.rememberShouldAnimate

/**
 * Reusable OutlinedTextField with comprehensive customization options.
 *
 * **Animations:**
 * - Border color: Smooth transition when error state changes (300ms)
 * - Icon color: Animated transition matching border
 * - Uses standard easing for professional feel
 *
 * **Accessibility:**
 * - Respects reduced motion preferences
 * - Proper error announcements for screen readers
 * - Semantic content descriptions
 *
 * Accepts parameters for label, placeholder, icons, and validation feedback.
 * Focused border and label use the primary theme color.
 * Supports accessibility with proper error states and descriptions.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val shouldAnimate by rememberShouldAnimate()

    // Log error state changes
    LaunchedEffect(isError) {
        if (isError) {
            Logger.d("AppTextField", "Field validation error: ${label ?: "unnamed field"}")
        }
    }

    // Animated border colors for error state
    val errorBorderColor by animateColorAsState(
        targetValue = if (isError) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.outline
        },
        animationSpec = if (shouldAnimate) {
            tween(
                durationMillis = AnimationConstants.DURATION_NORMAL,
                easing = AnimationConstants.EASING_STANDARD
            )
        } else {
            AnimationConstants.tweenInstant()
        },
        label = "TextField border color"
    )

    // Animated icon colors
    val iconColor by animateColorAsState(
        targetValue = if (isError) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = if (shouldAnimate) {
            tween(
                durationMillis = AnimationConstants.DURATION_NORMAL,
                easing = AnimationConstants.EASING_STANDARD
            )
        } else {
            AnimationConstants.tweenInstant()
        },
        label = "TextField icon color"
    )

    // Content description for accessibility
    val contentDesc = if (isError) {
        stringResource(R.string.cd_field_error)
    } else {
        stringResource(R.string.cd_field_valid)
    }

    Column(modifier = modifier.semantics { this.contentDescription = contentDesc }) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = readOnly,
            label = label?.let { { Text(it) } },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = if (isError) iconColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            trailingIcon = trailingIcon?.let {
                {
                    if (onTrailingIconClick != null) {
                        IconButton(onClick = onTrailingIconClick) {
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                tint = if (isError) iconColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = if (isError) iconColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            supportingText = if (supportingText != null) {
                {
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            } else null,
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (isError) errorBorderColor else MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedLeadingIconColor = iconColor,
                focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedTrailingIconColor = iconColor,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error,
                errorLeadingIconColor = MaterialTheme.colorScheme.error,
                errorTrailingIconColor = MaterialTheme.colorScheme.error,
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            ),
            shape = MaterialTheme.shapes.small
        )
    }
}

/**
 * Backward compatibility - Labeled TextField with simplified parameters
 */
@Composable
fun LabeledTextField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    placeholder: String? = null
) {
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        isError = isError,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        maxLines = maxLines,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions
    )
}

@Preview(showBackground = true)
@Composable
private fun AppTextFieldPreview() {
    GdsGpsCollectionTheme {
        var text1 by remember { mutableStateOf("") }
        var text2 by remember { mutableStateOf("Sample Text") }
        var text3 by remember { mutableStateOf("error@example.com") }
        var password by remember { mutableStateOf("") }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppTextField(
                value = text1,
                onValueChange = { text1 = it },
                label = "Label",
                placeholder = "Enter text here"
            )

            AppTextField(
                value = text2,
                onValueChange = { text2 = it },
                label = "Username",
                placeholder = "Enter username",
                leadingIcon = Icons.Filled.Person
            )

            AppTextField(
                value = text3,
                onValueChange = { text3 = it },
                label = "Email",
                placeholder = "Enter email",
                leadingIcon = Icons.Filled.Email,
                isError = true,
                supportingText = "Invalid email format"
            )

            AppTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "Enter password",
                leadingIcon = Icons.Filled.Lock,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
            )

            AppTextField(
                value = "Disabled field",
                onValueChange = { },
                label = "Disabled",
                enabled = false
            )
        }
    }
}
