package com.enbridge.gpsdeviceproj.ui.jobs

/**
 * @author Sathya Narayanan
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.enbridge.gpsdeviceproj.R
import com.enbridge.gpsdeviceproj.designsystem.components.AppDatePicker
import com.enbridge.gpsdeviceproj.designsystem.components.AppTextField
import com.enbridge.gpsdeviceproj.designsystem.components.SingleSelectDropdown
import com.enbridge.gpsdeviceproj.designsystem.theme.Spacing
import com.enbridge.gpsdeviceproj.domain.entity.JobCardEntry

@Composable
fun JobCardTab(
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
        // WorkOrder (Display-only)
        item {
            AppTextField(
                value = entry.workOrder,
                onValueChange = { },
                label = stringResource(R.string.field_work_order),
                readOnly = true,
                enabled = false
            )
        }

        // Address
        item {
            AppTextField(
                value = entry.address,
                onValueChange = { newValue -> onUpdateField { it.copy(address = newValue) } },
                label = stringResource(R.string.field_address)
            )
        }

        // Block/Lot
        item {
            AppTextField(
                value = entry.blockLot,
                onValueChange = { newValue -> onUpdateField { it.copy(blockLot = newValue) } },
                label = stringResource(R.string.field_block_lot)
            )
        }

        // Municipality
        item {
            AppTextField(
                value = entry.municipality,
                onValueChange = { newValue -> onUpdateField { it.copy(municipality = newValue) } },
                label = stringResource(R.string.field_municipality)
            )
        }

        // Parent Asset ID
        item {
            AppTextField(
                value = entry.parentAssetId,
                onValueChange = { newValue -> onUpdateField { it.copy(parentAssetId = newValue) } },
                label = stringResource(R.string.field_parent_asset_id)
            )
        }

        // Service Type
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_service_type_residential),
                    stringResource(R.string.option_service_type_commercial),
                    stringResource(R.string.option_service_type_industrial)
                ),
                selectedItem = entry.serviceType.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(serviceType = newValue) } },
                label = stringResource(R.string.field_service_type)
            )
        }

        // Service Design
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_service_design_overhead),
                    stringResource(R.string.option_service_design_underground)
                ),
                selectedItem = entry.serviceDesign.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(serviceDesign = newValue) } },
                label = stringResource(R.string.field_service_design)
            )
        }

        // Connection Type
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_connection_type_permanent),
                    stringResource(R.string.option_connection_type_temporary)
                ),
                selectedItem = entry.connectionType.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(connectionType = newValue) } },
                label = stringResource(R.string.field_connection_type)
            )
        }

        // Connection With
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_connection_with_gas),
                    stringResource(R.string.option_connection_with_electric)
                ),
                selectedItem = entry.connectionWith.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(connectionWith = newValue) } },
                label = stringResource(R.string.field_connection_with)
            )
        }

        // Inside Meter
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_yes),
                    stringResource(R.string.option_no)
                ),
                selectedItem = entry.insideMeter.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(insideMeter = newValue) } },
                label = stringResource(R.string.field_inside_meter)
            )
        }

        // Wall_IO
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_yes),
                    stringResource(R.string.option_no)
                ),
                selectedItem = entry.wallIO.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(wallIO = newValue) } },
                label = stringResource(R.string.field_wall_io)
            )
        }

        // Wall_RLFB
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_yes),
                    stringResource(R.string.option_no)
                ),
                selectedItem = entry.wallRLFB.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(wallRLFB = newValue) } },
                label = stringResource(R.string.field_wall_rlfb)
            )
        }

        // Joint Trench
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_yes),
                    stringResource(R.string.option_no)
                ),
                selectedItem = entry.jointTrench.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(jointTrench = newValue) } },
                label = stringResource(R.string.field_joint_trench)
            )
        }

        // Railway Crossing
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_yes),
                    stringResource(R.string.option_no)
                ),
                selectedItem = entry.railwayCrossing.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(railwayCrossing = newValue) } },
                label = stringResource(R.string.field_railway_crossing)
            )
        }

        // Wall To Wall
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_yes),
                    stringResource(R.string.option_no)
                ),
                selectedItem = entry.wallToWall.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(wallToWall = newValue) } },
                label = stringResource(R.string.field_wall_to_wall)
            )
        }

        // Basement
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_yes),
                    stringResource(R.string.option_no)
                ),
                selectedItem = entry.basement.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(basement = newValue) } },
                label = stringResource(R.string.field_basement)
            )
        }

        // Building Entry
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_yes),
                    stringResource(R.string.option_no)
                ),
                selectedItem = entry.buildingEntry.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(buildingEntry = newValue) } },
                label = stringResource(R.string.field_building_entry)
            )
        }

        // Entry
        item {
            AppTextField(
                value = entry.entry,
                onValueChange = { newValue -> onUpdateField { it.copy(entry = newValue) } },
                label = stringResource(R.string.field_entry)
            )
        }

        // Depth
        item {
            AppTextField(
                value = entry.depth,
                onValueChange = { newValue -> onUpdateField { it.copy(depth = newValue) } },
                label = stringResource(R.string.field_depth)
            )
        }

        // Restoration
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_yes),
                    stringResource(R.string.option_no)
                ),
                selectedItem = entry.restoration.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(restoration = newValue) } },
                label = stringResource(R.string.field_restoration)
            )
        }

        // Meter Guard Required
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_yes),
                    stringResource(R.string.option_no)
                ),
                selectedItem = entry.meterGuardRequired.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(meterGuardRequired = newValue) } },
                label = stringResource(R.string.field_meter_guard_required)
            )
        }

        // Service Pipe NPS
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_service_pipe_nps_half),
                    stringResource(R.string.option_service_pipe_nps_three_quarter),
                    stringResource(R.string.option_service_pipe_nps_one)
                ),
                selectedItem = entry.servicePipeNPS.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(servicePipeNPS = newValue) } },
                label = stringResource(R.string.field_service_pipe_nps)
            )
        }

        // Service Pipe Material
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_service_pipe_material_copper),
                    stringResource(R.string.option_service_pipe_material_pvc),
                    stringResource(R.string.option_service_pipe_material_steel)
                ),
                selectedItem = entry.servicePipeMaterial.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(servicePipeMaterial = newValue) } },
                label = stringResource(R.string.field_service_pipe_material)
            )
        }

        // Service Pipe Status
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_service_pipe_status_active),
                    stringResource(R.string.option_service_pipe_status_inactive)
                ),
                selectedItem = entry.servicePipeStatus.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(servicePipeStatus = newValue) } },
                label = stringResource(R.string.field_service_pipe_status)
            )
        }

        // Service Pipe Pressure
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_service_pipe_pressure_low),
                    stringResource(R.string.option_service_pipe_pressure_medium),
                    stringResource(R.string.option_service_pipe_pressure_high)
                ),
                selectedItem = entry.servicePipePressure.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(servicePipePressure = newValue) } },
                label = stringResource(R.string.field_service_pipe_pressure)
            )
        }

        // Method of Installation
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_method_installation_manual),
                    stringResource(R.string.option_method_installation_machine)
                ),
                selectedItem = entry.methodOfInstallation.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(methodOfInstallation = newValue) } },
                label = stringResource(R.string.field_method_of_installation)
            )
        }

        // Excess Flow Valve Inst
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_yes),
                    stringResource(R.string.option_no)
                ),
                selectedItem = entry.excessFlowValveInst.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(excessFlowValveInst = newValue) } },
                label = stringResource(R.string.field_excess_flow_valve_inst)
            )
        }

        // Bricked
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_yes),
                    stringResource(R.string.option_no)
                ),
                selectedItem = entry.bricked.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(bricked = newValue) } },
                label = stringResource(R.string.field_bricked)
            )
        }

        // Windows
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_yes),
                    stringResource(R.string.option_no)
                ),
                selectedItem = entry.windows.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(windows = newValue) } },
                label = stringResource(R.string.field_windows)
            )
        }

        // Vents
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_yes),
                    stringResource(R.string.option_no)
                ),
                selectedItem = entry.vents.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(vents = newValue) } },
                label = stringResource(R.string.field_vents)
            )
        }

        // Main Energized By
        item {
            AppTextField(
                value = entry.mainEnergizedBy,
                onValueChange = { newValue -> onUpdateField { it.copy(mainEnergizedBy = newValue) } },
                label = stringResource(R.string.field_main_energized_by)
            )
        }

        // Main Energized Date
        item {
            AppDatePicker(
                selectedDateMillis = entry.mainEnergizedDate,
                onDateSelected = { newValue -> onUpdateField { it.copy(mainEnergizedDate = newValue) } },
                label = stringResource(R.string.field_main_energized_date)
            )
        }

        // # of FAC Applications
        item {
            AppTextField(
                value = entry.numFACApplications,
                onValueChange = { newValue -> onUpdateField { it.copy(numFACApplications = newValue) } },
                label = stringResource(R.string.field_num_fac_applications)
            )
        }

        // Service Valve Fittings
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_service_valve_fittings_type_a),
                    stringResource(R.string.option_service_valve_fittings_type_b),
                    stringResource(R.string.option_service_valve_fittings_type_c)
                ),
                selectedItem = entry.serviceValveFittings.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(serviceValveFittings = newValue) } },
                label = stringResource(R.string.field_service_valve_fittings)
            )
        }

        // Application Certificate#
        item {
            AppTextField(
                value = entry.applicationCertificateNum,
                onValueChange = { newValue -> onUpdateField { it.copy(applicationCertificateNum = newValue) } },
                label = stringResource(R.string.field_application_certificate_num)
            )
        }

        // Service Valve Description
        item {
            AppTextField(
                value = entry.serviceValveDescription,
                onValueChange = { newValue -> onUpdateField { it.copy(serviceValveDescription = newValue) } },
                label = stringResource(R.string.field_service_valve_description)
            )
        }

        // Service Valve Ref Dir
        item {
            AppTextField(
                value = entry.serviceValveRefDir,
                onValueChange = { newValue -> onUpdateField { it.copy(serviceValveRefDir = newValue) } },
                label = stringResource(R.string.field_service_valve_ref_dir)
            )
        }

        // Pressure Test Type
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(columns) }) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                SingleSelectDropdown(
                    items = listOf(
                        stringResource(R.string.option_pressure_test_type_1),
                        stringResource(R.string.option_pressure_test_type_2),
                        stringResource(R.string.option_pressure_test_type_other)
                    ),
                    selectedItem = entry.pressureTestType.takeIf { it.isNotEmpty() },
                    onItemSelected = { newValue -> onUpdateField { it.copy(pressureTestType = newValue) } },
                    label = stringResource(R.string.field_pressure_test_type)
                )

                // Show custom input if "Other" is selected
                if (entry.pressureTestType == stringResource(R.string.option_pressure_test_type_other)) {
                    AppTextField(
                        value = entry.pressureTestTypeOther,
                        onValueChange = { newValue -> onUpdateField { it.copy(pressureTestTypeOther = newValue) } },
                        label = stringResource(R.string.field_pressure_test_type_custom)
                    )
                }
            }
        }

        // Test Pressure
        item {
            AppTextField(
                value = entry.testPressure,
                onValueChange = { newValue -> onUpdateField { it.copy(testPressure = newValue) } },
                label = stringResource(R.string.field_test_pressure)
            )
        }

        // Test Duration
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_test_duration_short),
                    stringResource(R.string.option_test_duration_medium),
                    stringResource(R.string.option_test_duration_long)
                ),
                selectedItem = entry.testDuration.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(testDuration = newValue) } },
                label = stringResource(R.string.field_test_duration)
            )
        }

        // Test Unit
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_test_unit_psi),
                    stringResource(R.string.option_test_unit_bar)
                ),
                selectedItem = entry.testUnit.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(testUnit = newValue) } },
                label = stringResource(R.string.field_test_unit)
            )
        }

        // Test Medium
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_test_medium_air),
                    stringResource(R.string.option_test_medium_water)
                ),
                selectedItem = entry.testMedium.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(testMedium = newValue) } },
                label = stringResource(R.string.field_test_medium)
            )
        }

        // Test Date
        item {
            AppDatePicker(
                selectedDateMillis = entry.testDate,
                onDateSelected = { newValue -> onUpdateField { it.copy(testDate = newValue) } },
                label = stringResource(R.string.field_test_date)
            )
        }

        // Field App Coating Type
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_field_app_coating_type_a),
                    stringResource(R.string.option_field_app_coating_type_b)
                ),
                selectedItem = entry.fieldAppCoatingType.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(fieldAppCoatingType = newValue) } },
                label = stringResource(R.string.field_field_app_coating_type)
            )
        }

        // Service Valve Reference Pt
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_service_valve_ref_pt_a),
                    stringResource(R.string.option_service_valve_ref_pt_b)
                ),
                selectedItem = entry.serviceValveReferencePt.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(serviceValveReferencePt = newValue) } },
                label = stringResource(R.string.field_service_valve_reference_pt)
            )
        }

        // Service Valve Loc Dir
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_service_valve_loc_dir_north),
                    stringResource(R.string.option_service_valve_loc_dir_south)
                ),
                selectedItem = entry.serviceValveLocDir.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(serviceValveLocDir = newValue) } },
                label = stringResource(R.string.field_service_valve_loc_dir)
            )
        }

        // MQAP Version#
        item {
            SingleSelectDropdown(
                items = listOf(
                    stringResource(R.string.option_mqap_version_1),
                    stringResource(R.string.option_mqap_version_2)
                ),
                selectedItem = entry.mqapVersion.takeIf { it.isNotEmpty() },
                onItemSelected = { newValue -> onUpdateField { it.copy(mqapVersion = newValue) } },
                label = stringResource(R.string.field_mqap_version)
            )
        }

        // UserID
        item {
            AppTextField(
                value = entry.userId,
                onValueChange = { newValue -> onUpdateField { it.copy(userId = newValue) } },
                label = stringResource(R.string.field_user_id)
            )
        }
    }
}
