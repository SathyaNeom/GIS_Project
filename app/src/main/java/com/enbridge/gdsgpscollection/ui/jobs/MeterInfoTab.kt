package com.enbridge.gdsgpscollection.ui.jobs

/**
 * Meter Information Tab Component
 *
 * This file contains the meter information form tab that displays all meter and
 * regulator-related fields for job card entries. This data is critical for tracking
 * meter installations, configurations, and regulatory compliance.
 *
 * Features:
 * - Responsive grid layout (adapts to device screen size)
 * - Meter identification fields (number, size, make, model)
 * - Meter location and installation information
 * - Regulator specifications and settings
 * - Mixed input types for flexible data entry
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
 * MeterInfoTab displays all meter and regulator information fields.
 *
 * This tab is focused on meter and regulator specifications, including:
 * - Meter identification (number, size, make, model)
 * - Meter location and installation year
 * - Initial meter reading
 * - Regulator type, location, and function
 * - Manufacturer and certification data
 *
 * This information is essential for asset tracking, maintenance scheduling,
 * and regulatory compliance reporting.
 *
 * @param entry The job card entry containing all meter and regulator values
 * @param onUpdateField Callback invoked when any field value changes.
 *                      Receives a lambda that transforms the current entry
 * @param isWideScreen Boolean indicating if the device has a wide screen (tablet)
 *                     Controls whether to display 1 or 2 columns
 * @param modifier Optional modifier for the grid
 */
@Composable
fun MeterInfoTab(
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
        // ========== Section 1: Meter Identification ==========

        // Meter On Index - Initial meter reading at installation
        item {
            AppTextField(
                value = entry.meterOnIndex,
                onValueChange = { newValue -> onUpdateField { it.copy(meterOnIndex = newValue) } },
                label = stringResource(R.string.field_meter_on_index)
            )
        }

        // Meter Size - Physical size/capacity of the meter
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_meter_size_small),
                    stringResource(R.string.option_meter_size_medium),
                    stringResource(R.string.option_meter_size_large)
                ),
                selectedItem = entry.meterSize.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(meterSize = newValue) } },
                label = stringResource(R.string.field_meter_size)
            )
        }

        // Meter Number - Unique meter identifier
        item {
            AppTextField(
                value = entry.meterNumber,
                onValueChange = { newValue -> onUpdateField { it.copy(meterNumber = newValue) } },
                label = stringResource(R.string.field_meter_number)
            )
        }

        // Meter Make Number - Manufacturer's model number
        item {
            AppTextField(
                value = entry.meterMakeNo,
                onValueChange = { newValue -> onUpdateField { it.copy(meterMakeNo = newValue) } },
                label = stringResource(R.string.field_meter_make_no)
            )
        }

        // Meter Number of Dials - Dial count on meter display
        item {
            AppTextField(
                value = entry.meterNoDials,
                onValueChange = { newValue -> onUpdateField { it.copy(meterNoDials = newValue) } },
                label = stringResource(R.string.field_meter_no_dials)
            )
        }

        // ========== Section 2: Meter Installation Details ==========

        // Meter Location - Indoor or Outdoor installation
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_meter_location_indoor),
                    stringResource(R.string.option_meter_location_outdoor)
                ),
                selectedItem = entry.meterLocation.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(meterLocation = newValue) } },
                label = stringResource(R.string.field_meter_location)
            )
        }

        // Meter GI Year - Year of meter installation (GI = Gas Installation)
        item {
            AppTextField(
                value = entry.meterGIYear,
                onValueChange = { newValue -> onUpdateField { it.copy(meterGIYear = newValue) } },
                label = stringResource(R.string.field_meter_gi_year)
            )
        }

        // ========== Section 3: Regulator Information ==========

        // Regulator Location - Where the regulator is installed
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_regulator_location_1),
                    stringResource(R.string.option_regulator_location_2)
                ),
                selectedItem = entry.regulatorLocation.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(regulatorLocation = newValue) } },
                label = stringResource(R.string.field_regulator_location)
            )
        }

        // Regulator Type Code - Type/model code of the regulator
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_regulator_code_a),
                    stringResource(R.string.option_regulator_code_b)
                ),
                selectedItem = entry.regulatorTypeCode.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(regulatorTypeCode = newValue) } },
                label = stringResource(R.string.field_regulator_type_code)
            )
        }

        // Regulator Manufacturer Data - Manufacturer information and date
        item {
            AppTextField(
                value = entry.regulatorManufacturerDt,
                onValueChange = { newValue -> onUpdateField { it.copy(regulatorManufacturerDt = newValue) } },
                label = stringResource(R.string.field_regulator_manufacturer_dt)
            )
        }

        // Regulator Function - Primary function/purpose of the regulator
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_regulator_function_1),
                    stringResource(R.string.option_regulator_function_2)
                ),
                selectedItem = entry.regulatorFunction.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(regulatorFunction = newValue) } },
                label = stringResource(R.string.field_regulator_function)
            )
        }
    }
}

/**
 * Preview for MeterInfoTab in single column layout (phone)
 */
@Preview(
    name = "MeterInfoTab - Phone",
    showBackground = true,
    widthDp = 360
)
@Composable
private fun MeterInfoTabPhonePreview() {
    GdsGpsCollectionTheme {
        MeterInfoTab(
            entry = JobCardEntry(
                meterOnIndex = "00012345",
                meterSize = "Medium",
                meterNumber = "MTR-2024-001",
                meterLocation = "Indoor",
                meterGIYear = "2024"
            ),
            onUpdateField = { },
            isWideScreen = false
        )
    }
}

/**
 * Preview for MeterInfoTab in two column layout (tablet)
 */
@Preview(
    name = "MeterInfoTab - Tablet",
    showBackground = true,
    widthDp = 800
)
@Composable
private fun MeterInfoTabTabletPreview() {
    GdsGpsCollectionTheme {
        MeterInfoTab(
            entry = JobCardEntry(
                meterOnIndex = "00012345",
                meterSize = "Medium",
                meterNumber = "MTR-2024-001",
                meterMakeNo = "MAKE-123",
                meterNoDials = "5",
                meterLocation = "Indoor",
                meterGIYear = "2024",
                regulatorTypeCode = "Code A"
            ),
            onUpdateField = { },
            isWideScreen = true
        )
    }
}
