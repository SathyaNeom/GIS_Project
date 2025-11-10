package com.enbridge.gdsgpscollection.ui.jobs

/**
 * Measurements Tab Component
 *
 * This file contains the measurements form tab that displays all measurement-related
 * fields for job card entries. The form includes distances, directions, and positional
 * data necessary for accurate service installation and documentation.
 *
 * Features:
 * - Responsive grid layout (adapts to device screen size)
 * - Mixed input types (text fields for measurements, dropdowns for directions)
 * - Directional reference points (North, South, East, West)
 * - Position and location measurements
 * - Distance calculations
 *
 * @author Sathya Narayanan
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.enbridge.gdsgpscollection.R
import com.enbridge.gdsgpscollection.designsystem.components.AppTextField
import com.enbridge.gdsgpscollection.designsystem.components.SingleSelectDropdown
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing
import com.enbridge.gdsgpscollection.domain.entity.JobCardEntry

/**
 * MeasurementsTab displays all measurement-related fields for a job card.
 *
 * This tab focuses on positional and measurement data including:
 * - Connector pipe location and reference points
 * - Riser measurements (distance, depth, length)
 * - Street and building measurements
 * - Tie-in location information
 *
 * All measurements are critical for accurate service installation and
 * future reference/maintenance activities.
 *
 * @param entry The job card entry containing all measurement values
 * @param onUpdateField Callback invoked when any field value changes.
 *                      Receives a lambda that transforms the current entry
 * @param isWideScreen Boolean indicating if the device has a wide screen (tablet)
 *                     Controls whether to display 1 or 2 columns
 * @param modifier Optional modifier for the grid
 */
