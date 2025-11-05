package com.enbridge.gpsdeviceproj.designsystem.components

/**
 * @author Sathya Narayanan
 */
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enbridge.gpsdeviceproj.designsystem.theme.ElectronicServicesTheme
import com.enbridge.gpsdeviceproj.designsystem.theme.Spacing

/**
 * Custom TabRow component with app theme styling
 *
 * @param selectedTabIndex The index of the currently selected tab
 * @param onTabSelected Callback when a tab is selected
 * @param tabs List of tab titles
 * @param modifier Modifier to be applied to the TabRow
 */
@Composable
fun AppTabRow(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<String>,
    modifier: Modifier = Modifier
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier.fillMaxWidth(),
        containerColor = Color.White,
        contentColor = Color.Black,
        indicator = { tabPositions ->
            if (tabPositions.isNotEmpty() && selectedTabIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.primaryContainer,  // Yellow indicator
                    height = 3.dp
                )
            }
        },
        divider = {},
        edgePadding = Spacing.small
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (selectedTabIndex == index) {
                            Color(0xFF424242)  // Dark grey for active tab
                        } else {
                            Color(0xFF757575)  // Light grey for inactive tab
                        }
                    )
                },
                selectedContentColor = Color(0xFF424242),      // Dark grey
                unselectedContentColor = Color(0xFF757575)    // Light grey
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppTabRowPreview() {
    ElectronicServicesTheme {
        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("JobCard", "Measurements", "MeterInfo")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(Spacing.normal)
        ) {
            AppTabRow(
                selectedTabIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                tabs = tabs
            )

            Text(
                text = "Selected: ${tabs[selectedTab]}",
                modifier = Modifier.padding(Spacing.normal),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
