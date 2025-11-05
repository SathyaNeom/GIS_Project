package com.enbridge.gpsdeviceproj.ui.map.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.enbridge.gpsdeviceproj.designsystem.components.AppRadioButton
import com.enbridge.gpsdeviceproj.designsystem.components.AppTextField
import com.enbridge.gpsdeviceproj.designsystem.components.PrimaryButton
import com.enbridge.gpsdeviceproj.designsystem.components.SecondaryButton
import com.enbridge.gpsdeviceproj.designsystem.theme.Spacing
import com.enbridge.gpsdeviceproj.domain.entity.ProjectSettings
import com.enbridge.gpsdeviceproj.domain.entity.WorkOrder
import com.enbridge.gpsdeviceproj.ui.map.ProjectSettingsUiState

/**
 * Sealed class to represent the different screens in the bottom sheet
 */
sealed class ProjectSettingsScreen {
    data object WorkOrderSelection : ProjectSettingsScreen()
    data class CrewInformation(val workOrder: WorkOrder) : ProjectSettingsScreen()
}

/**
 * Main Project Settings Bottom Sheet
 * Uses a single bottom sheet with state management for navigation between screens
 *
 * Screen Flow:
 * 1. Work Order Selection: Select pole type, fetch and select work order
 * 2. Crew Information: Edit crew details and save
 *
 * @param onDismissRequest Callback invoked when the bottom sheet should be dismissed
 * @param uiState The UI state containing work orders, project settings, and loading states
 * @param onSelectPoleType Callback when user selects a pole type
 * @param onGetWorkOrders Callback when user clicks "Get WO" button
 * @param onSelectWorkOrder Callback when user selects a work order
 * @param onSearchQueryChange Callback when user types in search field
 * @param onClearSearch Callback when user clears search
 * @param onUpdateProjectSettings Callback when user updates crew information
 * @param onSave Callback when user saves project settings
 * @param onRetryLoadSettings Callback to retry loading project settings
 * @param onRetryLoadWorkOrders Callback to retry loading work orders
 * @param modifier Optional modifier for the bottom sheet
 * @param sheetState State object for controlling the bottom sheet behavior
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectSettingsBottomSheet(
    onDismissRequest: () -> Unit,
    uiState: ProjectSettingsUiState,
    onSelectPoleType: (String) -> Unit,
    onGetWorkOrders: () -> Unit,
    onSelectWorkOrder: (WorkOrder) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onUpdateProjectSettings: (ProjectSettings) -> Unit,
    onSave: () -> Unit,
    onRetryLoadSettings: () -> Unit,
    onRetryLoadWorkOrders: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    // Navigation stack for bottom sheet screens
    var screenStack by remember {
        mutableStateOf<List<ProjectSettingsScreen>>(listOf(ProjectSettingsScreen.WorkOrderSelection))
    }

    val currentScreen = screenStack.last()

    // Handle save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onDismissRequest()
        }
    }

    // Handle save error
    LaunchedEffect(uiState.saveError) {
        uiState.saveError?.let { error ->
        }
    }

    // Handle back navigation within the bottom sheet
    val onBack: () -> Unit = {
        if (screenStack.size > 1) {
            screenStack = screenStack.dropLast(1)
        } else {
            onDismissRequest()
        }
    }

    // Navigate to crew information when work order is selected and Save is clicked
    val onWorkOrderSaveClick: () -> Unit = {
        if (uiState.selectedWorkOrder != null) {
            screenStack =
                screenStack + ProjectSettingsScreen.CrewInformation(uiState.selectedWorkOrder)
        } else {
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.extraLarge,
        dragHandle = {
            // Custom drag handle with visual indicator
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(Spacing.small))
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
                Spacer(modifier = Modifier.height(Spacing.small))
            }
        }
    ) {
        // Animated transitions between screens
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                if (targetState is ProjectSettingsScreen.CrewInformation) {
                    // Forward navigation - slide in from right
                    slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith
                            slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                } else {
                    // Back navigation - slide in from left
                    slideInHorizontally(initialOffsetX = { -it }) + fadeIn() togetherWith
                            slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                }
            },
            label = "project_settings_navigation"
        ) { screen ->
            when (screen) {
                is ProjectSettingsScreen.WorkOrderSelection -> {
                    WorkOrderSelectionScreen(
                        uiState = uiState,
                        onSelectPoleType = onSelectPoleType,
                        onGetWorkOrders = onGetWorkOrders,
                        onSelectWorkOrder = onSelectWorkOrder,
                        onSearchQueryChange = onSearchQueryChange,
                        onClearSearch = onClearSearch,
                        onSave = {
                            // Navigate to crew information screen
                            uiState.selectedWorkOrder?.let { workOrder ->
                                screenStack =
                                    screenStack + ProjectSettingsScreen.CrewInformation(workOrder)
                            }
                        },
                        onRetry = onRetryLoadWorkOrders,
                        onDismiss = onDismissRequest
                    )
                }

                is ProjectSettingsScreen.CrewInformation -> {
                    CrewInformationScreen(
                        workOrder = screen.workOrder,
                        projectSettings = uiState.projectSettings,
                        isLoading = uiState.isLoadingProjectSettings,
                        isSaving = uiState.isSaving,
                        error = uiState.projectSettingsError ?: uiState.saveError,
                        onUpdateProjectSettings = onUpdateProjectSettings,
                        onSave = onSave,
                        onRetry = onRetryLoadSettings,
                        onBack = onBack,
                        onDismiss = onDismissRequest
                    )
                }
            }
        }
    }
}

/**
 * Screen 1: Work Order Selection
 * Displays pole type selection, work order list with search, and Get WO button
 */
