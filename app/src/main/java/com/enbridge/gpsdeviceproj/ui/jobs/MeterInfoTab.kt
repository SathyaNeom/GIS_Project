package com.enbridge.gpsdeviceproj.ui.jobs

/**
 * This package contains the feature implementation for jobs.
 * It includes Compose UI components and functions for displaying job-related data.
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
import com.enbridge.gpsdeviceproj.R
import com.enbridge.gpsdeviceproj.designsystem.components.AppTextField
import com.enbridge.gpsdeviceproj.designsystem.components.SingleSelectDropdown
import com.enbridge.gpsdeviceproj.designsystem.theme.Spacing
import com.enbridge.gpsdeviceproj.domain.entity.JobCardEntry

@Composable
fun MeterInfoTab(
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
        // Meter On Index
        item {
            AppTextField(
                value = entry.meterOnIndex,
                onValueChange = { newValue -> onUpdateField { it.copy(meterOnIndex = newValue) } },
                label = stringResource(R.string.field_meter_on_index)
            )
        }

        // Meter Size
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

        // Meter Number
        item {
            AppTextField(
                value = entry.meterNumber,
                onValueChange = { newValue -> onUpdateField { it.copy(meterNumber = newValue) } },
                label = stringResource(R.string.field_meter_number)
            )
        }

        // Meter Make No
        item {
            AppTextField(
                value = entry.meterMakeNo,
                onValueChange = { newValue -> onUpdateField { it.copy(meterMakeNo = newValue) } },
                label = stringResource(R.string.field_meter_make_no)
            )
        }

        // Meter No Dials
        item {
            AppTextField(
                value = entry.meterNoDials,
                onValueChange = { newValue -> onUpdateField { it.copy(meterNoDials = newValue) } },
                label = stringResource(R.string.field_meter_no_dials)
            )
        }

        // Meter Location
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

        // Meter GI Year
        item {
            AppTextField(
                value = entry.meterGIYear,
                onValueChange = { newValue -> onUpdateField { it.copy(meterGIYear = newValue) } },
                label = stringResource(R.string.field_meter_gi_year)
            )
        }

        // Regulator Location
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

        // Regulator Type Code
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

        // Regulator Manufacturer Dt
        item {
            AppTextField(
                value = entry.regulatorManufacturerDt,
                onValueChange = { newValue -> onUpdateField { it.copy(regulatorManufacturerDt = newValue) } },
                label = stringResource(R.string.field_regulator_manufacturer_dt)
            )
        }

        // Regulator Function
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
