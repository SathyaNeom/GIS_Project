package com.enbridge.gdsgpscollection.ui.jobs

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
import androidx.compose.ui.tooling.preview.Preview
import com.enbridge.gdsgpscollection.R
import com.enbridge.gdsgpscollection.designsystem.components.AppDatePicker
import com.enbridge.gdsgpscollection.designsystem.components.AppTextField
import com.enbridge.gdsgpscollection.designsystem.components.SingleSelectDropdown
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing
import com.enbridge.gdsgpscollection.domain.entity.JobCardEntry

/**
 * JobCard Tab Component
 *
 * This file contains the main job card form tab that displays comprehensive
 * job-related fields in a responsive grid layout. The form adapts to screen
 * size and displays fields in either 1 or 2 columns based on the device width.
 *
 * Features:
 * - Responsive grid layout (1 column for phones, 2 columns for tablets)
 * - Read-only work order field
 * - Multiple input types (text, dropdown, date picker)
 * - Conditional field visibility (e.g., custom pressure test type)
 * - Comprehensive validation support
 *
 * @author Sathya Narayanan
 */

/**
 * JobCardTab displays the main job card form with comprehensive job details.
 *
 * The form is organized in a responsive grid that adapts to screen size.
 * Fields include address information, service details, installation method,
 * pressure testing information, and various job-specific attributes.
 *
 * Key Features:
 * - Responsive layout (1-2 columns based on screen width)
 * - Read-only work order field (pre-filled)
 * - Mixed input types (text fields, dropdowns, date pickers)
 * - Conditional field visibility (e.g., "Other" pressure test type)
 * - Real-time field updates via callbacks
 *
 * @param entry The job card entry containing all field values
 * @param onUpdateField Callback invoked when any field value changes.
 *                      Receives a lambda that transforms the current entry
 * @param isWideScreen Boolean indicating if the device has a wide screen (tablet)
 *                     Controls whether to display 1 or 2 columns
 * @param modifier Optional modifier for the grid
 */
