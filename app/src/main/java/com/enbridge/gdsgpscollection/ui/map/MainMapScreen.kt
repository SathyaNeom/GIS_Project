package com.enbridge.gdsgpscollection.ui.map

/**
 * @author Sathya Narayanan
 * This package contains the feature map functionality for GPS Device Project.
 */

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arcgismaps.geometry.Point
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.toolkit.geoviewcompose.MapView
import com.arcgismaps.toolkit.geoviewcompose.MapViewProxy
import com.arcgismaps.toolkit.geoviewcompose.rememberLocationDisplay
import com.enbridge.gdsgpscollection.designsystem.components.AppIconButton
import com.arcgismaps.location.Location
import com.enbridge.gdsgpscollection.designsystem.components.AppScaffold
import com.enbridge.gdsgpscollection.designsystem.components.AppSnackbarHost
import com.enbridge.gdsgpscollection.designsystem.components.AppTopBar
import com.enbridge.gdsgpscollection.designsystem.components.ESNavigationDrawerContent
import com.enbridge.gdsgpscollection.designsystem.components.SnackbarType
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme
import com.enbridge.gdsgpscollection.ui.debug.DebugSettingsScreen
import com.enbridge.gdsgpscollection.ui.map.components.CollectESBottomSheet
import com.enbridge.gdsgpscollection.ui.map.components.CoordinateInfoBar
import com.enbridge.gdsgpscollection.ui.map.components.LayerRecreationProgress
import com.enbridge.gdsgpscollection.ui.map.components.LocationPermissionHandler
import com.enbridge.gdsgpscollection.ui.map.components.MainMapDialogs
import com.enbridge.gdsgpscollection.ui.map.components.ManageESBottomSheet
import com.enbridge.gdsgpscollection.ui.map.components.MapControlToolbar
import com.enbridge.gdsgpscollection.ui.map.components.MapModeIndicators
import com.enbridge.gdsgpscollection.ui.map.components.OfflineBanner
import com.enbridge.gdsgpscollection.ui.map.components.ProjectSettingsBottomSheet
import com.enbridge.gdsgpscollection.ui.map.components.TableOfContentsBottomSheet
import com.enbridge.gdsgpscollection.util.Logger
import com.enbridge.gdsgpscollection.util.extensions.toEnvelope
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * MainMapScreen displays the primary map interface for GPS Device Project.
 *
 * This screen provides comprehensive GIS functionality including:
 * - Interactive ArcGIS map with multiple basemap options
 * - Feature identification and measurement tools
 * - Map navigation controls (zoom, pan, fullscreen)
 * - Bottom sheets for collecting and managing electronic service data
 * - Real-time coordinate and scale information display
 * - Integration with job card entry functionality
 * - Offline mode indication with network monitoring
 *
 * ## Architecture
 * The screen follows a modular architecture with:
 * - **State Management:** Centralized via `MainMapScreenState`
 * - **Event System:** Unidirectional event flow for user actions
 * - **Component Extraction:** Toolbar, dialogs, and indicators are separate components
 * - **SOLID Principles:** Clear separation of concerns and responsibilities
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
    // UI state management
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Screen state holder - consolidates all UI state
    val screenState = rememberMainMapScreenState()

    // ViewModels
    val collectESViewModel: CollectESViewModel = hiltViewModel()
    val projectSettingsViewModel: ProjectSettingsViewModel = hiltViewModel()
    val mainMapViewModel: MainMapViewModel = hiltViewModel()
    val manageESViewModel: ManageESViewModel = hiltViewModel()

    // Location Feature Flags accessed from MainMapViewModel
    val locationFeatureFlags = mainMapViewModel.locationFeatureFlags

    // Debug screen state
    var showDebugSettings by remember { mutableStateOf(false) }

    // Device configuration
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    // Collect UI state from ViewModels
    val collectESUiState by collectESViewModel.uiState.collectAsStateWithLifecycle()
    val projectSettingsUiState by projectSettingsViewModel.uiState.collectAsStateWithLifecycle()
    val map by mainMapViewModel.map.collectAsStateWithLifecycle()
    val layerInfoList by mainMapViewModel.layerInfoList.collectAsStateWithLifecycle()
    val osmVisible by mainMapViewModel.osmVisible.collectAsStateWithLifecycle()
    val showFirstTimeGuidanceDialog by mainMapViewModel.showFirstTimeGuidance.collectAsStateWithLifecycle()
    val geodatabaseLoadError by mainMapViewModel.geodatabaseLoadError.collectAsStateWithLifecycle()
    val isOffline by mainMapViewModel.isOffline.collectAsStateWithLifecycle()
    val currentAutoPanMode by mainMapViewModel.currentAutoPanMode.collectAsStateWithLifecycle()
    val targetViewpoint by mainMapViewModel.targetViewpoint.collectAsStateWithLifecycle()
    val isRecreatingLayers by mainMapViewModel.isRecreatingLayers.collectAsStateWithLifecycle()
    val isLoadingLayers by mainMapViewModel.isLoadingLayers.collectAsStateWithLifecycle()

    // Determine feature visibility
    val showJobCardEntry = appVariant == "electronic"

    // Location display setup
    val customDataSource = mainMapViewModel.createLocationDataSource()
    val locationDisplay = rememberLocationDisplay {
        customDataSource?.let { dataSource = it }
    }

    // MapViewProxy for programmatic map control (e.g., setting viewpoint)
    val mapViewProxy = remember { com.arcgismaps.toolkit.geoviewcompose.MapViewProxy() }

    // Location permission state
    var hasLocationPermission by remember { mutableStateOf(false) }

    // Get resources for animation duration and scale
    val context = LocalContext.current
    val animationDurationMs = context.resources.getInteger(
        com.enbridge.gdsgpscollection.R.integer.animation_duration_location_pan_ms
    )
    val locationZoomScale = context.resources.getInteger(
        com.enbridge.gdsgpscollection.R.integer.map_scale_location_zoom
    ).toDouble()

    // Handle location permissions
    LocationPermissionHandler(
        onPermissionResult = { granted ->
            hasLocationPermission = granted
            if (granted) {
                mainMapViewModel.enableLocationDisplay()

                scope.launch {
                    locationDisplay.dataSource.start().onSuccess {
                        Logger.i("MainMapScreen", "Location data source started successfully")
                        locationDisplay.setAutoPanMode(
                            com.arcgismaps.location.LocationDisplayAutoPanMode.Off
                        )
                        mainMapViewModel.setAutoPanMode(
                            com.arcgismaps.location.LocationDisplayAutoPanMode.Off
                        )
                    }.onFailure { error ->
                        Logger.e("MainMapScreen", "Failed to start location data source", error)
                    }
                }
            } else {
                mainMapViewModel.disableLocationDisplay()
                Logger.w("MainMapScreen", "Location permission denied")
            }
        },
        shouldRequest = true
    )

    // Handle back press - show logout dialog when drawer is closed
    BackHandler(enabled = true) {
        when {
            drawerState.isOpen -> {
                scope.launch { drawerState.close() }
            }

            else -> {
                screenState.showLogoutDialog()
            }
        }
    }

    // Handle Project Settings save success
    LaunchedEffect(projectSettingsUiState.saveSuccess) {
        if (projectSettingsUiState.saveSuccess) {
            snackbarHostState.showSnackbar("Project settings saved successfully")
            screenState.dismissAllBottomSheets()
            projectSettingsViewModel.resetSaveSuccess()
        }
    }

    // Handle target viewpoint changes for distance-based zoom with smooth animation
    LaunchedEffect(targetViewpoint) {
        targetViewpoint?.let { viewpoint ->
            Logger.d(
                "MainMapScreen",
                "Animating to target viewpoint with duration: ${animationDurationMs}ms"
            )
            // Animate the map to the new viewpoint with smooth transition
            // Duration must be provided in Duration type (using .milliseconds extension)
            mapViewProxy.setViewpointAnimated(
                viewpoint = viewpoint,
                duration = animationDurationMs.milliseconds
            ).onSuccess {
                Logger.d("MainMapScreen", "Viewpoint animation completed successfully")
            }.onFailure { error ->
                Logger.e("MainMapScreen", "Viewpoint animation failed", error)
            }
        }
    }

    // Observe location updates and update current location in ViewModel
    val currentLocationValue = locationDisplay.location.collectAsStateWithLifecycle().value
    LaunchedEffect(currentLocationValue) {
        // Extract Point from Location and update ViewModel
        val locationPoint = currentLocationValue?.position
        mainMapViewModel.updateCurrentLocation(locationPoint)
    }

    GdsGpsCollectionTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = false,
            drawerContent = {
                ESNavigationDrawerContent(
                    onCollectESClick = {
                        screenState.showCollectESSheet()
                    },
                    onESJobCardEntryClick = onNavigateToJobCardEntry,
                    onProjectSettingsClick = {
                        screenState.showProjectSettingsSheet()
                    },
                    onManageESEditsClick = {
                        screenState.showManageESSheet(screenState.coordinateState.currentViewpoint)
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
                    // Note: SnackbarType is set to SUCCESS as default
                    // Individual snackbar calls can use AppSnackbarHost with specific types
                    // For download success/error, ManageESBottomSheet uses its own AppSnackbarHost
                    AppSnackbarHost(
                        hostState = snackbarHostState,
                        snackbarType = SnackbarType.SUCCESS // Default to success for most operations
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
                        onActionClick = onSearchClick,
                        actions = {
                            // Debug icon (only visible in debug builds)
                            if (locationFeatureFlags.showDebugMenu) {
                                AppIconButton(
                                    icon = Icons.Default.Build,
                                    contentDescription = "Debug Settings",
                                    onClick = { showDebugSettings = true }
                                )
                            }
                        }
                    )
                }
            ) { paddingValues ->
                // Main content container
                // Components are layered in a Box with specific z-ordering for proper visibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                ) {
                    // ArcGIS MapView - Positioned first as the base layer
                    MapView(
                        arcGISMap = map,
                        modifier = Modifier.fillMaxSize(),
                        locationDisplay = locationDisplay,
                        mapViewProxy = mapViewProxy,
                        onViewpointChangedForCenterAndScale = { viewpoint ->
                            viewpoint?.let { vp ->
                                val point = vp.targetGeometry as? Point
                                val x = point?.let { String.format("%.4f", it.x) } ?: "--"
                                val y = point?.let { String.format("%.4f", it.y) } ?: "--"
                                val scale =
                                    vp.targetScale?.let { String.format("%.0f", it) } ?: "--"
                                val scaleValue = vp.targetScale ?: 1e8

                                screenState.updateCoordinates(
                                    x = x,
                                    y = y,
                                    scale = scale,
                                    scaleValue = scaleValue,
                                    viewpoint = vp
                                )
                            }
                        },
                        onSingleTapConfirmed = { event ->
                            val mapPoint = event.mapPoint
                            if (mapPoint != null) {
                                val x = String.format("%.4f", mapPoint.x)
                                val y = String.format("%.4f", mapPoint.y)

                                screenState.updateCoordinates(
                                    x = x,
                                    y = y,
                                    scale = screenState.coordinateState.scale,
                                    scaleValue = screenState.coordinateState.scaleValue
                                )

                                // Handle identify mode
                                if (screenState.interactionState.identifyMode) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Feature identified at X: $x, Y: $y"
                                        )
                                        screenState.setIdentifyMode(false)
                                        screenState.setToolbarExpanded(false)
                                    }
                                }
                            }
                        }
                    )

                    // Offline banner - Positioned after MapView to draw on top
                    // Uses zIndex to ensure visibility above the map when offline
                    if (isOffline) {
                        OfflineBanner(
                            isOffline = isOffline,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .zIndex(30f) // Above all other UI elements including progress indicator
                        )
                    }

                    // Floating map control toolbar
                    MapControlToolbar(
                        isTablet = isTablet,
                        isExpanded = screenState.interactionState.isToolbarExpanded,
                        isFullscreen = screenState.interactionState.isFullscreen,
                        isLocationFollowing = currentAutoPanMode is com.arcgismaps.location.LocationDisplayAutoPanMode.CompassNavigation ||
                                currentAutoPanMode is com.arcgismaps.location.LocationDisplayAutoPanMode.Navigation,
                        onToggleExpanded = {
                            screenState.setToolbarExpanded(!screenState.interactionState.isToolbarExpanded)
                        },
                        onZoomIn = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Zoom In")
                            }
                        },
                        onZoomOut = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Zoom Out")
                            }
                        },
                        onToggleFullscreen = {
                            val newFullscreenState = !screenState.interactionState.isFullscreen
                            screenState.setFullscreen(newFullscreenState)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (newFullscreenState) "Fullscreen mode enabled" else "Fullscreen mode disabled"
                                )
                            }
                            screenState.setToolbarExpanded(false)
                        },
                        onIdentify = {
                            screenState.setIdentifyMode(true)
                            scope.launch {
                                snackbarHostState.showSnackbar("Tap on feature")
                            }
                        },
                        onShowLayers = {
                            screenState.showTableOfContentsSheet()
                            screenState.setToolbarExpanded(false)
                        },
                        onClear = {
                            screenState.showClearDialog()
                            screenState.setToolbarExpanded(false)
                        },
                        onMyLocation = {
                            scope.launch {
                                // Check if location permission is granted
                                if (!hasLocationPermission) {
                                    snackbarHostState.showSnackbar(
                                        context.getString(com.enbridge.gdsgpscollection.R.string.msg_location_permission_required)
                                    )
                                    return@launch
                                }

                                // Get current location from location display
                                val currentLocation = locationDisplay.location.value

                                if (currentLocation == null) {
                                    snackbarHostState.showSnackbar(
                                        context.getString(com.enbridge.gdsgpscollection.R.string.msg_location_acquiring)
                                    )
                                    // Display quick info message about waiting for fix
                                    screenState.updateCoordinates(
                                        x = "--",
                                        y = "--",
                                        scale = screenState.coordinateState.scale,
                                        scaleValue = screenState.coordinateState.scaleValue
                                    )
                                    return@launch
                                }

                                // Toggle location following mode
                                val newMode = mainMapViewModel.toggleLocationFollowing()

                                // Update the location display auto-pan mode
                                locationDisplay.setAutoPanMode(newMode)

                                // Animate to current location with ease curve
                                val viewpoint = Viewpoint(
                                    center = currentLocation.position,
                                    scale = locationZoomScale
                                )
                                mapViewProxy.setViewpointAnimated(
                                    viewpoint = viewpoint,
                                    duration = animationDurationMs.milliseconds
                                )

                                // Show appropriate message based on new mode
                                val message = when (newMode) {
                                    is com.arcgismaps.location.LocationDisplayAutoPanMode.CompassNavigation,
                                    is com.arcgismaps.location.LocationDisplayAutoPanMode.Navigation -> {
                                        context.getString(com.enbridge.gdsgpscollection.R.string.msg_location_following_enabled)
                                    }

                                    else -> {
                                        context.getString(com.enbridge.gdsgpscollection.R.string.msg_location_following_disabled)
                                    }
                                }

                                snackbarHostState.showSnackbar(message)
                            }
                            screenState.setToolbarExpanded(false)
                        },
                        onToggleMeasure = {
                            val newMeasurementState = !screenState.interactionState.measurementMode
                            screenState.setMeasurementMode(newMeasurementState)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (newMeasurementState) "Measurement mode enabled" else "Measurement mode disabled"
                                )
                            }
                            screenState.setToolbarExpanded(false)
                        },
                        modifier = Modifier.align(Alignment.TopEnd)
                    )

                    // Coordinate Info Bar (hide in fullscreen mode)
                    if (!screenState.interactionState.isFullscreen) {
                        CoordinateInfoBar(
                            accuracy = screenState.coordinateState.accuracy,
                            x = screenState.coordinateState.x,
                            y = screenState.coordinateState.y,
                            elevation = screenState.coordinateState.elevation,
                            scale = screenState.coordinateState.scale,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .windowInsetsPadding(
                                    WindowInsets.systemBars.only(
                                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                                    )
                                )
                        )
                    }

                    // Map mode indicators
                    MapModeIndicators(
                        identifyMode = screenState.interactionState.identifyMode,
                        measurementMode = screenState.interactionState.measurementMode,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )

                    // Layer recreation progress
                    if (isRecreatingLayers) {
                        LayerRecreationProgress(
                            isRecreating = isRecreatingLayers,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .zIndex(20f) // Above all other UI elements
                        )
                    }
                }
            }
        }

        // All dialogs consolidated
        MainMapDialogs(
            showLogoutDialog = screenState.dialogState.showLogoutDialog,
            showClearDialog = screenState.dialogState.showClearDialog,
            showBasemapDialog = screenState.dialogState.showBasemapDialog,
            showFirstTimeGuidance = showFirstTimeGuidanceDialog,
            geodatabaseLoadError = geodatabaseLoadError,
            currentBasemap = screenState.interactionState.selectedBasemapStyle,
            onLogout = onLogout,
            onClearData = {
                mainMapViewModel.deleteGeodatabase()
                scope.launch {
                    snackbarHostState.showSnackbar("Geodatabase cleared successfully")
                }
            },
            onDismissLogout = { screenState.dismissLogoutDialog() },
            onDismissClear = { screenState.dismissClearDialog() },
            onBasemapSelected = { basemap ->
                screenState.updateBasemapStyle(basemap)
                mainMapViewModel.updateBasemapStyle(basemap)
            },
            onDismissBasemap = { screenState.dismissBasemapDialog() },
            onDismissFirstTimeGuidance = { mainMapViewModel.dismissFirstTimeGuidance() },
            onDismissGeodatabaseError = { mainMapViewModel.dismissGeodatabaseLoadError() }
        )

        // CollectES Bottom Sheet
        if (screenState.bottomSheetState.showCollectES) {
            CollectESBottomSheet(
                onDismissRequest = {
                    screenState.dismissAllBottomSheets()
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
        if (screenState.bottomSheetState.showManageES) {
            ManageESBottomSheet(
                onDismissRequest = {
                    screenState.dismissAllBottomSheets()
                },
                onPostDataSnackbar = {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            context.getString(com.enbridge.gdsgpscollection.R.string.msg_data_downloaded_successfully)
                        )
                    }
                },
                initialViewpoint = screenState.bottomSheetState.manageESCapturedViewpoint,
                onRestoreViewpoint = { viewpoint ->
                    mainMapViewModel.restoreViewpoint(viewpoint)
                },
                snackbarHostState = snackbarHostState,
                onGeodatabasesDownloaded = { geodatabaseInfos ->
                    Logger.i(
                        "MainMapScreen",
                        "onGeodatabasesDownloaded callback invoked with ${geodatabaseInfos.size} geodatabase(s)"
                    )

                    // Filter geodatabases that should be displayed on map
                    val geodatabasesToDisplay = geodatabaseInfos.filter { info ->
                        Logger.d(
                            "MainMapScreen",
                            "Processing ${info.serviceName} geodatabase: ${info.fileName}"
                        )
                        Logger.d("MainMapScreen", "Geodatabase path: ${info.geodatabase.path}")
                        Logger.d(
                            "MainMapScreen",
                            "Geodatabase loadStatus: ${info.geodatabase.loadStatus.value}"
                        )

                        if (info.displayOnMap) {
                            Logger.d(
                                "MainMapScreen",
                                "Will load layers from ${info.serviceName} to map"
                            )
                            true
                        } else {
                            Logger.d(
                                "MainMapScreen",
                                "Skipping map display for ${info.serviceName} (displayOnMap=false)"
                            )
                            false
                        }
                    }

                    // Load all geodatabases at once (more efficient than one-by-one)
                    if (geodatabasesToDisplay.isNotEmpty()) {
                        Logger.i(
                            "MainMapScreen",
                            "Loading ${geodatabasesToDisplay.size} geodatabase(s) onto map"
                        )
                        mainMapViewModel.loadMultipleGeodatabases(
                            geodatabasesToDisplay.map { it.geodatabase }
                        )
                    } else {
                        Logger.w("MainMapScreen", "No geodatabases to display on map")
                    }

                    // Save timestamp after all geodatabases are processed
                    mainMapViewModel.saveGeodatabaseTimestamp()

                    Logger.i("MainMapScreen", "All geodatabases processed successfully")
                },
                onDistanceSelected = { distance ->
                    mainMapViewModel.updateMaxExtent(distance)
                }
            )
        }

        // Project Settings Bottom Sheet
        if (screenState.bottomSheetState.showProjectSettings) {
            ProjectSettingsBottomSheet(
                onDismissRequest = {
                    screenState.dismissAllBottomSheets()
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

        // Table of Contents Bottom Sheet
        if (screenState.bottomSheetState.showTableOfContents) {
            TableOfContentsBottomSheet(
                layers = layerInfoList,
                onDismissRequest = { screenState.dismissAllBottomSheets() },
                osmVisible = osmVisible,
                isLoadingLayers = isLoadingLayers,
                onToggleLayerVisibility = { id, visible ->
                    mainMapViewModel.toggleLayerVisibility(id, visible)
                },
                onToggleLayerExpanded = { id ->
                    mainMapViewModel.toggleLayerExpanded(id)
                },
                onToggleSelectAll = {
                    mainMapViewModel.toggleSelectAll()
                },
                onToggleOsmVisibility = { visible ->
                    mainMapViewModel.toggleOsmVisibility(visible)
                }
            )
        }

        // Debug settings screen
        if (showDebugSettings) {
            DebugSettingsScreen(
                onNavigateBack = { showDebugSettings = false },
                locationMode = if (locationFeatureFlags.useSimulatedLocation) {
                    context.getString(com.enbridge.gdsgpscollection.R.string.debug_location_simulated)
                } else {
                    context.getString(com.enbridge.gdsgpscollection.R.string.debug_location_real)
                },
                currentLocation = mainMapViewModel.currentLocation.collectAsStateWithLifecycle().value,
                isLocationAvailable = mainMapViewModel.isLocationAvailable.collectAsStateWithLifecycle().value
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MainMapScreenPreview() {
    MainMapScreen(
        onNavigateToJobCardEntry = {},
        onAddNewFeature = {},
        onSearchClick = {},
        onLogout = {}
    )
}