package com.enbridge.electronicservices.feature.map

/**
 * @author Sathya Narayanan
 * This package contains the feature map functionality for electronic services.
 */

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.toolkit.geoviewcompose.MapView
import com.enbridge.electronicservices.designsystem.components.AppFloatingActionButton
import com.enbridge.electronicservices.designsystem.components.AppDialog
import com.enbridge.electronicservices.designsystem.components.DialogType
import com.enbridge.electronicservices.designsystem.components.PrimaryButton
import com.enbridge.electronicservices.designsystem.components.AppIconButton
import com.enbridge.electronicservices.designsystem.components.AppRadioButton
import com.enbridge.electronicservices.designsystem.components.AppScaffold
import com.enbridge.electronicservices.designsystem.components.AppSnackbarHost
import com.enbridge.electronicservices.designsystem.components.AppTextButton
import com.enbridge.electronicservices.designsystem.components.AppTopBar
import com.enbridge.electronicservices.designsystem.components.SnackbarType
import com.enbridge.electronicservices.designsystem.components.ESNavigationDrawerContent
import com.enbridge.electronicservices.feature.map.components.CollectESBottomSheet
import com.enbridge.electronicservices.feature.map.components.ManageESBottomSheet
import com.enbridge.electronicservices.feature.map.components.ProjectSettingsBottomSheet
import com.enbridge.electronicservices.designsystem.theme.ElectronicServicesTheme
import com.enbridge.electronicservices.designsystem.theme.Spacing
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enbridge.electronicservices.feature.map.CollectESViewModel
import com.enbridge.electronicservices.feature.map.ProjectSettingsViewModel

