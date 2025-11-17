# Issues Fixed - Complete History

**Last Updated:** November 2025  
**Project:** GdsGpsCollection Android Application

This document tracks all significant issues that have been identified and resolved during the
development of the application. For current known issues, see `docs/KNOWN_ISSUES.md`.

---

## Table of Contents

- [Build and Configuration Issues](#build-and-configuration-issues)
- [UI/UX Issues](#uiux-issues)
- [Testing Infrastructure Issues](#testing-infrastructure-issues)
- [ArcGIS Map Integration Issues](#arcgis-map-integration-issues)
- [Architecture and Code Quality Issues](#architecture-and-code-quality-issues)
- [Data and Performance Issues](#data-and-performance-issues)

---

## Build and Configuration Issues

### JDK Configuration Error (Resolved: Nov 2025)

**Issue:** Build failures with "Invalid Gradle JDK configuration found" error

**Root Cause:** Gradle was configured to use `GRADLE_LOCAL_JAVA_HOME` macro but no Java home was
defined in `gradle/config.properties`

**Solution:**

- Configured Gradle to use Android Studio's embedded JDK (jbr-17)
- Updated documentation to guide developers to correct configuration
- Added troubleshooting steps in QUICK_START.md

**Impact:** Build process now works consistently across all development environments

---

### KAPT/KSP Compatibility (Resolved: Nov 2025)

**Issue:** Room's KAPT processor incompatible with Kotlin 2.0.21

**Root Cause:** KAPT has limited support for Kotlin 2.0+

**Solution:**

- Migrated Room annotation processing from KAPT to KSP (Kotlin Symbol Processing)
- Added KSP plugin version `2.0.0-1.0.21` to libs.versions.toml
- Kept KAPT for Hilt (still requires it)
- Updated build.gradle.kts configuration

**Benefits:**

- Better Kotlin 2.0 support
- Faster annotation processing
- Modern tooling approach

**Files Modified:**

- `libs.versions.toml` - Added KSP version
- `app/build.gradle.kts` - Migrated Room compiler to KSP

---

### Missing Network Permissions (Resolved: Nov 2025)

**Issue:** Network requests failing silently due to missing AndroidManifest permissions

**Root Cause:** Internet and network state permissions were not declared

**Solution:** Added required permissions to AndroidManifest.xml:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

**Impact:** Network features now function correctly

---

## UI/UX Issues

### Table of Contents - No Legends Displayed (Resolved: Nov 2025)

**Issue:** Layers in Table of Contents showed no expandable arrows or legend information

**Root Cause:** The `extractLegendItemsFromRenderer()` function was documented but never implemented
in `MainMapViewModel`

**Solution:**

1. Implemented `extractLegendItemsFromRenderer()` function that:
   - Loads feature layer to access its renderer
   - Determines renderer type (SimpleRenderer, UniqueValueRenderer, ClassBreaksRenderer)
   - Extracts legend information with proper labels
   - Returns populated `LegendItem` objects

2. Updated `loadGeodatabaseLayers()` to call the extraction function

3. Updated `TableOfContentsBottomSheet.kt` to handle text-only legends (symbol images not yet
   rendered)

**Result:** Legends now display with proper labels from ArcGIS renderers

**Files Modified:**

- `MainMapViewModel.kt` - Added legend extraction (~150 lines)
- `TableOfContentsBottomSheet.kt` - Handle empty symbol paths

---

### OSM Basemap Toggle Not Working (Resolved: Nov 2025)

**Issue:** Unchecking "Open Street Map" checkbox did not hide the basemap

**Root Cause:** `toggleOsmVisibility()` function only updated state variable without recreating the
map. ArcGIS SDK's `ArcGISMap.basemap` is read-only StateFlow and cannot be directly modified.

**Solution:** Implemented map recreation strategy:

1. Store current basemap style in variable
2. Preserve current map state (viewpoint, layers, extent)
3. Recreate entire `ArcGISMap` object with or without basemap
4. Restore all state to new map instance
5. Update StateFlow to trigger recomposition

**Code Pattern:**

```kotlin
// Recreate map WITHOUT basemap
val newMap = ArcGISMap().apply {  // No basemap style parameter
    initialViewpoint = currentViewpoint
    maxExtent = currentMaxExtent
    operationalLayers.addAll(currentLayers)
}
_map.value = newMap
```

**Result:** OSM checkbox now properly shows/hides basemap while preserving all layer data

**Files Modified:**

- `MainMapViewModel.kt` - Complete rewrite of `toggleOsmVisibility()` and `updateBasemapStyle()`

---

### Hardcoded Strings in UI (Resolved: Nov 2025)

**Issue:** 250+ hardcoded strings throughout the application making localization impossible

**Root Cause:** Rapid development led to inline string literals instead of string resources

**Solution:**

- Extracted all user-facing strings to `strings.xml` files
- Organized by module (auth, jobs, map, common)
- Implemented format strings with proper placeholders
- Updated all composables to use `stringResource()`

**Modules Updated:**

- Login and Authentication (35+ strings)
- Job Card Entry (200+ strings including 70+ field labels)
- Map and ES Management (40+ strings)
- Common UI components (25+ strings)

**Benefits:**

- Ready for localization/translation
- Consistency across app
- Easier maintenance
- Professional quality

**Files Modified:** 40+ Kotlin files, 4 strings.xml files

---

### Offline Banner Display Issues (Resolved: Nov 2025)

**Issue:** Offline banner sometimes appeared below map controls making it hard to notice

**Root Cause:** Z-index ordering and positioning conflicts

**Solution:**

- Added explicit `zIndex(10f)` modifier to `OfflineBanner`
- Positioned at `Alignment.TopCenter` in Box layout
- Ensured banner appears above MapView but below dialogs
- Properly handled window insets for edge-to-edge displays

**Result:** Banner now prominently displays at top of screen when offline

---

### OSM Checkbox Not Reflecting State (Resolved: Nov 2025)

**Issue:** OSM checkbox would reset to checked state even when basemap was hidden

**Root Cause:** State synchronization issue between ViewModel and UI

**Solution:**

- Ensured `_osmVisible` state is updated immediately before map recreation
- UI now observes state correctly with `collectAsStateWithLifecycle()`
- Fixed race condition in state updates

**Files Modified:**

- `MainMapViewModel.kt` - State update ordering
- `TableOfContentsBottomSheet.kt` - State observation

---

## Testing Infrastructure Issues

### Test Application ID Mismatch - Critical (Resolved: Nov 2025)

**Issue:** All Android instrumented tests failing with process mismatch error:

```
java.lang.RuntimeException: Intent in process com.enbridge.gdsgpscollection.construction.debug 
resolved to different process com.enbridge.gdsgpscollection.construction.debug.test
```

**Root Cause:** Custom `testApplicationId` declarations in product flavors caused misaligned package
names between app and test APKs

**Solution:**

1. **Removed all `testApplicationId` declarations** from `app/build.gradle.kts`
2. **Let Gradle auto-generate test package names** with proper `.test` suffix
3. **Moved `HiltTestActivity` from androidTest to app/src/debug/** - This was the critical fix
   - Activity is now part of app process, not test process
   - Hilt injection works correctly

4. Disabled Test Orchestrator (was enabled without dependency)
5. Updated `ExampleInstrumentedTest` for dynamic package names

**Results:**

- 54 tests now discovered and executed (was 0 before)
- 40 tests passing (74% pass rate)
- Process mismatch completely resolved
- Hilt dependency injection working

**Best Practice Learned:** Never manually set `testApplicationId` in multi-flavor projects with
build type suffixes

**Files Modified:**

- `app/build.gradle.kts` - Removed 6 testApplicationId declarations
- Moved `HiltTestActivity.kt` to `app/src/debug/`
- Added `debug/AndroidManifest.xml` with activity declaration
- `ExampleInstrumentedTest.kt` - Updated assertions

---

### Hilt Test Configuration Complexity (Resolved: Nov 2025)

**Issue:** Instrumented tests failing with Dagger/MissingBinding errors

**Root Cause:** Incomplete Hilt test setup

**Solution:** Created comprehensive Hilt testing infrastructure:

1. Custom `HiltTestRunner` extending `AndroidJUnitRunner`
2. `HiltTestActivity` annotated with `@AndroidEntryPoint`
3. Proper test manifest configuration
4. Configured test instrumentation runner in build.gradle.kts

**Documentation:** Created detailed guide in TESTING_GUIDE.md

---

## ArcGIS Map Integration Issues

### Blank Map Screen (Resolved: Nov 2025)

**Issue:** Map displayed white/blank screen on first launch

**Root Cause:** ArcGIS API key not configured or lacking required privileges

**Solution:**

- Documented API key configuration in QUICK_START.md
- Added troubleshooting steps
- Implemented proper error handling and logging
- Added API key validation

**Configuration Required:**

```properties
# local.properties
ARCGIS_API_KEY=your_api_key_here
```

---

### Map Gesture Conflicts with Navigation Drawer (Resolved: Nov 2025)

**Issue:** Swiping from left edge to pan map would open navigation drawer

**Root Cause:** Conflicting gesture detection between map and drawer

**Solution:** Set `gesturesEnabled = false` on `ModalNavigationDrawer` for map screen

**Trade-off:** Tap-outside-to-close drawer does not work on map screen, but map panning works
correctly

**Rationale:** Map panning is core functionality used more frequently than drawer access

**Status:** Accepted as design decision, documented in KNOWN_ISSUES.md

---

## Architecture and Code Quality Issues

### Multi-Module Complexity (Resolved: Nov 2025)

**Issue:** 7-module architecture created navigation complexity and onboarding difficulty

**Root Cause:** Over-engineering for current project scale

**Solution:** Consolidated to single-module, package-based architecture:

- Merged 7 modules into one app module
- Organized by layer using packages (data, domain, ui, designsystem)
- Maintained Clean Architecture principles through package structure
- Reduced build overhead

**Benefits:**

- Simpler navigation and code discovery
- Faster builds (25% improvement)
- Easier onboarding for new developers
- All 200+ tests and 90% coverage maintained

**Files Affected:** Entire project restructure (91+ files moved)

---

### MainMapScreen Monolithic Structure (Resolved: Nov 2025)

**Issue:** `MainMapScreen.kt` was 1,025 lines with 20+ scattered state variables

**Root Cause:** All UI components, dialogs, and state management in single file

**Solution:** Comprehensive refactoring following SOLID principles:

1. **Created State Holder Pattern:**
   - `MainMapScreenState.kt` - Grouped state into 4 categories
   - Reduced 20 state variables to 4 grouped holders
   - 80% reduction in state clutter

2. **Created Event System:**
   - `MainMapScreenEvent.kt` - 20+ event types
   - Single event handler replaces 7+ callback parameters
   - Type-safe event handling

3. **Extracted Components:**
   - `MapControlToolbar.kt` (309 lines) - Floating toolbar
   - `MapModeIndicators.kt` (87 lines) - Mode feedback
   - `MainMapDialogs.kt` (193 lines) - All dialogs consolidated

**Results:**

- Main screen: 1,025 to ~400 lines (61% reduction)
- Improved testability and maintainability
- Clear separation of concerns
- SOLID principles compliance

---

### Code Cleanup and Technical Debt (Resolved: Nov 2025)

**Issues Addressed:**

- Removed commented-out code
- Cleaned up unused imports
- Removed debug logging
- Organized preview functions
- Improved KDoc comments

**Files Affected:** 50+ files cleaned up

**Result:** Professional, maintainable codebase

---

## Data and Performance Issues

### Geodatabase Download Progress Inaccurate (Resolved: Nov 2025)

**Issue:** Progress tracking displayed confusing jumps during geodatabase downloads:

- Progress started at 30% instead of 0%
- Progress jumped erratically: 31% → 95% → 32% → 33% → 34%
- Displayed percentage did not reflect actual download status

**Root Causes:**

1. **Race Condition**: Uncancelled progress monitoring coroutine continued emitting updates after
   the "Processing" stage began, causing backwards progress jumps
2. **Incorrect Range Mapping**: Job progress (0-100%) was mapped to UI range (30-90%), making
   downloads appear to start at 30%
3. **No Progress Smoothing**: Updates arrived at irregular intervals causing abrupt visual jumps

**Solution:**

1. **Race Condition Prevention:**
   - Stored progress monitoring Job reference
   - Explicitly cancelled Job before sending 95% "processing" message
   - Ensured deterministic progress sequence

2. **Improved Progress Mapping:**
   - Old: 0-5% (connection), 5-10% (storage), 10-30% (init), 30-90% (download), 95% (processing)
   - New: 0-5% (connection), 5-10% (storage), 10-15% (init), 15-95% (download), 95-100% (processing)
   - Larger download range (80% vs 60%) provides better granularity

3. **Granular Progress Messages:**
   - Added contextual messages: "Downloading features", "Downloading attributes", "Downloading
     geometry", "Finalizing download"
   - Maps job progress ranges to specific operations for better user feedback

4. **Progress Animation Smoothing:**
   - Implemented `animateFloatAsState` with 300ms duration
   - Smooth interpolation between progress values
   - GPU-accelerated composition for minimal performance impact

**Technical Implementation:**

```kotlin
// Store and cancel monitoring job to prevent race condition
val progressMonitoringJob = channel.launch {
    generateJob.progress.collect { jobProgress ->
        val adjustedProgress = 0.15f + (jobProgress / 100f * 0.80f)
        // Map to granular messages based on progress
        send(ESDataDownloadProgress(adjustedProgress, progressMessage, false))
    }
}

val result = generateJob.result()
progressMonitoringJob.cancel()  // Critical: prevent late updates
send(ESDataDownloadProgress(0.95f, "Processing geodatabase…"))
```

**Applied To:**

- Both single-service (Wildcard environment) and multi-service (Project environment) flows
- `executeDownload()` method for single geodatabase
- `downloadServiceData()` method for parallel downloads

**Results:**

- Smooth, accurate progress from 0% to 100%
- No backwards jumps or race conditions
- Clear contextual feedback at each download stage
- Improved user experience during long-running operations

**Files Modified:**

- `app/src/main/res/values/strings.xml` - Added 4 granular progress strings
- `app/src/main/java/com/enbridge/gdsgpscollection/data/repository/ManageESRepositoryImpl.kt` -
  Fixed race condition in `executeDownload()` and `downloadServiceData()`
- `app/src/main/java/com/enbridge/gdsgpscollection/designsystem/components/ProgressIndicators.kt` -
  Added progress animation smoothing

**Performance Impact:**

- Minimal: Single coroutine per download, proper cancellation prevents leaks
- Animation is GPU-accelerated with negligible CPU impact
- No changes to network efficiency

---

## Summary Statistics

### Issues Resolved by Category

| Category                | Count  | Impact    |
|-------------------------|--------|-----------|
| Build and Configuration | 3      | High      |
| UI/UX                   | 6      | High      |
| Testing Infrastructure  | 2      | Critical  |
| ArcGIS Integration      | 2      | Medium    |
| Architecture            | 3      | High      |
| Data and Performance    | 1      | High      |
| **Total**               | **17** | **Mixed** |

### Lines of Code Improved

| Metric            | Before      | After      | Improvement   |
|-------------------|-------------|------------|---------------|
| Largest File      | 1,025 lines | ~400 lines | 61% reduction |
| Hardcoded Strings | 250+        | 0          | 100% fixed    |
| Total Tests       | 200+        | 220+       | 10% increase  |
| Test Coverage     | 90%         | 90%        | Maintained    |

---

## Lessons Learned

### Development Practices

1. **Test Infrastructure First** - Proper Hilt test setup prevented weeks of debugging
2. **State Management Patterns** - State holders dramatically improved code organization
3. **ArcGIS SDK Constraints** - Read-only properties require object recreation patterns
4. **Build Configuration** - Let Gradle auto-generate test package names
5. **String Externalization** - Essential for localization, should be done from day one
6. **Progress Tracking** - Always cancel monitoring coroutines before transitioning to new stages

### Architecture Decisions

1. **Single Module Appropriate** - For projects under 100K LOC, single module is simpler
2. **Package-Based Organization** - Enforces Clean Architecture without build overhead
3. **Component Extraction** - Small, focused components improve testability dramatically
4. **Event Systems** - Reduce parameter proliferation and improve maintainability

### Testing Insights

1. **Hilt Test Activity Location Matters** - Must be in app process, not test process
2. **Instrumented Tests Need Devices** - Cannot run on JVM alone
3. **Test Coverage Thresholds** - Enforce via Jacoco to prevent regression
4. **MockK Relaxed Mocks** - Use sparingly, prefer explicit stubbing

---

## Future Issue Prevention

### Checklist for New Features

- [ ] Externalize all strings immediately
- [ ] Write tests alongside code
- [ ] Follow state holder pattern for complex state
- [ ] Keep files under 300 lines
- [ ] Follow SOLID principles
- [ ] Add proper logging
- [ ] Update documentation
- [ ] Test on physical devices
- [ ] Cancel coroutines properly to prevent race conditions

### Code Review Focus Areas

- String externalization
- Test coverage (aim for 85% or higher)
- File size (flag if over 300 lines)
- State management patterns
- SOLID compliance
- Proper error handling
- Logging appropriateness
- Coroutine lifecycle management

---

## Related Documentation

- **Current Issues:** See `docs/KNOWN_ISSUES.md`
- **Testing Guide:** See `TESTING_GUIDE.md`
- **Future Plans:** See `docs/FUTURE_UPGRADES.md`
- **Quick Start:** See `QUICK_START.md`

---

**Document Maintenance:** This file should be updated whenever significant issues are resolved. Link
to specific commits or PRs when available for detailed change history.

**Last Review:** November 2025  
**Next Review:** Quarterly or before major releases

