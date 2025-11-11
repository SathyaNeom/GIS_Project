package com.enbridge.gdsgpscollection.ui.map.components.projectsettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.enbridge.gdsgpscollection.designsystem.components.AppRadioButton
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing
import androidx.compose.ui.tooling.preview.Preview
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme

/**
 * Pole Type Selector component.
 *
 * Displays radio buttons for selecting pole type:
 * - 6 Foot Pole
 * - 8 Foot Pole
 * - Handheld
 *
 * @param selectedPoleType Currently selected pole type
 * @param onSelectPoleType Callback when a pole type is selected
 * @param modifier Optional modifier
 */
@Composable
fun PoleTypeSelector(
    selectedPoleType: String,
    onSelectPoleType: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        AppRadioButton(
            selected = selectedPoleType == "6 Foot Pole",
            onClick = { onSelectPoleType("6 Foot Pole") },
            label = "6 Foot Pole"
        )
        AppRadioButton(
            selected = selectedPoleType == "8 Foot Pole",
            onClick = { onSelectPoleType("8 Foot Pole") },
            label = "8 Foot Pole"
        )
        AppRadioButton(
            selected = selectedPoleType == "Handheld",
            onClick = { onSelectPoleType("Handheld") },
            label = "Handheld"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PoleTypeSelectorSixFootPreview() {
    GdsGpsCollectionTheme {
        PoleTypeSelector(
            selectedPoleType = "6 Foot Pole",
            onSelectPoleType = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PoleTypeSelectorEightFootPreview() {
    GdsGpsCollectionTheme {
        PoleTypeSelector(
            selectedPoleType = "8 Foot Pole",
            onSelectPoleType = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PoleTypeSelectorHandheldPreview() {
    GdsGpsCollectionTheme {
        PoleTypeSelector(
            selectedPoleType = "Handheld",
            onSelectPoleType = {}
        )
    }
}