/**
 * MainMapScreen displays the primary map interface for electronic services.
 *
 * This screen provides comprehensive GIS functionality including:
 * - Interactive ArcGIS map with multiple basemap options
 * - Feature identification and measurement tools
 * - Map navigation controls (zoom, pan, fullscreen)
 * - Bottom sheet for collecting and managing electronic service data
 * - Real-time coordinate and scale information display
 * - Integration with job card entry functionality
 *
 * The map is initialized with a view centered on Toronto, Canada, and supports
 * various basemap styles (Streets, Imagery, Topographic, etc.) for different
 * operational requirements.
 *
 * @param onNavigateToJobCardEntry Callback to navigate to the job card entry screen
 * @param onAddNewFeature Callback invoked when adding a new feature to the map
 * @param onSearchClick Callback invoked when the user initiates a search
 * @param onLogout Callback invoked when the user logs out
 * @param appName The name of the application (from BuildConfig, defaults to "Electronic Services")
 * @param appVariant The variant of the application (from BuildConfig, determines feature visibility)
 * @param modifier Optional modifier for this composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMapScreen(
    onNavigateToJobCardEntry: () -> Unit,
    onAddNewFeature: () -> Unit,
    onSearchClick: () -> Unit,
    onLogout: () -> Unit = {},
    appName: String = "Electronic Services",
    appVariant: String = "electronic",
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val collectESViewModel: CollectESViewModel = hiltViewModel()
    val projectSettingsViewModel: ProjectSettingsViewModel = hiltViewModel()

    // Get device configuration for tablet detection
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    // Collect UI state from the CollectES ViewModel
    // This is scoped to the MainMapScreen so data persists between bottom sheet open/close
    val collectESUiState by collectESViewModel.uiState.collectAsStateWithLifecycle()

    // Collect UI state from ProjectSettingsViewModel
    val projectSettingsUiState by projectSettingsViewModel.uiState.collectAsStateWithLifecycle()

    var selectedNavItem by remember { mutableStateOf("map") }
    var isToolbarExpanded by remember { mutableStateOf(false) }
    var showBasemapDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var identifyMode by remember { mutableStateOf(false) }
    var measurementMode by remember { mutableStateOf(false) }
    var selectedBasemapStyle: BasemapStyle by remember { mutableStateOf(BasemapStyle.ArcGISTopographic) }
    var isFullscreen by remember { mutableStateOf(false) }
    var showCollectESBottomSheet by remember { mutableStateOf(false) }
    var showManageESBottomSheet by remember { mutableStateOf(false) }
    var showProjectSettingsBottomSheet by remember { mutableStateOf(false) }

    // Determine if Job Card Entry should be shown (only for electronic variant)
    val showJobCardEntry = appVariant == "electronic"

    // Coordinate state
    var currentX by remember { mutableStateOf("--") }
    var currentY by remember { mutableStateOf("--") }
    var currentScale by remember { mutableStateOf("--") }
    var currentScaleValue by remember { mutableStateOf(1e8) }
    var currentAccuracy by remember { mutableStateOf("--") }
    var currentElevation by remember { mutableStateOf("--") }

    // Handle back press - show logout dialog when drawer is closed
    BackHandler(enabled = true) {
        when {
            drawerState.isOpen -> {
                scope.launch {
                    drawerState.close()
                }
            }

            else -> {
                // Show logout confirmation dialog
                showLogoutDialog = true
            }
        }
    }

    // Create the ArcGIS Map - recreate when basemap style changes
    // Initial viewpoint is set to Toronto, Canada
    // Coordinates: Longitude -79.3832, Latitude 43.6532 (in WGS84)
    // Converted to Web Mercator spatial reference for ArcGIS compatibility
    // Scale of 144,447 (~1:144k) provides a city-wide view showing Toronto's downtown and surrounding areas
    var map by remember {
        mutableStateOf(
            ArcGISMap(selectedBasemapStyle).apply {
                initialViewpoint = Viewpoint(
                    center = Point(-8833785.0, 5397884.0, SpatialReference.webMercator()),
                    scale = 144447.0
                )
            }
        )
    }

    // Update map when basemap style changes
    // Maintains the same initial viewpoint (Toronto) when user switches basemap layers
    LaunchedEffect(selectedBasemapStyle) {
        map = ArcGISMap(selectedBasemapStyle).apply {
            initialViewpoint = Viewpoint(
                center = Point(-8833785.0, 5397884.0, SpatialReference.webMercator()),
                scale = 144447.0
            )
        }
    }

    // Handle Project Settings save success
    LaunchedEffect(projectSettingsUiState.saveSuccess) {
        if (projectSettingsUiState.saveSuccess) {
            snackbarHostState.showSnackbar("Project settings saved successfully")
            showProjectSettingsBottomSheet = false
            projectSettingsViewModel.resetSaveSuccess()
        }
    }

    ElectronicServicesTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = false,
            drawerContent = {
                ESNavigationDrawerContent(
                    onCollectESClick = {
                        showCollectESBottomSheet = true
                    },
                    onESJobCardEntryClick = {
                        onNavigateToJobCardEntry()
                    },
                    onProjectSettingsClick = {
                        showProjectSettingsBottomSheet = true
                    },
                    onManageESEditsClick = {
                        showManageESBottomSheet = true
                    },
                    drawerState = drawerState,
                    scope = scope,
                    showJobCardEntry = showJobCardEntry
                )
            }
        ) {
            AppScaffold(
                modifier = modifier,
                snackbarHost = {
                    AppSnackbarHost(
                        hostState = snackbarHostState,
                        snackbarType = SnackbarType.INFO
                    )
                },
                topBar = {
                    AppTopBar(
                        title = appName,
                        navigationIcon = {
                            AppIconButton(
                                icon = Icons.Default.Menu,
                                contentDescription = "Menu",
                                onClick = { scope.launch { drawerState.open() } }
                            )
                        },
                        onActionClick = onSearchClick
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                ) {
                    // ArcGIS MapView
                    MapView(
                        arcGISMap = map,
                        modifier = Modifier.fillMaxSize(),
                        onViewpointChangedForCenterAndScale = { viewpoint ->
                            viewpoint?.targetGeometry?.let { point ->
                                if (point is Point) {
                                    currentX = String.format("%.4f", point.x)
                                    currentY = String.format("%.4f", point.y)
                                }
                            }
                            viewpoint?.targetScale?.let { scale ->
                                currentScale = String.format("%.0f", scale)
                                currentScaleValue = scale
                            }
                        },
                        onSingleTapConfirmed = { event ->
                            val mapPoint = event.mapPoint
                            if (mapPoint != null) {
                                currentX = String.format("%.4f", mapPoint.x)
                                currentY = String.format("%.4f", mapPoint.y)

                                // Handle identify mode
                                if (identifyMode) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Feature identified at X: ${currentX}, Y: ${currentY}"
                                        )
                                        identifyMode = false
                                        isToolbarExpanded = false
                                    }
                                }
                            }
                        }
                    )

                    // Floating Action Button with Expandable Toolbar
                    // Expands horizontally on tablets (>= 600dp) and vertically on phones
                    if (isTablet) {
                        // Horizontal layout for tablets
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.End + WindowInsetsSides.Top))
                                .padding(end = Spacing.normal, top = Spacing.normal),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(
                                Spacing.small,
                                Alignment.End
                            )
                        ) {
                            // Expandable toolbar buttons
                            AnimatedVisibility(
                                visible = isToolbarExpanded,
                                enter = expandHorizontally() + fadeIn(),
                                exit = shrinkHorizontally() + fadeOut()
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Zoom In
                                    MapControlButton(
                                        icon = Icons.Default.Add,
                                        contentDescription = "Zoom In",
                                        onClick = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Zoom In")
                                            }
                                        }
                                    )

                                    // Zoom Out
                                    MapControlButton(
                                        icon = Icons.Default.Remove,
                                        contentDescription = "Zoom Out",
                                        onClick = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Zoom Out")
                                            }
                                        }
                                    )

                                    // Fullscreen
                                    MapControlButton(
                                        icon = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                        contentDescription = if (isFullscreen) "Exit Fullscreen" else "Fullscreen",
                                        onClick = {
                                            isFullscreen = !isFullscreen
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    if (isFullscreen) "Fullscreen mode enabled" else "Fullscreen mode disabled"
                                                )
                                            }
                                            isToolbarExpanded = false
                                        }
                                    )

                                    // Identify
                                    MapControlButton(
                                        icon = Icons.Default.Info,
                                        contentDescription = "Identify",
                                        onClick = {
                                            identifyMode = true
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Tap on feature")
                                            }
                                        }
                                    )

                                    // Layers
                                    MapControlButton(
                                        icon = Icons.Default.Layers,
                                        contentDescription = "Layers",
                                        onClick = {
                                            showBasemapDialog = true
                                            isToolbarExpanded = false
                                        }
                                    )

                                    // Clear
                                    MapControlButton(
                                        icon = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        onClick = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Clear selections")
                                            }
                                            isToolbarExpanded = false
                                        }
                                    )

                                    // My Location
                                    MapControlButton(
                                        icon = Icons.Default.MyLocation,
                                        contentDescription = "My Location",
                                        onClick = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Location feature not yet implemented")
                                            }
                                            isToolbarExpanded = false
                                        }
                                    )

                                    // Measure
                                    MapControlButton(
                                        icon = Icons.Default.Straighten,
                                        contentDescription = "Measure",
                                        onClick = {
                                            measurementMode = !measurementMode
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    if (measurementMode) "Measurement mode enabled" else "Measurement mode disabled"
                                                )
                                            }
                                            isToolbarExpanded = false
                                        }
                                    )
                                }
                            }

                            // Main FAB
                            FloatingActionButton(
                                onClick = { isToolbarExpanded = !isToolbarExpanded },
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Icon(
                                    imageVector = if (isToolbarExpanded) Icons.Default.Close else Icons.Default.Menu,
                                    contentDescription = "Map Controls"
                                )
                            }
                        }
                    } else {
                        // Vertical layout for phones
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.End + WindowInsetsSides.Top))
                                .padding(end = Spacing.normal, top = Spacing.normal),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(
                                Spacing.small,
                                Alignment.Top
                            )
                        ) {
                            // Expandable toolbar buttons
                            AnimatedVisibility(
                                visible = isToolbarExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(Spacing.small),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    // Zoom In
                                    MapControlButton(
                                        icon = Icons.Default.Add,
                                        contentDescription = "Zoom In",
                                        onClick = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Zoom In")
                                            }
                                        }
                                    )

                                    // Zoom Out
                                    MapControlButton(
                                        icon = Icons.Default.Remove,
                                        contentDescription = "Zoom Out",
                                        onClick = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Zoom Out")
                                            }
                                        }
                                    )

                                    // Fullscreen
                                    MapControlButton(
                                        icon = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                        contentDescription = if (isFullscreen) "Exit Fullscreen" else "Fullscreen",
                                        onClick = {
                                            isFullscreen = !isFullscreen
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    if (isFullscreen) "Fullscreen mode enabled" else "Fullscreen mode disabled"
                                                )
                                            }
                                            isToolbarExpanded = false
                                        }
                                    )

                                    // Identify
                                    MapControlButton(
                                        icon = Icons.Default.Info,
                                        contentDescription = "Identify",
                                        onClick = {
                                            identifyMode = true
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Tap on feature")
                                            }
                                        }
                                    )

                                    // Layers
                                    MapControlButton(
                                        icon = Icons.Default.Layers,
                                        contentDescription = "Layers",
                                        onClick = {
                                            showBasemapDialog = true
                                            isToolbarExpanded = false
                                        }
                                    )

                                    // Clear
                                    MapControlButton(
                                        icon = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        onClick = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Clear selections")
                                            }
                                            isToolbarExpanded = false
                                        }
                                    )

                                    // My Location
                                    MapControlButton(
                                        icon = Icons.Default.MyLocation,
                                        contentDescription = "My Location",
                                        onClick = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Location feature not yet implemented")
                                            }
                                            isToolbarExpanded = false
                                        }
                                    )

                                    // Measure
                                    MapControlButton(
                                        icon = Icons.Default.Straighten,
                                        contentDescription = "Measure",
                                        onClick = {
                                            measurementMode = !measurementMode
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    if (measurementMode) "Measurement mode enabled" else "Measurement mode disabled"
                                                )
                                            }
                                            isToolbarExpanded = false
                                        }
                                    )
                                }
                            }

                            // Main FAB
                            FloatingActionButton(
                                onClick = { isToolbarExpanded = !isToolbarExpanded },
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Icon(
                                    imageVector = if (isToolbarExpanded) Icons.Default.Close else Icons.Default.Menu,
                                    contentDescription = "Map Controls"
                                )
                            }
                        }
                    }

                    // Coordinate Info Bar (hide in fullscreen mode)
                    if (!isFullscreen) {
                        CoordinateInfoBar(
                            accuracy = currentAccuracy,
                            x = currentX,
                            y = currentY,
                            elevation = currentElevation,
                            scale = currentScale,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .windowInsetsPadding(
                                    WindowInsets.systemBars.only(
                                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                                    )
                                )
                        )
                    }

                    // Measurement mode indicator
                    if (measurementMode) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = Spacing.normal),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.medium,
                            tonalElevation = 4.dp
                        ) {
                            Text(
                                text = "Measurement mode active",
                                modifier = Modifier.padding(
                                    horizontal = Spacing.normal,
                                    vertical = Spacing.small
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Identify mode indicator
                    if (identifyMode) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = Spacing.normal),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = MaterialTheme.shapes.medium,
                            tonalElevation = 4.dp
                        ) {
                            Text(
                                text = "Identify mode",
                                modifier = Modifier.padding(
                                    horizontal = Spacing.normal,
                                    vertical = Spacing.small
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            AppDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = "Logout Confirmation",
                type = DialogType.INFO,
                content = {
                    Text(
                        text = "Are you sure you want to log out?",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    PrimaryButton(
                        text = "Logout",
                        onClick = {
                            showLogoutDialog = false
                            onLogout()
                        }
                    )
                },
                dismissButton = {
                    AppTextButton(
                        text = "Cancel",
                        onClick = { showLogoutDialog = false }
                    )
                }
            )
        }

        // Basemap Selector Dialog
        if (showBasemapDialog) {
            BasemapSelectorDialog(
                currentBasemap = selectedBasemapStyle,
                onBasemapSelected = { basemap ->
                    selectedBasemapStyle = basemap
                    showBasemapDialog = false
                },
                onDismiss = { showBasemapDialog = false }
            )
        }

        // CollectES Bottom Sheet
        if (showCollectESBottomSheet) {
            CollectESBottomSheet(
                onDismissRequest = {
                    showCollectESBottomSheet = false
                },
                uiState = collectESUiState,
                onRetry = {
                    collectESViewModel.loadFeatureTypes()
                },
                onSave = { featureType, attributes ->
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Saved feature: ${featureType.name} with ${attributes.size} attributes"
                        )
                    }
                }
            )
        }

        // ManageES Bottom Sheet
        if (showManageESBottomSheet) {
            ManageESBottomSheet(
                onDismissRequest = {
                    showManageESBottomSheet = false
                },
                onPostDataSnackbar = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Post data")
                    }
                }
            )
        }

        // Project Settings Bottom Sheet
        if (showProjectSettingsBottomSheet) {
            ProjectSettingsBottomSheet(
                onDismissRequest = {
                    showProjectSettingsBottomSheet = false
                    projectSettingsViewModel.resetState()
                },
                uiState = projectSettingsUiState,
                onSelectPoleType = { poleType ->
                    projectSettingsViewModel.selectPoleType(poleType)
                },
                onGetWorkOrders = {
                    projectSettingsViewModel.getWorkOrders()
                },
                onSelectWorkOrder = { workOrder ->
                    projectSettingsViewModel.selectWorkOrder(workOrder)
                },
                onSearchQueryChange = { query ->
                    projectSettingsViewModel.updateSearchQuery(query)
                },
                onClearSearch = {
                    projectSettingsViewModel.clearSearch()
                },
                onUpdateProjectSettings = { settings ->
                    projectSettingsViewModel.updateProjectSettings(settings)
                },
                onSave = {
                    projectSettingsViewModel.saveProjectSettings()
                },
                onRetryLoadSettings = {
                    projectSettingsViewModel.loadProjectSettings()
                },
                onRetryLoadWorkOrders = {
                    projectSettingsViewModel.getWorkOrders()
                }
            )
        }
    }
}

@Composable
fun MapControlButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun CoordinateInfoBar(
    accuracy: String,
    x: String,
    y: String,
    elevation: String,
    scale: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CoordinateItem("Accuracy", accuracy)
            CoordinateItem("X", x)
            CoordinateItem("Y", y)
            CoordinateItem("Elev", elevation)
            CoordinateItem("Scale", scale)
        }
    }
}

@Composable
fun CoordinateItem(
    label: String,
    value: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun BasemapSelectorDialog(
    currentBasemap: BasemapStyle,
    onBasemapSelected: (BasemapStyle) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Basemap") },
        text = {
            Column {
                BasemapOption(
                    "Streets",
                    BasemapStyle.ArcGISStreets,
                    currentBasemap,
                    onBasemapSelected
                )
                BasemapOption(
                    "Imagery",
                    BasemapStyle.ArcGISImagery,
                    currentBasemap,
                    onBasemapSelected
                )
                BasemapOption(
                    "Topographic",
                    BasemapStyle.ArcGISTopographic,
                    currentBasemap,
                    onBasemapSelected
                )
                BasemapOption(
                    "Light Gray",
                    BasemapStyle.ArcGISLightGray,
                    currentBasemap,
                    onBasemapSelected
                )
                BasemapOption(
                    "Dark Gray",
                    BasemapStyle.ArcGISDarkGray,
                    currentBasemap,
                    onBasemapSelected
                )
                BasemapOption(
                    "Navigation",
                    BasemapStyle.ArcGISNavigation,
                    currentBasemap,
                    onBasemapSelected
                )
                BasemapOption(
                    "Oceans",
                    BasemapStyle.ArcGISOceans,
                    currentBasemap,
                    onBasemapSelected
                )
            }
        },
        confirmButton = {
            AppTextButton(
                text = "Close",
                onClick = onDismiss
            )
        }
    )
}

@Composable
fun BasemapOption(
    name: String,
    style: BasemapStyle,
    currentStyle: BasemapStyle,
    onSelected: (BasemapStyle) -> Unit
) {
    AppRadioButton(
        selected = currentStyle == style,
        onClick = { onSelected(style) },
        label = name
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MainMapScreenPreview() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isToolbarExpanded by remember { mutableStateOf(false) }
    var currentX by remember { mutableStateOf("--") }
    var currentY by remember { mutableStateOf("--") }
    var currentScale by remember { mutableStateOf("--") }
    var currentAccuracy by remember { mutableStateOf("--") }
    var currentElevation by remember { mutableStateOf("--") }

    ElectronicServicesTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = false,
            drawerContent = {
                ESNavigationDrawerContent(
                    onCollectESClick = {},
                    onESJobCardEntryClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Job Card Entry")
                        }
                    },
                    onProjectSettingsClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Project Settings")
                        }
                    },
                    onManageESEditsClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Manage ES Edits")
                        }
                    },
                    drawerState = drawerState,
                    scope = rememberCoroutineScope()
                )
            }
        ) {
            AppScaffold(
                snackbarHost = {
                    AppSnackbarHost(
                        hostState = snackbarHostState,
                        snackbarType = SnackbarType.INFO
                    )
                },
                topBar = {
                    AppTopBar(
                        title = "Map Screen",
                        navigationIcon = {
                            AppIconButton(
                                icon = Icons.Default.Menu,
                                contentDescription = "Menu",
                                onClick = { }
                            )
                        },
                        onActionClick = { }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                ) {
                    // Placeholder for map
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ArcGIS Map View",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Floating Action Button
                    FloatingActionButton(
                        onClick = { isToolbarExpanded = !isToolbarExpanded },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.End + WindowInsetsSides.Bottom))
                            .padding(end = Spacing.normal, bottom = Spacing.massive)
                    ) {
                        Icon(
                            imageVector = if (isToolbarExpanded) Icons.Default.Close else Icons.Default.Menu,
                            contentDescription = "Map Controls"
                        )
                    }

                    // Coordinate Info Bar
                    CoordinateInfoBar(
                        accuracy = currentAccuracy,
                        x = currentX,
                        y = currentY,
                        elevation = currentElevation,
                        scale = currentScale,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                    )
                }
            }
        }
    }
}