@Composable
fun MeasurementsTab(
    entry: JobCardEntry,
    onUpdateField: ((JobCardEntry) -> JobCardEntry) -> Unit,
    isWideScreen: Boolean,
    modifier: Modifier = Modifier
) {
    // Scroll state for the grid
    val gridState = rememberLazyGridState()

    // Determine column count based on screen width
    val columns = if (isWideScreen) 2 else 1

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        state = gridState,
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.normal),
        horizontalArrangement = Arrangement.spacedBy(Spacing.normal),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        // ========== Section 1: Connector Pipe Location ==========

        // Connector Pipe Location Distance - Distance from reference point
        item {
            AppTextField(
                value = entry.connectorPipeLocDist,
                onValueChange = { newValue -> onUpdateField { it.copy(connectorPipeLocDist = newValue) } },
                label = stringResource(R.string.field_connector_pipe_loc_dist)
            )
        }

        // Connector Pipe Location Direction - North or South
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_direction_north),
                    stringResource(R.string.option_direction_south)
                ),
                selectedItem = entry.connectorPipeLocDir.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(connectorPipeLocDir = newValue) } },
                label = stringResource(R.string.field_connector_pipe_loc_dir)
            )
        }

        // Connector Pipe Reference Direction - East or West
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_direction_east),
                    stringResource(R.string.option_direction_west)
                ),
                selectedItem = entry.connectorPipeRefDir.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(connectorPipeRefDir = newValue) } },
                label = stringResource(R.string.field_connector_pipe_ref_dir)
            )
        }

        // Connector Pipe Reference Point - Fixed reference location
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_connector_pipe_ref_point_a),
                    stringResource(R.string.option_connector_pipe_ref_point_b)
                ),
                selectedItem = entry.connectorPipeRefPoint.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(connectorPipeRefPoint = newValue) } },
                label = stringResource(R.string.field_connector_pipe_ref_point)
            )
        }

        // Connector Pipe Position - Specific position marker
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_connector_pipe_position_1),
                    stringResource(R.string.option_connector_pipe_position_2)
                ),
                selectedItem = entry.connectorPipePosition.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(connectorPipePosition = newValue) } },
                label = stringResource(R.string.field_connector_pipe_position)
            )
        }

        // ========== Section 2: Street and Tap Measurements ==========

        // Street Width - Width of the street at service location
        item {
            AppTextField(
                value = entry.streetWidth,
                onValueChange = { newValue -> onUpdateField { it.copy(streetWidth = newValue) } },
                label = stringResource(R.string.field_street_width)
            )
        }

        // Tap Size - Size of the main line tap
        item {
            AppTextField(
                value = entry.tapSize,
                onValueChange = { newValue -> onUpdateField { it.copy(tapSize = newValue) } },
                label = stringResource(R.string.field_tap_size)
            )
        }

        // ========== Section 3: Riser Measurements ==========

        // Riser On Wall - Is riser mounted on wall?
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_yes),
                    stringResource(R.string.option_no)
                ),
                selectedItem = entry.riserOnWall.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(riserOnWall = newValue) } },
                label = stringResource(R.string.field_riser_on_wall)
            )
        }

        // Riser Distance - Distance from reference point
        item {
            AppTextField(
                value = entry.riserDistance,
                onValueChange = { newValue -> onUpdateField { it.copy(riserDistance = newValue) } },
                label = stringResource(R.string.field_riser_distance)
            )
        }

        // Riser From Wall - Distance from wall to riser
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_yes),
                    stringResource(R.string.option_no)
                ),
                selectedItem = entry.riserFromWall.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(riserFromWall = newValue) } },
                label = stringResource(R.string.field_riser_from_wall)
            )
        }

        // Riser Depth - Depth of riser installation
        item {
            AppTextField(
                value = entry.riserDepth,
                onValueChange = { newValue -> onUpdateField { it.copy(riserDepth = newValue) } },
                label = stringResource(R.string.field_riser_depth)
            )
        }

        // Riser Length - Total length of riser pipe
        item {
            AppTextField(
                value = entry.riserLength,
                onValueChange = { newValue -> onUpdateField { it.copy(riserLength = newValue) } },
                label = stringResource(R.string.field_riser_length)
            )
        }

        // Riser In Foundation - Is riser embedded in foundation?
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_yes),
                    stringResource(R.string.option_no)
                ),
                selectedItem = entry.riserInFoundation.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(riserInFoundation = newValue) } },
                label = stringResource(R.string.field_riser_in_foundation)
            )
        }

        // ========== Section 4: Building and Main Line Distances ==========

        // Main to Building Line - Distance from main to building
        item {
            AppTextField(
                value = entry.mainToBuildingLine,
                onValueChange = { newValue -> onUpdateField { it.copy(mainToBuildingLine = newValue) } },
                label = stringResource(R.string.field_main_to_building_line)
            )
        }

        // Main to Street Line - Distance from main to street line
        item {
            AppTextField(
                value = entry.mainToStreetLine,
                onValueChange = { newValue -> onUpdateField { it.copy(mainToStreetLine = newValue) } },
                label = stringResource(R.string.field_main_to_street_line)
            )
        }

        // Street Line to Building Line (SL To BL)
        item {
            AppTextField(
                value = entry.slToBL,
                onValueChange = { newValue -> onUpdateField { it.copy(slToBL = newValue) } },
                label = stringResource(R.string.field_sl_to_bl)
            )
        }

        // Curb to Gas Main Distance - Distance from curb to gas main
        item {
            AppTextField(
                value = entry.curbToGasMainDistance,
                onValueChange = { newValue -> onUpdateField { it.copy(curbToGasMainDistance = newValue) } },
                label = stringResource(R.string.field_curb_to_gas_main_distance)
            )
        }

        // ========== Section 5: Connection Data & Tie-In Information ==========

        // Connection Data Location - Where connection data is recorded
        item {
            AppTextField(
                value = entry.connectionDataLocation,
                onValueChange = { newValue -> onUpdateField { it.copy(connectionDataLocation = newValue) } },
                label = stringResource(R.string.field_connection_data_location)
            )
        }

        // Tie In Building Reference - Building reference for tie-in
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_tie_in_ref_1),
                    stringResource(R.string.option_tie_in_ref_2)
                ),
                selectedItem = entry.tieInBuildingRef.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(tieInBuildingRef = newValue) } },
                label = stringResource(R.string.field_tie_in_building_ref)
            )
        }

        // Tie In Location Description - Description of tie-in location
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_tie_in_desc_1),
                    stringResource(R.string.option_tie_in_desc_2)
                ),
                selectedItem = entry.tieInLocationDesc.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(tieInLocationDesc = newValue) } },
                label = stringResource(R.string.field_tie_in_location_desc)
            )
        }

        // Connection Depth - Depth of service connection
        item {
            AppTextField(
                value = entry.connectionDepth,
                onValueChange = { newValue -> onUpdateField { it.copy(connectionDepth = newValue) } },
                label = stringResource(R.string.field_connection_depth)
            )
        }

        // ========== Section 6: Service Length and Reference Points ==========

        // Building Corner Reference - Reference corner of building
        item {
            AppTextField(
                value = entry.buildingCornerRef,
                onValueChange = { newValue -> onUpdateField { it.copy(buildingCornerRef = newValue) } },
                label = stringResource(R.string.field_building_corner_ref)
            )
        }

        // Service Length Total - Total length of service run
        item {
            AppTextField(
                value = entry.serviceLengthTotal,
                onValueChange = { newValue -> onUpdateField { it.copy(serviceLengthTotal = newValue) } },
                label = stringResource(R.string.field_service_length_total)
            )
        }

        // Stub Length - Length of service stub
        item {
            AppTextField(
                value = entry.stubLength,
                onValueChange = { newValue -> onUpdateField { it.copy(stubLength = newValue) } },
                label = stringResource(R.string.field_stub_length)
            )
        }

        // Main to Stick Outlet - Distance from main to stick outlet
        item {
            AppTextField(
                value = entry.mainToStickOutlet,
                onValueChange = { newValue -> onUpdateField { it.copy(mainToStickOutlet = newValue) } },
                label = stringResource(R.string.field_main_to_stick_outlet)
            )
        }
    }
}

/**
 * Preview for MeasurementsTab in single column layout (phone)
 */
@Preview(
    name = "MeasurementsTab - Phone",
    showBackground = true,
    widthDp = 360
)
@Composable
private fun MeasurementsTabPhonePreview() {
    GdsGpsCollectionTheme {
        MeasurementsTab(
            entry = JobCardEntry(
                connectorPipeLocDist = "15.5",
                connectorPipeLocDir = "North",
                streetWidth = "30.0",
                riserDistance = "12.5"
            ),
            onUpdateField = { },
            isWideScreen = false
        )
    }
}

/**
 * Preview for MeasurementsTab in two column layout (tablet)
 */
@Preview(
    name = "MeasurementsTab - Tablet",
    showBackground = true,
    widthDp = 800
)
@Composable
private fun MeasurementsTabTabletPreview() {
    GdsGpsCollectionTheme {
        MeasurementsTab(
            entry = JobCardEntry(
                connectorPipeLocDist = "15.5",
                connectorPipeLocDir = "North",
                connectorPipeRefDir = "East",
                streetWidth = "30.0",
                riserDistance = "12.5",
                riserDepth = "3.5",
                mainToBuildingLine = "45.0"
            ),
            onUpdateField = { },
            isWideScreen = true
        )
    }
}
