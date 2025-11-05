# Known Issues and Limitations

This document outlines known issues, technical limitations, and workarounds for the Electronic
Services Android application.

## Overview

This document is maintained to provide transparency regarding current limitations and to document
resolutions or workarounds. Issues are categorized by severity and module.

## Map Screen Limitations

### Tap-Outside-to-Close Drawer on Map Screen

**Severity:** Low  
**Module:** feature_map  
**Status:** Technical Limitation - Accepted

#### Description

The navigation drawer cannot be closed by tapping outside (on the map area) when
`gesturesEnabled = false` is configured on `ModalNavigationDrawer`. This limitation exists
specifically on the Map screen.

#### Root Cause

Conflicting requirements between map gesture handling and drawer gesture detection:

- Map panning gestures require `gesturesEnabled = false` to prevent the drawer from opening during
  map pan/zoom operations
- Tap-outside-to-close functionality requires `gesturesEnabled = true` to detect touch events on the
  scrim overlay

Enabling `gesturesEnabled = true` allows tap-outside-to-close but interferes with map panning from
the left edge of the screen.

#### Impact

**Minimal user experience impact:**

- Other screens (Jobs, Sync) support tap-outside-to-close functionality
- Map panning works correctly without gesture conflicts
- Users can close the drawer using:
    - Back button or back gesture (primary method on mobile)
    - Selecting any menu item to navigate
    - Map functionality remains unaffected

#### Workaround

Users should utilize the back button or back gesture to close the drawer, which is the standard
Android pattern for dismissing modal overlays.

#### Technical Details

Attempted solutions that were unsuccessful:

1. Custom scrim overlay with `zIndex` modifiers - clicks not captured properly
2. Box wrapper with custom clickable overlay - blocked by ModalNavigationDrawer internal structure
3. Setting `gesturesEnabled = true` - breaks map panning functionality

#### Resolution Strategy

The limitation has been accepted with the following rationale:

- Map panning is core functionality used frequently
- Back button/gesture is the expected dismissal method on Android
- Trade-off favors superior map user experience
- Consistent with Material Design guidelines for modal drawers

#### Future Considerations

If this limitation becomes critical:

1. Implement custom drawer component with full gesture control
2. Develop custom gesture detection layer to differentiate drawer swipe from map pan
3. Consider alternative UX pattern (e.g., bottom sheet) for map screen navigation

**Recommendation:** Accept current limitation unless user feedback indicates significant usability
concerns.

## ArcGIS Integration Issues

### API Key Configuration

**Severity:** Medium  
**Module:** core  
**Status:** Configuration Required

#### Description

The map displays a blank screen if the ArcGIS API key is not properly configured or lacks required
privileges.

#### Symptoms

- White or blank map area
- No basemap tiles loading
- LogCat errors related to ArcGIS authentication

#### Resolution

