package com.enbridge.gdsgpscollection.ui.map.components

/**
 * @author Sathya Narayanan
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.enbridge.gdsgpscollection.R
import com.enbridge.gdsgpscollection.designsystem.components.AppIconButton
import com.enbridge.gdsgpscollection.designsystem.components.AppTriStateCheckbox
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing
import com.enbridge.gdsgpscollection.domain.entity.GeometryType
import com.enbridge.gdsgpscollection.domain.entity.LegendItem
import com.enbridge.gdsgpscollection.ui.map.models.LayerUiState
import java.io.File

/**
 * Table of Contents bottom sheet for managing layer visibility and viewing legend information.
 *
 * Displays a hierarchical list of map layers with:
 * - Master "Select All" checkbox for toggling all layers
 * - Individual layer checkboxes for visibility control
 * - Expandable legend sections showing symbol information for each layer
 * - OpenStreetMap basemap toggle at the bottom
 *
 * This implementation uses ArcGIS renderer information to display actual symbol
 * images cached as PNG files, providing accurate legend representation.
 *
 * @param layers List of layer UI states with their visibility and legend information
 * @param osmVisible Current visibility state of the OpenStreetMap basemap
 * @param isLoadingLayers Whether layers are currently being loaded from geodatabase
 * @param onDismissRequest Callback invoked when the bottom sheet is dismissed
 * @param onToggleLayerVisibility Callback invoked when a layer's visibility is toggled
 * @param onToggleLayerExpanded Callback invoked when a layer's expansion state changes
 * @param onToggleSelectAll Callback invoked when the master "Select All" checkbox is clicked
 * @param onToggleOsmVisibility Callback invoked when OpenStreetMap visibility is toggled
 * @param modifier Optional modifier for the bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableOfContentsBottomSheet(
    layers: List<LayerUiState>,
    osmVisible: Boolean,
    isLoadingLayers: Boolean = false,
    onDismissRequest: () -> Unit,
    onToggleLayerVisibility: (layerId: String, visible: Boolean) -> Unit,
    onToggleLayerExpanded: (layerId: String) -> Unit,
    onToggleSelectAll: () -> Unit,
    onToggleOsmVisibility: (visible: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Calculate master checkbox state based on layer visibility
    val selectAllState = when {
        layers.isEmpty() -> ToggleableState.Off
        layers.all { it.isVisible } -> ToggleableState.On
        layers.none { it.isVisible } -> ToggleableState.Off
        else -> ToggleableState.Indeterminate
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.large)
                .padding(bottom = Spacing.large)
        ) {
            // Header with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.table_of_contents_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                AppIconButton(
                    icon = Icons.Default.Close,
                    contentDescription = stringResource(R.string.action_close),
                    onClick = onDismissRequest
                )
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            // Master "Select All" checkbox
            if (layers.isNotEmpty()) {
                AppTriStateCheckbox(
                    state = selectAllState,
                    onClick = onToggleSelectAll,
                    label = stringResource(R.string.select_all_layers),
                    modifier = Modifier.padding(vertical = Spacing.extraSmall)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.small))
            }

            // Layer list
            if (layers.isEmpty()) {
                // Empty state - no layers available
                Text(
                    text = stringResource(R.string.no_layers_available),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.extraLarge)
                )
            } else {
                // Display list of layers
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    items(layers, key = { it.id }) { layer ->
                        LayerItemWithLegend(
                            layer = layer,
                            onToggleVisibility = {
                                onToggleLayerVisibility(layer.id, !layer.isVisible)
                            },
                            onToggleExpanded = {
                                onToggleLayerExpanded(layer.id)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.normal))
            }

            // OpenStreetMap toggle at bottom
            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.small))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.extraSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = osmVisible,
                    onCheckedChange = if (isLoadingLayers) {
                        null // Disable interaction when loading
                    } else {
                        { checked -> onToggleOsmVisibility(checked) }
                    },
                    enabled = !isLoadingLayers
                )

                Text(
                    text = stringResource(R.string.open_street_map),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = Spacing.small)
                )

                if (isLoadingLayers) {
                    Spacer(modifier = Modifier.width(Spacing.small))
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

/**
 * Individual layer item with expandable legend section.
 *
 * Displays:
 * - Checkbox for layer visibility
 * - Layer name with geometry type indicator
 * - Single symbol inline (for SimpleRenderer with 1 symbol)
 * - Expand/collapse button (for layers with multiple symbols)
 * - Expandable legend list showing all symbols
 *
 * @param layer The layer UI state to display
 * @param onToggleVisibility Callback invoked when the visibility checkbox is toggled
 * @param onToggleExpanded Callback invoked when expand/collapse is triggered
 * @param modifier Optional modifier for the layer item
 */
