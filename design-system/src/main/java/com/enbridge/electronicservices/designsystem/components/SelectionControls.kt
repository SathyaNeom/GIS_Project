package com.enbridge.electronicservices.designsystem.components

/**
 * @author Sathya Narayanan
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enbridge.electronicservices.designsystem.theme.ElectronicServicesTheme
import com.enbridge.electronicservices.designsystem.theme.MinTouchTargetSize
import com.enbridge.electronicservices.designsystem.theme.Spacing

/**
 * Custom Checkbox with primary theme color when checked
 * Meets accessibility standards with proper touch targets
 */
@Composable
fun AppCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null
) {
    val checkboxModifier = if (label != null) {
        modifier.toggleable(
            value = checked,
            enabled = enabled,
            role = Role.Checkbox,
            onValueChange = { onCheckedChange?.invoke(it) }
        )
    } else {
        modifier
    }

    Row(
        modifier = checkboxModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = if (label == null) onCheckedChange else null,
            enabled = enabled,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                disabledCheckedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                disabledUncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        )

        if (label != null) {
            Spacer(modifier = Modifier.width(Spacing.small))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
        }
    }
}

/**
 * Custom RadioButton with primary theme color when selected
 * Used in radio button groups for single selection
 */
@Composable
fun AppRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null
) {
    val radioModifier = if (label != null) {
        modifier.selectable(
            selected = selected,
            enabled = enabled,
            role = Role.RadioButton,
            onClick = { onClick?.invoke() }
        )
    } else {
        modifier
    }

    Row(
        modifier = radioModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = if (label == null) onClick else null,
            enabled = enabled,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledSelectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                disabledUnselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        )

        if (label != null) {
            Spacer(modifier = Modifier.width(Spacing.small))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
        }
    }
}

/**
 * Custom Chip for filtering and selection
 * Clear visual states for selected and unselected
 * Selected state uses primary color for emphasis
 */
@Composable
fun AppChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge
            )
        },
        modifier = modifier,
        enabled = enabled,
        leadingIcon = if (leadingIcon != null) {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface,
            iconColor = MaterialTheme.colorScheme.onSurface,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.38f),
            disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        border = if (!selected) {
            FilterChipDefaults.filterChipBorder(
                borderColor = MaterialTheme.colorScheme.outline,
                selectedBorderColor = MaterialTheme.colorScheme.primary,
                disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                enabled = enabled,
                selected = selected
            )
        } else null
    )
}

@Preview(showBackground = true)
@Composable
private fun AppCheckboxPreview() {
    ElectronicServicesTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var checked1 by remember { mutableStateOf(false) }
            var checked2 by remember { mutableStateOf(true) }

            AppCheckbox(
                checked = checked1,
                onCheckedChange = { checked1 = it },
                label = "Unchecked Checkbox"
            )
            AppCheckbox(
                checked = checked2,
                onCheckedChange = { checked2 = it },
                label = "Checked Checkbox"
            )
            AppCheckbox(
                checked = true,
                onCheckedChange = { },
                label = "Disabled",
                enabled = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppRadioButtonPreview() {
    ElectronicServicesTheme {
        var selectedOption by remember { mutableStateOf("option1") }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppRadioButton(
                selected = selectedOption == "option1",
                onClick = { selectedOption = "option1" },
                label = "Option 1"
            )
            AppRadioButton(
                selected = selectedOption == "option2",
                onClick = { selectedOption = "option2" },
                label = "Option 2"
            )
            AppRadioButton(
                selected = selectedOption == "option3",
                onClick = { selectedOption = "option3" },
                label = "Option 3"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppChipPreview() {
    ElectronicServicesTheme {
        var chip1 by remember { mutableStateOf(false) }
        var chip2 by remember { mutableStateOf(true) }

        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppChip(
                label = "Unselected",
                selected = chip1,
                onClick = { chip1 = !chip1 },
                leadingIcon = Icons.Filled.FilterList
            )
            AppChip(
                label = "Selected",
                selected = chip2,
                onClick = { chip2 = !chip2 },
                leadingIcon = Icons.Filled.Star
            )
        }
    }
}
