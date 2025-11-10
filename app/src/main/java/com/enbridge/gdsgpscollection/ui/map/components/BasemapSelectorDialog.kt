package com.enbridge.gdsgpscollection.ui.map.components

/**
 * @author Sathya Narayanan
 */

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.arcgismaps.mapping.BasemapStyle
import com.enbridge.gdsgpscollection.designsystem.components.AppRadioButton
import com.enbridge.gdsgpscollection.designsystem.components.AppTextButton
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme

/**
 * Dialog for selecting a basemap style for the map.
 *
 * Displays a list of available ArcGIS basemap styles with radio button selection.
 * The user can choose from various basemap options like Streets, Imagery, Topographic, etc.
 *
 * @param currentBasemap The currently selected basemap style
 * @param onBasemapSelected Callback invoked when user selects a basemap
 * @param onDismiss Callback invoked when dialog is dismissed
 * @param modifier Optional modifier for this composable
 */
@Composable
fun BasemapSelectorDialog(
    currentBasemap: BasemapStyle,
    onBasemapSelected: (BasemapStyle) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Basemap") },
        text = {
            Column {
                BasemapOption(
                    "Streets",
                    BasemapStyle.ArcGISStreets,
                    currentBasemap,
                    onBasemapSelected
                )
                BasemapOption(
                    "Imagery",
                    BasemapStyle.ArcGISImagery,
                    currentBasemap,
                    onBasemapSelected
                )
                BasemapOption(
                    "Topographic",
                    BasemapStyle.ArcGISTopographic,
                    currentBasemap,
                    onBasemapSelected
                )
                BasemapOption(
                    "Light Gray",
                    BasemapStyle.ArcGISLightGray,
                    currentBasemap,
                    onBasemapSelected
                )
                BasemapOption(
                    "Dark Gray",
                    BasemapStyle.ArcGISDarkGray,
                    currentBasemap,
                    onBasemapSelected
                )
                BasemapOption(
                    "Navigation",
                    BasemapStyle.ArcGISNavigation,
                    currentBasemap,
                    onBasemapSelected
                )
                BasemapOption(
                    "Oceans",
                    BasemapStyle.ArcGISOceans,
                    currentBasemap,
                    onBasemapSelected
                )
            }
        },
        confirmButton = {
            AppTextButton(
                text = "Close",
                onClick = onDismiss
            )
        },
        modifier = modifier
    )
}

/**
 * Individual basemap option with radio button.
 *
 * @param name Display name for the basemap
 * @param style The BasemapStyle enum value
 * @param currentStyle The currently selected basemap style
 * @param onSelected Callback when this option is selected
 */
@Composable
private fun BasemapOption(
    name: String,
    style: BasemapStyle,
    currentStyle: BasemapStyle,
    onSelected: (BasemapStyle) -> Unit
) {
    AppRadioButton(
        selected = currentStyle == style,
        onClick = { onSelected(style) },
        label = name
    )
}

@Preview(showBackground = true)
@Composable
private fun BasemapSelectorDialogPreview() {
    GdsGpsCollectionTheme {
        BasemapSelectorDialog(
            currentBasemap = BasemapStyle.ArcGISTopographic,
            onBasemapSelected = {},
            onDismiss = {}
        )
    }
}
