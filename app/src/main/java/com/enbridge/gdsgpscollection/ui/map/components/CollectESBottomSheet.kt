package com.enbridge.gdsgpscollection.ui.map.components

/**
 * @author Sathya Narayanan
 */

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.enbridge.gdsgpscollection.designsystem.components.AppTextField
import com.enbridge.gdsgpscollection.designsystem.components.PrimaryButton
import com.enbridge.gdsgpscollection.designsystem.components.SecondaryButton
import com.enbridge.gdsgpscollection.designsystem.components.SingleSelectDropdown
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing
import com.enbridge.gdsgpscollection.ui.map.CollectESUiState
import com.enbridge.gdsgpscollection.ui.map.components.common.BottomSheetHeader
import com.enbridge.gdsgpscollection.ui.map.components.common.EmptyState
import com.enbridge.gdsgpscollection.ui.map.components.common.LoadingErrorState
import com.enbridge.gdsgpscollection.ui.map.models.AttributeType
import com.enbridge.gdsgpscollection.ui.map.models.FeatureAttribute
import com.enbridge.gdsgpscollection.ui.map.models.FeatureType
import kotlinx.coroutines.launch

/**
 * Mapper to convert domain FeatureType to UI FeatureType
 */
private fun com.enbridge.gdsgpscollection.domain.entity.FeatureType.toUiModel(): FeatureType {
    return FeatureType(
        id = id,
        name = name,
        legendColor = Color(android.graphics.Color.parseColor(legendColor)),
        attributes = attributes.map { it.toUiModel() }
    )
}

/**
 * Mapper to convert domain FeatureAttribute to UI FeatureAttribute
 */
private fun com.enbridge.gdsgpscollection.domain.entity.FeatureAttribute.toUiModel(): FeatureAttribute {
    return FeatureAttribute(
        id = id,
        label = label,
        type = when (type) {
            com.enbridge.gdsgpscollection.domain.entity.AttributeType.TEXT -> AttributeType.TEXT
            com.enbridge.gdsgpscollection.domain.entity.AttributeType.TEXTMULTILINE -> AttributeType.TEXTMULTILINE
            com.enbridge.gdsgpscollection.domain.entity.AttributeType.NUMBER -> AttributeType.NUMBER
            com.enbridge.gdsgpscollection.domain.entity.AttributeType.DROPDOWN -> AttributeType.DROPDOWN
            com.enbridge.gdsgpscollection.domain.entity.AttributeType.DATE -> AttributeType.DATE
            com.enbridge.gdsgpscollection.domain.entity.AttributeType.LOCATION -> AttributeType.LOCATION
        },
        isRequired = isRequired,
        options = options,
        hint = hint,
        value = defaultValue
    )
}

/**
 * Sealed class to represent the different screens in the bottom sheet
 */
sealed class BottomSheetScreen {
    data object ChooseFeatureType : BottomSheetScreen()
    data class EditAttribute(val featureType: FeatureType) : BottomSheetScreen()
}

