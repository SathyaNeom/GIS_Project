package com.enbridge.gdsgpscollection.ui.map.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.enbridge.gdsgpscollection.R
import com.enbridge.gdsgpscollection.designsystem.components.AppTextField
import com.enbridge.gdsgpscollection.designsystem.components.PrimaryButton
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing
import com.enbridge.gdsgpscollection.domain.entity.ProjectSettings
import com.enbridge.gdsgpscollection.domain.entity.WorkOrder
import com.enbridge.gdsgpscollection.ui.map.ProjectSettingsUiState
import com.enbridge.gdsgpscollection.ui.map.components.common.BottomSheetHeader
import com.enbridge.gdsgpscollection.ui.map.components.common.EmptyState
import com.enbridge.gdsgpscollection.ui.map.components.common.LoadingErrorState
import com.enbridge.gdsgpscollection.ui.map.components.projectsettings.PoleTypeSelector
import com.enbridge.gdsgpscollection.ui.map.components.projectsettings.WorkOrderListItem

/**
 * Sealed class to represent the different screens in the bottom sheet
 */
sealed class ProjectSettingsScreen {
    data object WorkOrderSelection : ProjectSettingsScreen()
    data class CrewInformation(val workOrder: WorkOrder) : ProjectSettingsScreen()
}

