package com.enbridge.gdsgpscollection.ui.map.components.projectsettings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing
import com.enbridge.gdsgpscollection.domain.entity.WorkOrder
import androidx.compose.ui.tooling.preview.Preview
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme

/**
 * Work Order List Item component.
 *
 * Displays a clickable work order item with selection state.
 *
 * @param workOrder The work order to display
 * @param isSelected Whether this work order is currently selected
 * @param onClick Callback when the item is clicked
 * @param modifier Optional modifier
 */
@Composable
fun WorkOrderListItem(
    workOrder: WorkOrder,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        shape = MaterialTheme.shapes.small,
        tonalElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Text(
            text = workOrder.displayText,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.padding(Spacing.small)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkOrderListItemSelectedPreview() {
    GdsGpsCollectionTheme {
        WorkOrderListItem(
            workOrder = WorkOrder(
                id = "1",
                workOrderNumber = "WO-2024-001",
                address = "123 Main Street, Toronto, ON",
                poleType = "8 Foot Pole",
                distance = 150,
                displayText = "||123 Main Street, Toronto, ON||WO-2024-001"
            ),
            isSelected = true,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkOrderListItemUnselectedPreview() {
    GdsGpsCollectionTheme {
        WorkOrderListItem(
            workOrder = WorkOrder(
                id = "2",
                workOrderNumber = "WO-2024-002",
                address = "456 Elm Avenue, Mississauga, ON",
                poleType = "6 Foot Pole",
                distance = 250,
                displayText = "||456 Elm Avenue, Mississauga, ON||WO-2024-002"
            ),
            isSelected = false,
            onClick = {}
        )
    }
}