@Composable
fun JobCardTab(
    entry: JobCardEntry,
    onUpdateField: ((JobCardEntry) -> JobCardEntry) -> Unit,
    isWideScreen: Boolean,
    modifier: Modifier = Modifier
) {
    // Scroll state for the grid, allows programmatic scrolling if needed
    val gridState = rememberLazyGridState()

    // Determine column count based on screen width
    // Tablets (isWideScreen = true) show 2 columns, phones show 1 column
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
        // ========== Section 1: Work Order & Basic Information ==========

        // WorkOrder (Display-only field)
        // This field is pre-populated and cannot be edited
        item {
            AppTextField(
                value = entry.workOrder,
                onValueChange = { },
                label = stringResource(R.string.field_work_order),
                readOnly = true,
                enabled = false
            )
        }

        // Address field - Primary location information
        item {
            AppTextField(
                value = entry.address,
                onValueChange = { newValue -> onUpdateField { it.copy(address = newValue) } },
                label = stringResource(R.string.field_address)
            )
        }

        // Block/Lot - Property identifier
        item {
            AppTextField(
                value = entry.blockLot,
                onValueChange = { newValue -> onUpdateField { it.copy(blockLot = newValue) } },
                label = stringResource(R.string.field_block_lot)
            )
        }

        // Municipality - City/town information
        item {
            AppTextField(
                value = entry.municipality,
                onValueChange = { newValue -> onUpdateField { it.copy(municipality = newValue) } },
                label = stringResource(R.string.field_municipality)
            )
        }

        // Parent Asset ID - Reference to parent infrastructure
        item {
            AppTextField(
                value = entry.parentAssetId,
                onValueChange = { newValue -> onUpdateField { it.copy(parentAssetId = newValue) } },
                label = stringResource(R.string.field_parent_asset_id)
            )
        }

        // ========== Section 2: Service Configuration ==========

        // Service Type - Residential, Commercial, or Industrial
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

        // Service Design - Overhead or Underground installation
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

        // Connection Type - Permanent or Temporary
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

        // Connection With - Gas or Electric service
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

        // ========== Section 3: Installation Details (Yes/No Questions) ==========

        // Inside Meter - Is meter located inside?
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

        // Wall_IO - Wall inlet/outlet configuration
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

        // Wall_RLFB - Wall reinforcement/bracket
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

        // Joint Trench - Shared utility trench
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

        // Railway Crossing - Crosses railway tracks
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

        // Wall To Wall - Full wall penetration
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

        // Basement - Service enters through basement
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

        // Building Entry - Point of service entry
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

        // ========== Section 4: Additional Details ==========

        // Entry - Entry point description
        item {
            AppTextField(
                value = entry.entry,
                onValueChange = { newValue -> onUpdateField { it.copy(entry = newValue) } },
                label = stringResource(R.string.field_entry)
            )
        }

        // Depth - Installation depth
        item {
            AppTextField(
                value = entry.depth,
                onValueChange = { newValue -> onUpdateField { it.copy(depth = newValue) } },
                label = stringResource(R.string.field_depth)
            )
        }

        // Restoration - Site restoration required
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

        // Meter Guard Required - Protection device needed
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

        // ========== Section 5: Service Pipe Specifications ==========

        // Service Pipe NPS - Nominal Pipe Size
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

        // Service Pipe Material - Copper, PVC, Steel, etc.
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

        // Service Pipe Status - Active or Inactive
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

        // Service Pipe Pressure - Operating pressure level
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

        // ========== Section 6: Installation Method & Safety ==========

        // Method of Installation - Manual or Machine
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

        // Excess Flow Valve Installed - Safety device
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

        // ========== Section 7: Building Features ==========

        // Bricked - Brick construction present
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

        // Windows - Window proximity considerations
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

        // Vents - Ventilation considerations
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

        // ========== Section 8: Main Line Information ==========

        // Main Energized By - Person/crew who energized main
        item {
            AppTextField(
                value = entry.mainEnergizedBy,
                onValueChange = { newValue -> onUpdateField { it.copy(mainEnergizedBy = newValue) } },
                label = stringResource(R.string.field_main_energized_by)
            )
        }

        // Main Energized Date - When main was energized
        item {
            AppDatePicker(
                selectedDateMillis = entry.mainEnergizedDate,
                onDateSelected = { newValue -> onUpdateField { it.copy(mainEnergizedDate = newValue) } },
                label = stringResource(R.string.field_main_energized_date)
            )
        }

        // Number of FAC Applications - Facility Application Count
        item {
            AppTextField(
                value = entry.numFACApplications,
                onValueChange = { newValue -> onUpdateField { it.copy(numFACApplications = newValue) } },
                label = stringResource(R.string.field_num_fac_applications)
            )
        }

        // ========== Section 9: Service Valve Information ==========

        // Service Valve Fittings - Type of valve fittings
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

        // Application Certificate Number
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

        // Service Valve Reference Direction
        item {
            AppTextField(
                value = entry.serviceValveRefDir,
                onValueChange = { newValue -> onUpdateField { it.copy(serviceValveRefDir = newValue) } },
                label = stringResource(R.string.field_service_valve_ref_dir)
            )
        }

        // ========== Section 10: Pressure Testing ==========

        // Pressure Test Type with conditional "Other" input
        // Spans full width of grid when "Other" option is selected
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

                // Conditional field: Only show when "Other" is selected
                if (entry.pressureTestType == stringResource(R.string.option_pressure_test_type_other)) {
                    AppTextField(
                        value = entry.pressureTestTypeOther,
                        onValueChange = { newValue -> onUpdateField { it.copy(pressureTestTypeOther = newValue) } },
                        label = stringResource(R.string.field_pressure_test_type_custom)
                    )
                }
            }
        }

        // Test Pressure - Pressure value
        item {
            AppTextField(
                value = entry.testPressure,
                onValueChange = { newValue -> onUpdateField { it.copy(testPressure = newValue) } },
                label = stringResource(R.string.field_test_pressure)
            )
        }

        // Test Duration - How long test was conducted
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

        // Test Unit - PSI or Bar
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

        // Test Medium - Air or Water
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

        // Test Date - When test was performed
        item {
            AppDatePicker(
                selectedDateMillis = entry.testDate,
                onDateSelected = { newValue -> onUpdateField { it.copy(testDate = newValue) } },
                label = stringResource(R.string.field_test_date)
            )
        }

        // ========== Section 11: Additional Technical Details ==========

        // Field Applied Coating Type
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

        // Service Valve Reference Point
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

        // Service Valve Location Direction
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

        // MQAP Version Number - Quality assurance program version
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

        // UserID - Who filled out the form
        item {
            AppTextField(
                value = entry.userId,
                onValueChange = { newValue -> onUpdateField { it.copy(userId = newValue) } },
                label = stringResource(R.string.field_user_id)
            )
        }
    }
}

/**
 * Preview for JobCardTab in single column layout (phone)
 */
@Preview(
    name = "JobCardTab - Phone",
    showBackground = true,
    widthDp = 360
)
@Composable
private fun JobCardTabPhonePreview() {
    GdsGpsCollectionTheme {
        JobCardTab(
            entry = JobCardEntry(
                workOrder = "WO-12345",
                address = "123 Main Street",
                blockLot = "Block 5, Lot 12",
                municipality = "Toronto",
                serviceType = "Residential"
            ),
            onUpdateField = { },
            isWideScreen = false
        )
    }
}

/**
 * Preview for JobCardTab in two column layout (tablet)
 */
@Preview(
    name = "JobCardTab - Tablet",
    showBackground = true,
    widthDp = 800
)
@Composable
private fun JobCardTabTabletPreview() {
    GdsGpsCollectionTheme {
        JobCardTab(
            entry = JobCardEntry(
                workOrder = "WO-12345",
                address = "123 Main Street",
                blockLot = "Block 5, Lot 12",
                municipality = "Toronto",
                serviceType = "Residential",
                serviceDesign = "Underground",
                connectionType = "Permanent"
            ),
            onUpdateField = { },
            isWideScreen = true
        )
    }
}