@Composable
private fun ColumnScope.WorkOrderSelectionScreen(
    uiState: ProjectSettingsUiState,
    onSelectPoleType: (String) -> Unit,
    onGetWorkOrders: () -> Unit,
    onSelectWorkOrder: (WorkOrder) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.large)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Project Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.extraSmall))

        // Subtitle
        Text(
            text = "Get WO List - Select Workorder Number Around Where You Are Standing",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Spacing.normal))

        // Pole Type Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            AppRadioButton(
                selected = uiState.selectedPoleType == "6 Foot Pole",
                onClick = { onSelectPoleType("6 Foot Pole") },
                label = "6 Foot Pole"
            )
            AppRadioButton(
                selected = uiState.selectedPoleType == "8 Foot Pole",
                onClick = { onSelectPoleType("8 Foot Pole") },
                label = "8 Foot Pole"
            )
            AppRadioButton(
                selected = uiState.selectedPoleType == "Handheld",
                onClick = { onSelectPoleType("Handheld") },
                label = "Handheld"
            )
        }

        Spacer(modifier = Modifier.height(Spacing.normal))

        // Get WO Button
        PrimaryButton(
            text = "Get WO",
            onClick = onGetWorkOrders,
            enabled = !uiState.isLoadingWorkOrders,
            modifier = Modifier.align(Alignment.End)
        )

        Spacer(modifier = Modifier.height(Spacing.normal))

        // Work Order List Label
        Text(
            text = "Work Order List:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(Spacing.small))

        // Content based on state
        when {
            uiState.isLoadingWorkOrders -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.workOrdersError != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.normal),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.workOrdersError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(Spacing.normal))
                    SecondaryButton(
                        text = "Retry",
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            uiState.workOrders.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Click 'Get WO' to fetch work orders",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                // Search Field
                AppTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = "Search by Work Order Number",
                    placeholder = "Enter work order number...",
                    leadingIcon = Icons.Default.Search,
                    trailingIcon = if (uiState.searchQuery.isNotEmpty()) Icons.Default.Close else null,
                    onTrailingIconClick = onClearSearch,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.small))

                // Work Order List
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 1.dp
                ) {
                    if (uiState.filteredWorkOrders.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No work orders found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.padding(Spacing.small)
                        ) {
                            items(uiState.filteredWorkOrders) { workOrder ->
                                WorkOrderListItem(
                                    workOrder = workOrder,
                                    isSelected = uiState.selectedWorkOrder?.id == workOrder.id,
                                    onClick = { onSelectWorkOrder(workOrder) }
                                )
                                Spacer(modifier = Modifier.height(Spacing.extraSmall))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.normal))

        // Selected Work Order Display
        if (uiState.selectedWorkOrder != null) {
            Text(
                text = "Enter WO: ${uiState.selectedWorkOrder.workOrderNumber}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(Spacing.normal))
        }

        // Save Button
        PrimaryButton(
            text = "Save",
            onClick = onSave,
            enabled = uiState.selectedWorkOrder != null && !uiState.isSaving,
            modifier = Modifier.align(Alignment.End)
        )

        Spacer(modifier = Modifier.height(Spacing.large))
    }
}