/**
 * Main Collect Electronic Services Bottom Sheet
 * Uses a single bottom sheet with state management for navigation between screens
 *
 * @param onDismissRequest Callback invoked when the bottom sheet should be dismissed
 * @param onSave Callback invoked when user saves the feature with attributes
 * @param uiState The UI state containing feature types and loading/error states
 * @param onRetry Callback to retry loading feature types in case of error
 * @param modifier Optional modifier for the bottom sheet
 * @param sheetState State object for controlling the bottom sheet behavior
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectESBottomSheet(
    onDismissRequest: () -> Unit,
    onSave: (FeatureType, Map<String, String>) -> Unit,
    uiState: CollectESUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    // Navigation stack for bottom sheet screens
    // Starts with ChooseFeatureType screen
    var screenStack by remember {
        mutableStateOf<List<BottomSheetScreen>>(listOf(BottomSheetScreen.ChooseFeatureType))
    }

    val currentScreen = screenStack.last()

    // Handle back navigation within the bottom sheet
    // If there are multiple screens in the stack, go back to previous screen
    // Otherwise, dismiss the entire bottom sheet
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
            // Custom drag handle with visual indicator
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(Spacing.small))
                // Visual drag handle - a rounded bar at the top
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
        // Slides horizontally when navigating forward/backward
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                if (targetState is BottomSheetScreen.EditAttribute) {
                    // Forward navigation - slide in from right
                    slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith
                            slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                } else {
                    // Back navigation - slide in from left
                    slideInHorizontally(initialOffsetX = { -it }) + fadeIn() togetherWith
                            slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                }
            },
            label = "bottom_sheet_navigation"
        ) { screen ->
            when (screen) {
                is BottomSheetScreen.ChooseFeatureType -> {
                    ChooseFeatureTypeScreen(
                        featureTypes = uiState.featureTypes.map { it.toUiModel() },
                        isLoading = uiState.isLoading,
                        error = uiState.error,
                        onFeatureTypeSelected = { featureType ->
                            // Navigate to EditAttribute screen when a feature type is selected
                            screenStack = screenStack + BottomSheetScreen.EditAttribute(featureType)
                        },
                        onRetry = onRetry,
                        onDismiss = onDismissRequest
                    )
                }

                is BottomSheetScreen.EditAttribute -> {
                    EditAttributeScreen(
                        featureType = screen.featureType,
                        onBack = onBack,
                        onSave = { attributes ->
                            onSave(screen.featureType, attributes)
                            onDismissRequest()
                        },
                        onSelectAsset = { /* Handle select asset */ },
                        onDismiss = onDismissRequest
                    )
                }
            }
        }
    }
}

/**
 * Choose Feature Type Screen
 * Displays a list of available feature types for the user to select from
 * Shows loading state, error state, or the list of feature types
 *
 * @param featureTypes List of feature types to display
 * @param isLoading Whether data is currently being loaded
 * @param error Error message to display, if any
 * @param onFeatureTypeSelected Callback when a feature type is selected
 * @param onRetry Callback to retry loading data after an error
 * @param onDismiss Callback to dismiss the bottom sheet
 */