@Composable
private fun LayerItemWithLegend(
    layer: LayerUiState,
    onToggleVisibility: () -> Unit,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Layer header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.extraSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visibility checkbox
            Checkbox(
                checked = layer.isVisible,
                onCheckedChange = { onToggleVisibility() }
            )

            // Layer name
            Text(
                text = layer.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Spacing.small)
            )

            // For layers with single symbol (SimpleRenderer), show inline
            if (layer.legendItems.size == 1) {
                val symbolPath = layer.legendItems.first().symbolImagePath
                if (symbolPath.isNotEmpty()) {
                    LegendSymbolImage(
                        symbolImagePath = symbolPath,
                        contentDescription = layer.legendItems.first().label,
                        modifier = Modifier.padding(start = Spacing.small)
                    )
                }
            } else if (layer.legendItems.size > 1) {
                // For layers with multiple symbols, show expand/collapse icon
                AppIconButton(
                    icon = if (layer.isExpanded) {
                        Icons.Default.Close // Use as collapse indicator
                    } else {
                        Icons.Default.Close // TODO: Use proper expand icon
                    },
                    contentDescription = if (layer.isExpanded) {
                        stringResource(R.string.action_collapse_legend)
                    } else {
                        stringResource(R.string.action_expand_legend)
                    },
                    onClick = onToggleExpanded
                )
            }
        }

        // Expandable legend section (only for layers with multiple symbols)
        if (layer.legendItems.size > 1 && layer.isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Spacing.massive, top = Spacing.extraSmall),
                verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
            ) {
                layer.legendItems.forEach { legendItem ->
                    LegendItemRow(legendItem = legendItem)
                }
            }
        }
    }
}

/**
 * Individual legend item row showing symbol and label.
 *
 * Displays the symbol image (loaded from cache) and its descriptive label.
 * If symbol image path is empty, displays text-only legend.
 *
 * @param legendItem The legend item data to display
 * @param modifier Optional modifier for the row
 */
@Composable
private fun LegendItemRow(
    legendItem: LegendItem,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Symbol image
        if (legendItem.symbolImagePath.isNotEmpty()) {
            LegendSymbolImage(
                symbolImagePath = legendItem.symbolImagePath,
                contentDescription = legendItem.label
            )
        }

        // Legend label
        Text(
            text = legendItem.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                start = if (legendItem.symbolImagePath.isNotEmpty()) Spacing.small else 0.dp
            )
        )
    }
}

/**
 * Composable for displaying a legend symbol image loaded from cache.
 *
 * Uses Coil to asynchronously load symbol PNG images that were pre-rendered
 * by the ViewModel. Shows a placeholder/error icon if the image fails to load.
 *
 * @param symbolImagePath File path to the cached symbol image
 * @param contentDescription Accessibility description for the image
 * @param modifier Optional modifier for the image
 */
@Composable
private fun LegendSymbolImage(
    symbolImagePath: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val file = remember(symbolImagePath) { File(symbolImagePath) }

    Box(
        modifier = modifier
            .size(24.dp)
            .clip(RoundedCornerShape(2.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (file.exists()) {
            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(context)
                    .data(file)
                    .crossfade(true)
                    .build()
            )
            androidx.compose.foundation.Image(
                painter = painter,
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(24.dp)
            )
        } else {
            // Fallback icon if image fails to load or file doesn't exist
            Icon(
                imageVector = Icons.Default.BrokenImage,
                contentDescription = "Symbol unavailable",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ============================================================================
// Preview Composables
// ============================================================================

@Preview(showBackground = true)
@Composable
private fun TableOfContentsBottomSheetPreview() {
    GdsGpsCollectionTheme {
        TableOfContentsBottomSheet(
            layers = listOf(
                LayerUiState(
                    id = "1",
                    name = "Gas Pipeline",
                    isVisible = true,
                    geometryType = GeometryType.POLYLINE,
                    legendItems = listOf(
                        LegendItem("Main Line", "/cache/legend/1_0.png", "MAIN"),
                        LegendItem("Service Line", "/cache/legend/1_1.png", "SERVICE"),
                        LegendItem("Lateral", "/cache/legend/1_2.png", "LATERAL")
                    ),
                    isExpanded = true
                ),
                LayerUiState(
                    id = "2",
                    name = "Meter Stations",
                    isVisible = true,
                    geometryType = GeometryType.POINT,
                    legendItems = listOf(
                        LegendItem("Meter", "/cache/legend/2_0.png")
                    ),
                    isExpanded = false
                ),
                LayerUiState(
                    id = "3",
                    name = "Survey Areas",
                    isVisible = false,
                    geometryType = GeometryType.POLYGON,
                    legendItems = listOf(
                        LegendItem("High Priority", "/cache/legend/3_0.png", "HIGH"),
                        LegendItem("Standard", "/cache/legend/3_1.png", "STANDARD")
                    ),
                    isExpanded = false
                )
            ),
            osmVisible = true,
            isLoadingLayers = false,
            onDismissRequest = {},
            onToggleLayerVisibility = { _, _ -> },
            onToggleLayerExpanded = {},
            onToggleSelectAll = {},
            onToggleOsmVisibility = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TableOfContentsBottomSheetEmptyPreview() {
    GdsGpsCollectionTheme {
        TableOfContentsBottomSheet(
            layers = emptyList(),
            osmVisible = false,
            isLoadingLayers = false,
            onDismissRequest = {},
            onToggleLayerVisibility = { _, _ -> },
            onToggleLayerExpanded = {},
            onToggleSelectAll = {},
            onToggleOsmVisibility = {}
        )
    }
}
