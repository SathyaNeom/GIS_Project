package com.enbridge.gdsgpscollection.ui.map.components.collectes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.enbridge.gdsgpscollection.designsystem.components.AppTextField
import com.enbridge.gdsgpscollection.designsystem.components.SingleSelectDropdown
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing
import com.enbridge.gdsgpscollection.ui.map.models.AttributeType
import com.enbridge.gdsgpscollection.ui.map.models.FeatureAttribute

/**
 * Attribute Field component that renders different input types.
 *
 * Supports multiple attribute types:
 * - TEXT: Single-line text input
 * - TEXTMULTILINE: Multi-line text input
 * - NUMBER: Numeric input
 * - DROPDOWN: Single-select dropdown
 * - DATE: Date picker input
 * - LOCATION: Location indicator (read-only)
 *
 * @param attribute The attribute definition
 * @param value Current value of the attribute
 * @param onValueChange Callback when value changes
 * @param modifier Optional modifier
 */
@Composable
fun AttributeField(
    attribute: FeatureAttribute,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        when (attribute.type) {
            AttributeType.LOCATION -> {
                // Location field with icon (read-only)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (attribute.isRequired) "${attribute.label} * :" else "${attribute.label} :",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            AttributeType.DROPDOWN -> {
                Text(
                    text = if (attribute.isRequired) "${attribute.label} * :" else "${attribute.label} :",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Spacing.extraSmall))
                SingleSelectDropdown(
                    items = attribute.options,
                    selectedItem = if (value.isEmpty()) null else value,
                    onItemSelected = onValueChange,
                    label = if (attribute.hint.isNotEmpty()) attribute.hint else attribute.label,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AttributeType.TEXT, AttributeType.TEXTMULTILINE, AttributeType.NUMBER -> {
                AppTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = if (attribute.isRequired) "${attribute.label} * :" else "${attribute.label} :",
                    placeholder = attribute.hint.takeIf { it.isNotEmpty() },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = attribute.type != AttributeType.TEXTMULTILINE,
                    maxLines = if (attribute.type == AttributeType.TEXTMULTILINE) 4 else 1
                )
            }

            AttributeType.DATE -> {
                AppTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = if (attribute.isRequired) "${attribute.label} * :" else "${attribute.label} :",
                    placeholder = "MM/DD/YYYY",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