@Composable
private fun ColumnScope.ChooseFeatureTypeScreen(
    featureTypes: List<FeatureType>,
    isLoading: Boolean,
    error: String?,
    onFeatureTypeSelected: (FeatureType) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.large)
    ) {
        // Use extracted BottomSheetHeader component
        BottomSheetHeader(
            title = "Choose Feature Type",
            subtitle = "Select feature layer to collect points.",
            onClose = onDismiss
        )

        Spacer(modifier = Modifier.height(Spacing.normal))

        // Use extracted LoadingErrorState component
        when {
            isLoading || error != null -> {
                LoadingErrorState(
                    isLoading = isLoading,
                    error = error,
                    onRetry = onRetry,
                    loadingHeight = 200
                )
            }

            featureTypes.isEmpty() -> {
                EmptyState(message = "No feature types available")
            }

            else -> {
                // Display feature type list using extracted component
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Spacing.small),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(featureTypes) { featureType ->
                        FeatureTypeListItem(
                            featureType = featureType,
                            onClick = { onFeatureTypeSelected(featureType) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.large))
    }
}

/**
 * Feature Type List Item with legend indicator and name
 */
@Composable
private fun FeatureTypeListItem(
    featureType: FeatureType,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.normal),
            horizontalArrangement = Arrangement.spacedBy(Spacing.normal),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Legend indicator (colored circle)
            Canvas(
                modifier = Modifier.size(24.dp)
            ) {
                drawCircle(
                    color = featureType.legendColor,
                    radius = size.minDimension / 2
                )
            }

            Text(
                text = featureType.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Edit Attribute Screen
 * Allows users to edit attributes for a selected feature type
 * Includes form validation and prevents bottom sheet dismissal while scrolling
 *
 * @param featureType The selected feature type with its attributes
 * @param onBack Callback to navigate back to previous screen
 * @param onSave Callback when user saves the attributes
 * @param onSelectAsset Callback when user wants to select an asset
 * @param onDismiss Callback to dismiss the bottom sheet
 */
@Composable
private fun ColumnScope.EditAttributeScreen(
    featureType: FeatureType,
    onBack: () -> Unit,
    onSave: (Map<String, String>) -> Unit,
    onSelectAsset: () -> Unit,
    onDismiss: () -> Unit
) {
    // State to hold attribute values
    // Initialize with default values from the feature type
    val attributeValues = remember {
        mutableStateOf(
            featureType.attributes.associate { it.id to it.value }.toMutableMap()
        )
    }

    // Snackbar for showing validation errors
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // LazyListState to track scroll position
    // This is used to determine if content is scrollable and prevent bottom sheet dismissal
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.large)
    ) {
        // Use extracted BottomSheetHeader component with back button
        BottomSheetHeader(
            title = "Edit Attribute",
            onBack = onBack,
            onClose = onDismiss
        )

        // Feature type name
        Text(
            text = featureType.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 48.dp)
        )

        Spacer(modifier = Modifier.height(Spacing.extraSmall))

        Text(
            text = "Enbridge Edit Attribute Task",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 48.dp)
        )

        Spacer(modifier = Modifier.height(Spacing.normal))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(Spacing.normal))

        // Attribute fields using extracted AttributeField component
        Box(modifier = Modifier.weight(1f, fill = true)) {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(Spacing.normal),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(featureType.attributes) { attribute ->
                    AttributeField(
                        attribute = attribute,
                        value = attributeValues.value[attribute.id] ?: "",
                        onValueChange = { newValue ->
                            // Update the attribute value in the mutable map
                            attributeValues.value = attributeValues.value.toMutableMap().apply {
                                put(attribute.id, newValue)
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.large))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            SecondaryButton(
                text = "Select Asset",
                onClick = onSelectAsset,
                modifier = Modifier.weight(1f)
            )

            PrimaryButton(
                text = "Save",
                onClick = {
                    // Validate required fields before saving
                    val hasEmptyRequiredFields = featureType.attributes.any { attribute ->
                        attribute.isRequired && (attributeValues.value[attribute.id]
                            ?: "").isEmpty()
                    }

                    if (hasEmptyRequiredFields) {
                        // Show validation error
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Please fill all required fields",
                                actionLabel = "OK",
                                withDismissAction = true
                            )
                        }
                    } else {
                        // All validations passed, save the data
                        onSave(attributeValues.value)
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(Spacing.large))
        SnackbarHost(hostState = snackbarHostState)
    }
}

/**
 * Attribute Field component that renders different input types
 */
@Composable
private fun AttributeField(
    attribute: FeatureAttribute,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        when (attribute.type) {
            AttributeType.LOCATION -> {
                // Location field with icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (attribute.isRequired) "${attribute.label} * :" else "${attribute.label} :",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            AttributeType.DROPDOWN -> {
                Text(
                    text = if (attribute.isRequired) "${attribute.label} * :" else "${attribute.label} :",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Spacing.extraSmall))
                SingleSelectDropdown(
                    items = attribute.options,
                    selectedItem = if (value.isEmpty()) null else value,
                    onItemSelected = onValueChange,
                    label = if (attribute.hint.isNotEmpty()) attribute.hint else attribute.label,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AttributeType.TEXT, AttributeType.TEXTMULTILINE, AttributeType.NUMBER -> {
                AppTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = if (attribute.isRequired) "${attribute.label} * :" else "${attribute.label} :",
                    placeholder = attribute.hint.takeIf { it.isNotEmpty() },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = attribute.type != AttributeType.TEXTMULTILINE,
                    maxLines = if (attribute.type == AttributeType.TEXTMULTILINE) 4 else 1
                )
            }

            AttributeType.DATE -> {
                AppTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = if (attribute.isRequired) "${attribute.label} * :" else "${attribute.label} :",
                    placeholder = "MM/DD/YYYY",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