/**
 * Main Project Settings Bottom Sheet (Refactored)
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
    var screenStack by remember {
        mutableStateOf<List<ProjectSettingsScreen>>(listOf(ProjectSettingsScreen.WorkOrderSelection))
    }

    val currentScreen = screenStack.last()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onDismissRequest()
        }
    }

    val onBack: () -> Unit = {
        if (screenStack.size > 1) {
            screenStack = screenStack.dropLast(1)
        } else {
            onDismissRequest()
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
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                if (targetState is ProjectSettingsScreen.CrewInformation) {
                    slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith
                            slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                } else {
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
 * Screen 1: Work Order Selection (Refactored)
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
        // Use extracted BottomSheetHeader component
        BottomSheetHeader(
            title = stringResource(R.string.projectsettings_title),
            subtitle = stringResource(R.string.projectsettings_get_wo_list),
            onClose = onDismiss
        )

        Spacer(modifier = Modifier.height(Spacing.normal))

        // Use extracted PoleTypeSelector component
        PoleTypeSelector(
            selectedPoleType = uiState.selectedPoleType,
            onSelectPoleType = onSelectPoleType
        )

        Spacer(modifier = Modifier.height(Spacing.normal))

        PrimaryButton(
            text = stringResource(R.string.projectsettings_get_wo),
            onClick = onGetWorkOrders,
            enabled = !uiState.isLoadingWorkOrders,
            modifier = Modifier.align(Alignment.End)
        )

        Spacer(modifier = Modifier.height(Spacing.normal))

        Text(
            text = stringResource(R.string.projectsettings_work_order_list),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(Spacing.small))

        // Use extracted LoadingErrorState component
        when {
            uiState.isLoadingWorkOrders || uiState.workOrdersError != null -> {
                LoadingErrorState(
                    isLoading = uiState.isLoadingWorkOrders,
                    error = uiState.workOrdersError,
                    onRetry = onRetry,
                    loadingHeight = 200
                )
            }

            uiState.workOrders.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(message = stringResource(R.string.projectsettings_click_get_wo))
                }
            }

            else -> {
                AppTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = stringResource(R.string.projectsettings_search_by_work_order_number),
                    placeholder = stringResource(R.string.projectsettings_enter_work_order_number),
                    leadingIcon = Icons.Default.Search,
                    trailingIcon = if (uiState.searchQuery.isNotEmpty()) Icons.Default.Close else null,
                    onTrailingIconClick = onClearSearch,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.small))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 1.dp
                ) {
                    if (uiState.filteredWorkOrders.isEmpty()) {
                        EmptyState(message = stringResource(R.string.projectsettings_no_work_orders_found))
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

        if (uiState.selectedWorkOrder != null) {
            Text(
                text = stringResource(
                    R.string.projectsettings_enter_wo,
                    uiState.selectedWorkOrder.workOrderNumber
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(Spacing.normal))
        }

        PrimaryButton(
            text = stringResource(R.string.action_save),
            onClick = onSave,
            enabled = uiState.selectedWorkOrder != null && !uiState.isSaving,
            modifier = Modifier.align(Alignment.End)
        )

        Spacer(modifier = Modifier.height(Spacing.large))
    }
}

/**
 * Screen 2: Crew Information (Refactored)
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
        // Use extracted BottomSheetHeader component with back button
        BottomSheetHeader(
            title = stringResource(R.string.projectsettings_crew_info),
            subtitle = stringResource(R.string.projectsettings_enter_crew_info),
            onBack = onBack,
            onClose = onDismiss
        )

        Spacer(modifier = Modifier.height(Spacing.normal))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(Spacing.normal))

        // Use extracted LoadingErrorState component
        when {
            isLoading || error != null -> {
                LoadingErrorState(
                    isLoading = isLoading,
                    error = error,
                    onRetry = onRetry,
                    loadingHeight = 400
                )
            }

            projectSettings != null -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Spacing.normal),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    item {
                        AppTextField(
                            value = contractor,
                            onValueChange = { contractor = it },
                            label = stringResource(R.string.projectsettings_contractor),
                            placeholder = stringResource(R.string.projectsettings_enter_contractor_name),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        AppTextField(
                            value = crewId,
                            onValueChange = { crewId = it },
                            label = stringResource(R.string.projectsettings_crew_id),
                            placeholder = stringResource(R.string.projectsettings_enter_crew_id),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        AppTextField(
                            value = supervisor,
                            onValueChange = { supervisor = it },
                            label = stringResource(R.string.projectsettings_supervisor),
                            placeholder = stringResource(R.string.projectsettings_enter_supervisor_name),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        AppTextField(
                            value = supervisorTSSA,
                            onValueChange = { supervisorTSSA = it },
                            label = stringResource(R.string.projectsettings_supervisor_tssa),
                            placeholder = stringResource(R.string.projectsettings_enter_supervisor_tssa),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        AppTextField(
                            value = fitterName,
                            onValueChange = { fitterName = it },
                            label = stringResource(R.string.projectsettings_fitter_name),
                            placeholder = stringResource(R.string.projectsettings_enter_fitter_name),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        AppTextField(
                            value = fitterId,
                            onValueChange = { fitterId = it },
                            label = stringResource(R.string.projectsettings_fitter_id),
                            placeholder = stringResource(R.string.projectsettings_enter_fitter_id),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        AppTextField(
                            value = welderName,
                            onValueChange = { welderName = it },
                            label = stringResource(R.string.projectsettings_welder_name),
                            placeholder = stringResource(R.string.projectsettings_enter_welder_name),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        AppTextField(
                            value = welderId,
                            onValueChange = { welderId = it },
                            label = stringResource(R.string.projectsettings_welder_id),
                            placeholder = stringResource(R.string.projectsettings_enter_welder_id),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        AppTextField(
                            value = inspectedBy,
                            onValueChange = { inspectedBy = it },
                            label = stringResource(R.string.projectsettings_inspected_by),
                            placeholder = stringResource(R.string.projectsettings_enter_inspector_name),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        AppTextField(
                            value = inspectedByTSSA,
                            onValueChange = { inspectedByTSSA = it },
                            label = stringResource(R.string.projectsettings_inspected_by_tssa),
                            placeholder = stringResource(R.string.projectsettings_enter_inspector_tssa),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        AppTextField(
                            value = comments,
                            onValueChange = { comments = it },
                            label = stringResource(R.string.projectsettings_comments),
                            placeholder = stringResource(R.string.projectsettings_enter_comments),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false,
                            maxLines = 4
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.normal))

                PrimaryButton(
                    text = if (isSaving) stringResource(R.string.projectsettings_saving) else stringResource(
                        R.string.action_save
                    ),
                    onClick = {
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
