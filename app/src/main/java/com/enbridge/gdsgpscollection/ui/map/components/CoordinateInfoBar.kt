package com.enbridge.gdsgpscollection.ui.map.components

/**
 * @author Sathya Narayanan
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme

/**
 * Bottom bar displaying real-time coordinate and map information.
 *
 * Shows GPS accuracy, X/Y coordinates, elevation, and current map scale.
 * This component is typically placed at the bottom of the map view to provide
 * continuous feedback about the map state and user's position.
 *
 * @param accuracy GPS accuracy value (e.g., "±5m" or "--" if unavailable)
 * @param x X coordinate (longitude) formatted as string
 * @param y Y coordinate (latitude) formatted as string
 * @param elevation Elevation value formatted as string
 * @param scale Current map scale formatted as string
 * @param modifier Optional modifier for this composable
 */
@Composable
fun CoordinateInfoBar(
    accuracy: String,
    x: String,
    y: String,
    elevation: String,
    scale: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CoordinateItem("Accuracy", accuracy)
            CoordinateItem("X", x)
            CoordinateItem("Y", y)
            CoordinateItem("Elev", elevation)
            CoordinateItem("Scale", scale)
        }
    }
}

/**
 * Individual coordinate information item.
 *
 * Displays a label and its corresponding value in a compact format.
 *
 * @param label The label text (e.g., "X", "Y", "Scale")
 * @param value The value to display
 */
@Composable
private fun CoordinateItem(
    label: String,
    value: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CoordinateInfoBarPreview() {
    GdsGpsCollectionTheme {
        CoordinateInfoBar(
            accuracy = "±5m",
            x = "-122.4194",
            y = "37.7749",
            elevation = "15m",
            scale = "72000"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CoordinateInfoBarEmptyPreview() {
    GdsGpsCollectionTheme {
        CoordinateInfoBar(
            accuracy = "--",
            x = "--",
            y = "--",
            elevation = "--",
            scale = "--"
        )
    }
}
