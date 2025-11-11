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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arcgismaps.geometry.Point
import com.arcgismaps.toolkit.geoviewcompose.MapView
import com.enbridge.gdsgpscollection.designsystem.components.AppIconButton
import com.enbridge.gdsgpscollection.designsystem.components.AppScaffold
import com.enbridge.gdsgpscollection.designsystem.components.AppSnackbarHost
import com.enbridge.gdsgpscollection.designsystem.components.AppTopBar
import com.enbridge.gdsgpscollection.designsystem.components.ESNavigationDrawerContent
import com.enbridge.gdsgpscollection.designsystem.components.SnackbarType
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme
import com.enbridge.gdsgpscollection.ui.map.components.CollectESBottomSheet
import com.enbridge.gdsgpscollection.ui.map.components.CoordinateInfoBar
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

    // Determine feature visibility
    val showJobCardEntry = appVariant == "electronic"

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
                        screenState.showManageESSheet()
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
                        onViewpointChangedForCenterAndScale = { viewpoint ->
                            // Update coordinate state
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
                                .zIndex(10f)
                        )
                    }

                    // Floating map control toolbar
                    MapControlToolbar(
                        isTablet = isTablet,
                        isExpanded = screenState.interactionState.isToolbarExpanded,
                        isFullscreen = screenState.interactionState.isFullscreen,
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
                                snackbarHostState.showSnackbar("Location feature not yet implemented")
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
                        snackbarHostState.showSnackbar("Data posted successfully")
                    }
                },
                getCurrentMapExtent = {
                    screenState.coordinateState.currentViewpoint?.toEnvelope()
                },
                onGeodatabasesDownloaded = { geodatabaseInfos ->
                    Logger.i(
                        "MainMapScreen",
                        "onGeodatabasesDownloaded callback invoked with ${geodatabaseInfos.size} geodatabase(s)"
                    )

                    // Load layers from all downloaded geodatabases
                    geodatabaseInfos.forEach { info ->
                        Logger.d(
                            "MainMapScreen",
                            "Processing ${info.serviceName} geodatabase: ${info.fileName}"
                        )
                        Logger.d("MainMapScreen", "Geodatabase path: ${info.geodatabase.path}")
                        Logger.d(
                            "MainMapScreen",
                            "Geodatabase loadStatus: ${info.geodatabase.loadStatus.value}"
                        )

                        // Only load layers for geodatabases that should be displayed on map
                        if (info.displayOnMap) {
                            Logger.d(
                                "MainMapScreen",
                                "Loading layers from ${info.serviceName} to map"
                            )
                            mainMapViewModel.loadGeodatabaseLayers(info.geodatabase)
                        } else {
                            Logger.d(
                                "MainMapScreen",
                                "Skipping map display for ${info.serviceName} (displayOnMap=false)"
                            )
                        }
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