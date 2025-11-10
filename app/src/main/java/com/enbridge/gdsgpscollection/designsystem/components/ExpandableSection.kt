package com.enbridge.gdsgpscollection.designsystem.components

/**
 * @author Sathya Narayanan
 */

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing

/**
 * Expandable section component with animated expansion/collapse.
 *
 * Displays a header that can be clicked to expand/collapse content below.
 * Uses smooth animations for better UX.
 *
 * @param expanded Current expansion state
 * @param onExpandedChange Callback invoked when expansion state changes
 * @param header Composable content for the section header
 * @param modifier Optional modifier for the section
 * @param content Composable content that is shown/hidden based on expansion state
 */
@Composable
fun ExpandableSection(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Header - clickable to expand/collapse
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(!expanded) },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Custom header content
            header()

            // Expand/collapse indicator
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Expandable content with animation
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            content()
        }
    }
}

/**
 * Expandable section with icon button for expansion control.
 *
 * Similar to ExpandableSection but uses an IconButton instead of making
 * the entire header clickable. Useful when the header contains other
 * interactive elements.
 *
 * @param expanded Current expansion state
 * @param onExpandedChange Callback invoked when expansion state changes
 * @param header Composable content for the section header
 * @param modifier Optional modifier for the section
 * @param content Composable content that is shown/hidden based on expansion state
 */
@Composable
fun ExpandableSectionWithButton(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Header with separate expand button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Custom header content
            header()

            // Expand/collapse button
            IconButton(onClick = { onExpandedChange(!expanded) }) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Expandable content with animation
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            content()
        }
    }
}

// ============================================================================
// Preview Composables
// ============================================================================

@Preview(showBackground = true)
@Composable
private fun ExpandableSectionPreview() {
    GdsGpsCollectionTheme {
        var expanded by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.padding(Spacing.normal),
            verticalArrangement = Arrangement.spacedBy(Spacing.normal)
        ) {
            ExpandableSection(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                header = {
                    Text(
                        text = "Expandable Section",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.normal)
                ) {
                    Text("Content line 1")
                    Text("Content line 2")
                    Text("Content line 3")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpandableSectionWithButtonPreview() {
    GdsGpsCollectionTheme {
        var expanded by remember { mutableStateOf(true) }

        Column(
            modifier = Modifier.padding(Spacing.normal),
            verticalArrangement = Arrangement.spacedBy(Spacing.normal)
        ) {
            ExpandableSectionWithButton(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                header = {
                    Text(
                        text = "Section with Button",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.normal)
                ) {
                    Text("Expanded content here")
                }
            }
        }
    }
}