/**
 * Work Order List Item
 */
@Composable
private fun WorkOrderListItem(
    workOrder: WorkOrder,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
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

/**
 * Screen 2: Crew Information
 * Displays form to edit crew details (contractor, crew ID, supervisor, fitter, welder)
 */
@Composable
private fun ColumnScope.CrewInformationScreen(
    workOrder: WorkOrder,
    projectSettings: ProjectSettings?,
    isLoading: Boolean,
    isSaving: Boolean,
    error: String?,
    onUpdateProjectSettings: (ProjectSettings) -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    // Local state for form fields
    var contractor by remember(projectSettings) {
        mutableStateOf(projectSettings?.contractor ?: "")
    }
    var crewId by remember(projectSettings) {
        mutableStateOf(projectSettings?.crewId ?: "")
    }
    var supervisor by remember(projectSettings) {
        mutableStateOf(projectSettings?.supervisor ?: "")
    }
    var supervisorTSSA by remember { mutableStateOf("") }
    var fitterName by remember(projectSettings) {
        mutableStateOf(projectSettings?.fitterName ?: "")
    }
    var fitterId by remember { mutableStateOf("") }
    var welderName by remember(projectSettings) {
        mutableStateOf(projectSettings?.welderName ?: "")
    }
    var welderId by remember { mutableStateOf("") }
    var inspectedBy by remember { mutableStateOf("") }
    var inspectedByTSSA by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.large)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Crew Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.extraSmall))

        Text(
            text = "Enter crew information",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Spacing.normal))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(Spacing.normal))

        // Content based on state
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.normal),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(Spacing.normal))
                    SecondaryButton(
                        text = "Retry",
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            projectSettings != null -> {
                // Scrollable form
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Spacing.normal),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    item {
                        AppTextField(
                            value = contractor,
                            onValueChange = { contractor = it },
                            label = "Contractor",
                            placeholder = "Enter contractor name",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        AppTextField(
                            value = crewId,
                            onValueChange = { crewId = it },
                            label = "Crew Id",
                            placeholder = "Enter crew ID",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        AppTextField(
                            value = supervisor,
                            onValueChange = { supervisor = it },
                            label = "Supervisor",
                            placeholder = "Enter supervisor name",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        AppTextField(
                            value = supervisorTSSA,
                            onValueChange = { supervisorTSSA = it },
                            label = "Supervisor TSSA#",
                            placeholder = "Enter supervisor TSSA number",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        AppTextField(
                            value = fitterName,
                            onValueChange = { fitterName = it },
                            label = "Fitter Name",
                            placeholder = "Enter fitter name",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        AppTextField(
                            value = fitterId,
                            onValueChange = { fitterId = it },
                            label = "Fitter Id",
                            placeholder = "Enter fitter ID",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        AppTextField(
                            value = welderName,
                            onValueChange = { welderName = it },
                            label = "Welder Name",
                            placeholder = "Enter welder name",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        AppTextField(
                            value = welderId,
                            onValueChange = { welderId = it },
                            label = "Welder Id",
                            placeholder = "Enter welder ID",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        AppTextField(
                            value = inspectedBy,
                            onValueChange = { inspectedBy = it },
                            label = "Inspected By",
                            placeholder = "Enter inspector name",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        AppTextField(
                            value = inspectedByTSSA,
                            onValueChange = { inspectedByTSSA = it },
                            label = "Inspected By TSSA#",
                            placeholder = "Enter inspector TSSA number",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        AppTextField(
                            value = comments,
                            onValueChange = { comments = it },
                            label = "Comments",
                            placeholder = "Enter comments",
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false,
                            maxLines = 4
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.normal))

                // Save Button
                PrimaryButton(
                    text = if (isSaving) "Saving..." else "Save",
                    onClick = {
                        // Update project settings with form data
                        val updatedSettings = projectSettings.copy(
                            contractor = contractor,
                            crewId = crewId,
                            supervisor = supervisor,
                            fitterName = fitterName,
                            welderName = welderName
                        )
                        onUpdateProjectSettings(updatedSettings)
                        onSave()
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.large))
    }
}
