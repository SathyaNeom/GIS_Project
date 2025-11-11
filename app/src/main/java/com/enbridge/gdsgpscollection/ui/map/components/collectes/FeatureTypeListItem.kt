package com.enbridge.gdsgpscollection.ui.map.components.collectes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing
import com.enbridge.gdsgpscollection.ui.map.models.FeatureType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme

/**
 * Feature Type List Item with legend indicator and name.
 *
 * Displays a clickable card with a colored circle legend indicator
 * and the feature type name.
 *
 * @param featureType The feature type to display
 * @param onClick Callback when the item is clicked
 * @param modifier Optional modifier
 */
@Composable
fun FeatureTypeListItem(
    featureType: FeatureType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.normal),
            horizontalArrangement = Arrangement.spacedBy(Spacing.normal),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Legend indicator (colored circle)
            Canvas(
                modifier = Modifier.size(24.dp)
            ) {
                drawCircle(
                    color = featureType.legendColor,
                    radius = size.minDimension / 2
                )
            }

            // Feature type name
            Text(
                text = featureType.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FeatureTypeListItemPreview() {
    GdsGpsCollectionTheme {
        FeatureTypeListItem(
            featureType = FeatureType(
                id = "1",
                name = "Electric Pole",
                legendColor = Color.Blue,
                attributes = emptyList()
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FeatureTypeListItemLongNamePreview() {
    GdsGpsCollectionTheme {
        FeatureTypeListItem(
            featureType = FeatureType(
                id = "2",
                name = "Underground Distribution Cable with Long Name",
                legendColor = Color.Red,
                attributes = emptyList()
            ),
            onClick = {}
        )
    }
}
