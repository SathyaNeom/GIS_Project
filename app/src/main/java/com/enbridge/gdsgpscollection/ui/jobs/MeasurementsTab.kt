package com.enbridge.gdsgpscollection.ui.jobs

/**
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
import com.enbridge.gdsgpscollection.R
import com.enbridge.gdsgpscollection.designsystem.components.AppTextField
import com.enbridge.gdsgpscollection.designsystem.components.SingleSelectDropdown
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing
import com.enbridge.gdsgpscollection.domain.entity.JobCardEntry

@Composable
fun MeasurementsTab(
    entry: JobCardEntry,
    onUpdateField: ((JobCardEntry) -> JobCardEntry) -> Unit,
    isWideScreen: Boolean,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()
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
        // Connector Pipe Loc Dist
        item {
            AppTextField(
                value = entry.connectorPipeLocDist,
                onValueChange = { newValue -> onUpdateField { it.copy(connectorPipeLocDist = newValue) } },
                label = stringResource(R.string.field_connector_pipe_loc_dist)
            )
        }

        // Connector Pipe Loc Dir
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

        // Connector Pipe Ref Dir
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

        // Connector Pipe Ref Point
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

        // Connector Pipe Position
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

        // Street Width
        item {
            AppTextField(
                value = entry.streetWidth,
                onValueChange = { newValue -> onUpdateField { it.copy(streetWidth = newValue) } },
                label = stringResource(R.string.field_street_width)
            )
        }

        // Tap Size
        item {
            AppTextField(
                value = entry.tapSize,
                onValueChange = { newValue -> onUpdateField { it.copy(tapSize = newValue) } },
                label = stringResource(R.string.field_tap_size)
            )
        }

        // Riser On Wall
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

        // Riser Distance
        item {
            AppTextField(
                value = entry.riserDistance,
                onValueChange = { newValue -> onUpdateField { it.copy(riserDistance = newValue) } },
                label = stringResource(R.string.field_riser_distance)
            )
        }

        // Riser From Wall
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

        // Riser Depth
        item {
            AppTextField(
                value = entry.riserDepth,
                onValueChange = { newValue -> onUpdateField { it.copy(riserDepth = newValue) } },
                label = stringResource(R.string.field_riser_depth)
            )
        }

        // Riser Length
        item {
            AppTextField(
                value = entry.riserLength,
                onValueChange = { newValue -> onUpdateField { it.copy(riserLength = newValue) } },
                label = stringResource(R.string.field_riser_length)
            )
        }

        // Riser In Foundation
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

        // Main to Building Line
        item {
            AppTextField(
                value = entry.mainToBuildingLine,
                onValueChange = { newValue -> onUpdateField { it.copy(mainToBuildingLine = newValue) } },
                label = stringResource(R.string.field_main_to_building_line)
            )
        }

        // Main to Street Line
        item {
            AppTextField(
                value = entry.mainToStreetLine,
                onValueChange = { newValue -> onUpdateField { it.copy(mainToStreetLine = newValue) } },
                label = stringResource(R.string.field_main_to_street_line)
            )
        }

        // SL To BL
        item {
            AppTextField(
                value = entry.slToBL,
                onValueChange = { newValue -> onUpdateField { it.copy(slToBL = newValue) } },
                label = stringResource(R.string.field_sl_to_bl)
            )
        }

        // Curb To GasMain Distance
        item {
            AppTextField(
                value = entry.curbToGasMainDistance,
                onValueChange = { newValue -> onUpdateField { it.copy(curbToGasMainDistance = newValue) } },
                label = stringResource(R.string.field_curb_to_gas_main_distance)
            )
        }

        // Connection Data Location
        item {
            AppTextField(
                value = entry.connectionDataLocation,
                onValueChange = { newValue -> onUpdateField { it.copy(connectionDataLocation = newValue) } },
                label = stringResource(R.string.field_connection_data_location)
            )
        }

        // Tie In Building Ref
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

        // Tie In Location Desc
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

        // Connection Depth
        item {
            AppTextField(
                value = entry.connectionDepth,
                onValueChange = { newValue -> onUpdateField { it.copy(connectionDepth = newValue) } },
                label = stringResource(R.string.field_connection_depth)
            )
        }

        // Building Corner Ref
        item {
            AppTextField(
                value = entry.buildingCornerRef,
                onValueChange = { newValue -> onUpdateField { it.copy(buildingCornerRef = newValue) } },
                label = stringResource(R.string.field_building_corner_ref)
            )
        }

        // Service Length Total
        item {
            AppTextField(
                value = entry.serviceLengthTotal,
                onValueChange = { newValue -> onUpdateField { it.copy(serviceLengthTotal = newValue) } },
                label = stringResource(R.string.field_service_length_total)
            )
        }

        // Stub Length
        item {
            AppTextField(
                value = entry.stubLength,
                onValueChange = { newValue -> onUpdateField { it.copy(stubLength = newValue) } },
                label = stringResource(R.string.field_stub_length)
            )
        }

        // Main To Stick Outlet
        item {
            AppTextField(
                value = entry.mainToStickOutlet,
                onValueChange = { newValue -> onUpdateField { it.copy(mainToStickOutlet = newValue) } },
                label = stringResource(R.string.field_main_to_stick_outlet)
            )
        }
    }
}
