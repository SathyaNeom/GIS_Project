package com.enbridge.gpsdeviceproj.designsystem.components

/**
 * @author Sathya Narayanan
 */

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enbridge.gpsdeviceproj.designsystem.theme.ElectronicServicesTheme
import com.enbridge.gpsdeviceproj.designsystem.theme.Spacing

/**
 * Reusable OutlinedTextField with comprehensive customization options
 * Accepts parameters for label, placeholder, icons, and validation feedback
 * Focused border and label use the primary theme color
 * Supports accessibility with proper error states and descriptions
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
    Column(modifier = modifier) {
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
                        contentDescription = null
                    )
                }
            },
            trailingIcon = trailingIcon?.let {
                {
                    if (onTrailingIconClick != null) {
                        IconButton(onClick = onTrailingIconClick) {
                            Icon(
                                imageVector = it,
                                contentDescription = null
                            )
                        }
                    } else {
                        Icon(
                            imageVector = it,
                            contentDescription = null
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
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
    ElectronicServicesTheme {
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