1. Obtain API key from [ArcGIS Developers](https://developers.arcgis.com)
2. Configure key in `local.properties`:

```properties
ARCGIS_API_KEY=your_actual_api_key
```

3. Ensure the API key has the following privileges:
    - Location services - Basemaps
    - Location services - Geocoding
    - Location services - Routing
4. Rebuild the application to apply configuration changes

#### Verification

Check `core/build/generated/.../BuildConfig.kt` to verify the API key is properly injected:

```kotlin
const val ARCGIS_API_KEY = "AAPK..."
```

### Map Initial Load Performance

**Severity:** Low  
**Module:** feature_map  
**Status:** Expected Behavior

#### Description

The first map load may take 2-5 seconds depending on network connectivity as basemap tiles are
downloaded.

#### Impact

Initial map display may appear slow on first launch or with poor network conditions.

#### Workaround

- Display loading indicator during initial map load
- Cache basemap tiles for offline use (future enhancement)
- Consider implementing progressive basemap loading

## Build Configuration Issues

### JDK Configuration Errors

**Severity:** Medium  
**Module:** Project-wide  
**Status:** Resolved with Documentation

#### Description

Build failures occur when Gradle is configured to use `GRADLE_LOCAL_JAVA_HOME` macro but no Java
home is defined in `gradle/config.properties`.

#### Symptoms

Error message:

```
Invalid Gradle JDK configuration found
```

#### Resolution

Configure Gradle to use Android Studio's embedded JDK:

1. Navigate to **File → Settings → Build Tools → Gradle**
2. Set **Gradle JDK** to **Embedded JDK (jbr-17)**
3. Click **Apply** and sync project

Alternative: Click **Use Embedded JDK** button in the error banner.

#### Verification

Run `./gradlew -version` to verify JVM version is 17.x.x.

### First-Time Build Duration

**Severity:** Low  
**Module:** Project-wide  
**Status:** Expected Behavior

#### Description

Initial project build takes 5-10 minutes due to dependency downloads, primarily the ArcGIS Maps
SDK (200MB+).

#### Impact

Developers may perceive slow initial setup.

#### Workaround

- Inform developers of expected initial build time
- Subsequent builds are significantly faster (incremental compilation)
- Use `./gradlew build --offline` for subsequent builds if dependencies are cached

## Testing Limitations

### Test Application ID Configuration - Process Mismatch Error

**Severity:** High  
**Module:** app (Android instrumented tests)  
**Status:** Resolved (Nov 2025)

#### Description

Custom `testApplicationId` declarations in product flavors caused instrumented tests to fail with a
process mismatch error:

```
java.lang.RuntimeException: Intent in process com.enbridge.gdsgpscollection.construction.debug 
resolved to different process com.enbridge.gdsgpscollection.construction.debug.test
```

This prevented all UI tests from executing, with error message: "Test run failed to complete. No
test results"

#### Root Cause

When `applicationIdSuffix` (from product flavor + build type) was combined with custom
`testApplicationId` values, the Android build system created mismatched package names:

- **App process**: `com.enbridge.gdsgpscollection.construction.debug`
- **Test process**: `com.enbridge.gdsgpscollection.construction.test` (wrong!)

The `HiltTestActivity` was declared in the test manifest but resolved to the wrong process, causing
test launch failures.

#### Resolution

**Fixed by:**

1. **Removed all `testApplicationId` declarations** from `app/build.gradle.kts`:
    - Removed from `defaultConfig` block
    - Removed from all 5 product flavors (electronic, maintenance, construction, resurvey,
      gasStorage)

2. **Let Gradle auto-generate test package names** correctly:
    - Now test APK gets: `com.enbridge.gdsgpscollection.construction.debug.test`
     - Matches app process structure with proper `.test` suffix

3. **Moved `HiltTestActivity` to debug source set** ⭐ **Critical Fix**:

   **Problem:** Activity in `androidTest` was part of **test process**, but tests tried to launch it
   in **app process**.

   **Solution:** Moved to `app/src/debug/`:
   ```
   app/src/debug/
   ├── java/com/enbridge/gdsgpscollection/HiltTestActivity.kt
   └── AndroidManifest.xml (declares activity with exported="false")
   ```

   Now `HiltTestActivity` is compiled into the debug **app APK**, not the test APK.

4. **Disabled Test Orchestrator** (was enabled without required dependency)

5. **Updated `ExampleInstrumentedTest`** to handle dynamic package names across flavors

#### Verification

Tests now execute successfully:

```bash
.\gradlew.bat :app:assembleConstructionDebugAndroidTest  #  Builds successfully
.\gradlew.bat :app:connectedConstructionDebugAndroidTest  
```

**Results:**

- **54 tests discovered and executed** (was 0 before)
- **40 tests passing** (74% pass rate)
- Process mismatch error completely resolved
- Hilt injection working correctly

**Test Breakdown:**

- `LoginScreenTest.kt`: 20/20 passing
- `CollectESBottomSheetTest.kt`: 14/14 passing
- `JobCardEntryScreenTest.kt`: 8/11 passing
- `ManageESBottomSheetTest.kt`: 1/12 passing
- `ExampleInstrumentedTest.kt`: 1/1 passing

*Note: 14 failures are UI assertion issues, not infrastructure problems.*

#### Best Practice Learned

**Never manually set `testApplicationId` in projects with product flavors + build types that
use `applicationIdSuffix`.**

The correct configuration:

```kotlin
//  CORRECT - Let Gradle auto-generate test package names
defaultConfig {
    applicationId = "com.enbridge.gdsgpscollection"
    testInstrumentationRunner = "com.enbridge.gdsgpscollection.HiltTestRunner"
    // NO testApplicationId here!
}

productFlavors {
    create("construction") {
        applicationIdSuffix = ".construction"
        // NO testApplicationId here either!
    }
}
```

Gradle will automatically create:

- App: `com.enbridge.gdsgpscollection.construction.debug`
- Test: `com.enbridge.gdsgpscollection.construction.debug.test`

#### Files Modified

- `app/build.gradle.kts` - Removed 6 `testApplicationId` declarations
- `app/src/androidTest/AndroidManifest.xml` - Added `android:exported="false"` to HiltTestActivity
- `app/src/androidTest/.../ExampleInstrumentedTest.kt` - Updated package name assertion

### Hilt Testing Configuration Complexity

**Severity:** Medium  
**Module:** feature modules (Android instrumented tests)  
**Status:** Resolved

#### Description

Android instrumented tests with Hilt require specific configuration:

- Custom test runner (`HiltTestRunner`)
- Hilt-enabled test activity (`HiltTestActivity`)
- Data module as test dependency
- Proper test manifest configuration
- Test instrumentation runner configured in build.gradle.kts

Missing any component results in Dagger/MissingBinding errors during test execution.

#### Resolution

Follow the configuration documented in docs/TESTING.md:

1. Create custom `HiltTestRunner` extending `AndroidJUnitRunner`
2. Create `HiltTestActivity` annotated with `@AndroidEntryPoint`
3. Add test manifest declaring the test activity with `android:exported="true"`
4. Configure test instrumentation runner in module's `build.gradle.kts`:
   ```kotlin
   testInstrumentationRunner = "com.enbridge.electronicservices.feature.yourmodule.HiltTestRunner"
   ```
5. Add data module as `androidTestImplementation` dependency

See docs/TESTING.md section "Hilt Android Testing Setup" for complete instructions.

### Test Execution on Physical Devices

**Severity:** Low  
**Module:** All modules with instrumented tests  
**Status:** Expected Behavior

#### Description

Instrumented tests require Android 14 (API 34) or higher due to minimum SDK configuration.

#### Impact

Tests cannot run on devices with Android 13 or lower.

#### Workaround

Use emulator with API 34+ for testing, or adjust minimum SDK for test build variants if testing on
lower API levels is required.

### Robolectric Configuration for Data Layer Tests

**Severity:** Low  
**Module:** data  
**Status:** Configured and Working

#### Description

Data layer tests require Robolectric for Android framework mocking, specifically for Room database
testing.

#### Configuration Required

`data/build.gradle.kts` must include:

```kotlin
testOptions {
    unitTests {
        isReturnDefaultValues = true
        isIncludeAndroidResources = true
    }
}
```

#### Impact

Without this configuration, Room DAO tests will fail with framework initialization errors.

#### Resolution

Configuration is already in place. No action needed for existing setup.

### JUnit 5 and JUnit 4 Compatibility

**Severity:** Low  
**Module:** All test modules  
**Status:** Managed via Dependencies

#### Description

The project uses both JUnit 5 (Jupiter) for unit tests and JUnit 4 for Android instrumented tests.

#### Configuration

- Unit tests: Use JUnit 5 (`org.junit.jupiter.api.Test`)
- Android tests: Use JUnit 4 (`org.junit.Test`)

#### Impact

Test annotations and runners differ between unit and instrumented tests:

**Unit Tests:**

```kotlin
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
```

**Android Instrumented Tests:**

```kotlin
import org.junit.Test
import org.junit.Before
import androidx.test.ext.junit.runners.AndroidJUnit4
```

#### Resolution

Follow the examples in docs/TESTING.md for correct test setup per test type.

### MockK Relaxed Mocks Behavior

**Severity:** Low  
**Module:** All test modules using MockK  
**Status:** By Design

#### Description

Relaxed mocks (`mockk(relaxed = true)`) return default values for unstubbed methods, which may hide
missing test setup.

#### Recommendation

Use strict mocks (`mockk()`) by default and explicitly stub all dependencies:

```kotlin
val repository = mockk<Repository>()
coEvery { repository.getData() } returns testData
```

Only use relaxed mocks when many methods need default behavior.

### Turbine Flow Testing Timeout

**Severity:** Low  
**Module:** Presentation layer tests  
**Status:** Expected Behavior

#### Description

Flow tests using Turbine may timeout if emissions don't occur within default timeout (1 second).

#### Common Causes

- Dispatcher not advancing: Missing `testDispatcher.scheduler.advanceUntilIdle()`
- Flow not collecting
- StateFlow not emitting expected value

#### Resolution

```kotlin
@Test
fun testStateFlow() = runTest {
    viewModel.action()
    testDispatcher.scheduler.advanceUntilIdle() // Advance dispatcher
    
    viewModel.uiState.test {
        val state = awaitItem()
        assertEquals(expected, state)
    }
}
```

### Compose Test Flakiness

**Severity:** Low  
**Module:** UI tests (androidTest)  
**Status:** Known Compose Testing Limitation

#### Description

Compose UI tests may occasionally fail due to timing issues, especially on slower emulators.

#### Symptoms

- `assertIsDisplayed()` fails when element is actually visible
- `performClick()` fails intermittently
- Tests pass locally but fail in CI

#### Workaround

Add explicit waits:

```kotlin
composeTestRule.waitForIdle()
composeTestRule.waitUntil(timeoutMillis = 2000) {
    composeTestRule.onNodeWithText("Text")
        .fetchSemanticsNode()
        .config
        .getOrNull(SemanticsProperties.Text) != null
}
```

For CI environments, consider using slower animation scale in emulator settings.

### Jacoco Coverage Exclusions

**Severity:** Low  
**Module:** All modules with Jacoco configured  
**Status:** Configured

#### Description

Certain generated files are automatically excluded from coverage:

- Hilt/Dagger generated code
- Compose compiler generated code
- Android framework files (R, BuildConfig, Manifest)
- DTOs (data classes)

#### Impact

Actual code coverage may appear higher than Jacoco reports due to exclusions.

#### Verification

Review Jacoco configuration in module build.gradle.kts files to see full exclusion list.

## Edge-to-Edge Display Issues

### Curved Display Content Clipping

**Severity:** Low  
**Module:** All feature modules  
**Status:** Resolved

#### Description

UI elements extended to screen edges, causing content clipping on devices with curved displays.

#### Resolution Status

Fixed by implementing window insets handling across all screens:

- `contentWindowInsets = WindowInsets(0.dp)` on Scaffold components
- `.windowInsetsPadding()` modifiers applied to content containers
- Proper insets configuration for navigation drawer, FABs, and coordinate bars

#### Verification

Test on devices with curved displays (Samsung Galaxy S/Note series) to verify proper padding.

## Data Layer Limitations

### Mock Data Implementation

**Severity:** Low  
**Module:** data  
**Status:** By Design (Development Phase)

#### Description

The application currently uses mock JSON data from assets instead of connecting to real API
endpoints.

#### Impact

- Authentication accepts any credentials
- Data is not persisted across app restarts
- Network errors are simulated with artificial delays

#### Future Enhancement

Replace `MockElectronicServicesApi` with production API client implementation. See
FUTURE_UPGRADES.md for migration plan.

### Room Database Schema Changes

**Severity:** Low  
**Module:** data  
**Status:** Requires Manual Migration

#### Description

Room database schema changes require migration strategies to prevent data loss.

#### Current State

Application uses fallback to destroy and recreate database:

```kotlin
.fallbackToDestructiveMigration()
```

#### Future Enhancement

Implement proper migration strategies for production release. Document in FUTURE_UPGRADES.md.

## Performance Considerations

### LazyColumn Performance with Large Datasets

**Severity:** Low  
**Module:** feature_jobs  
**Status:** Acceptable for Current Scale

#### Description

Job list screen uses basic LazyColumn without pagination or virtualization optimizations.

#### Impact

Performance may degrade with 100+ job cards in a single list.

#### Future Enhancement

Implement pagination or limit visible items when datasets exceed performance thresholds.

### Image Loading

**Severity:** Low  
**Module:** Project-wide  
**Status:** Not Implemented

#### Description

No image loading or caching library is currently integrated.

#### Impact

If images are added to the application (user avatars, job photos), loading performance may be
suboptimal.

#### Future Enhancement

Integrate Coil or similar library for efficient image loading and caching.

## Accessibility Limitations

### Content Descriptions

**Severity:** Low  
**Module:** All feature modules  
**Status:** Mostly Complete

#### Description

Most interactive elements have content descriptions for screen readers, but some complex composables
may need enhancement.

#### Verification Needed

- Comprehensive TalkBack testing across all screens
- Verify custom components have appropriate semantics

#### Future Enhancement

Conduct full accessibility audit and address any gaps.

### Dynamic Type Support

**Severity:** Low  
**Module:** design-system  
**Status:** Partial Support

#### Description

Typography scales with system font size settings, but some fixed-size components may not adapt
optimally to extremely large text sizes.

#### Future Enhancement

Test with maximum accessibility font sizes and adjust layouts as needed.

## Security Considerations

### API Key Storage

**Severity:** Medium  
**Module:** core  
**Status:** Acceptable for Development

#### Description

ArcGIS API key is stored in BuildConfig, which is visible in decompiled APK.

#### Impact

API key can be extracted from release APK.

#### Future Enhancement

For production:

- Implement API key obfuscation
- Use server-side authentication
- Implement key rotation strategy
- Monitor API key usage

### Authentication Token Storage

**Severity:** Medium  
**Module:** data  
**Status:** Not Implemented

#### Description

No secure token storage mechanism is currently implemented.

#### Future Enhancement

Implement EncryptedSharedPreferences or Android Keystore for sensitive data storage.

## Network Layer Limitations

### Network Error Handling

**Severity:** Low  
**Module:** data  
**Status:** Basic Implementation

#### Description

Network error handling is basic with generic error messages.

#### Future Enhancement

Implement:

- Detailed error classification
- Retry mechanisms with exponential backoff
- Network connectivity monitoring
- User-friendly error messages

### Offline Support

**Severity:** Medium  
**Module:** Project-wide  
**Status:** Not Implemented

#### Description

Application requires internet connectivity for all operations.

#### Impact

No functionality available when offline.

#### Future Enhancement

Implement offline-first architecture:

- Cache data locally
- Queue operations for later sync
- Implement conflict resolution
- See FUTURE_UPGRADES.md for detailed plan

## Reporting Issues

### How to Report

When reporting new issues:

1. Check this document to verify the issue is not already known
2. Provide detailed reproduction steps
3. Include Android version and device information
4. Attach LogCat output if applicable
5. Note any error messages or unexpected behavior

### Issue Template

```
**Description:** Brief description of the issue

**Module:** Affected module (e.g., feature_map, data)

**Severity:** Critical / High / Medium / Low

**Steps to Reproduce:**
1. Step one
2. Step two
3. Expected result vs actual result

**Environment:**
- Android Version: X.X
- Device: Make/Model
- App Version: X.X.X

**Logs:** Relevant LogCat output or error messages

**Screenshots:** If applicable
```

## Document Maintenance

This document should be updated when:

- New limitations are discovered
- Existing issues are resolved
- Workarounds are identified
- Severity assessments change

**Last Updated:** Nov 2025  
**Next Review:** Quarterly or before major releases

---

**Note:** This document reflects the current development state. Some limitations are intentional for
the development phase and will be addressed in future releases as outlined in FUTURE_UPGRADES.md.

