

# Build Variants Implementation Summary

## Overview

This document describes the implementation of multiple build variants (product flavors) for the
Electronic Services Android application. The implementation allows the same codebase to produce five
different applications with unique branding and feature sets.

## Build Variants

### 1. Electronic Services (electronic)

- **App Name**: Electronic Services
- **Icon**: Build/Wrench (Material Design)
- **Color**: Yellow (#FFC107)
- **Application ID**: com.enbridge.electronicservices.electronic
- **Features**: All features including ES Job Card Entry

### 2. Maintenance (maintenance)

- **App Name**: Maintenance
- **Icon**: Handyman (Material Design)
- **Color**: Blue (#2196F3)
- **Application ID**: com.enbridge.electronicservices.maintenance
- **Features**: All features except ES Job Card Entry (hidden)

### 3. Construction (construction)

- **App Name**: Construction
- **Icon**: Construction (Material Design)
- **Color**: Orange/Red (#FF5722)
- **Application ID**: com.enbridge.electronicservices.construction
- **Features**: All features except ES Job Card Entry (hidden)

### 4. Resurvey (resurvey)

- **App Name**: Resurvey
- **Icon**: Location Pin (Material Design)
- **Color**: Green (#4CAF50)
- **Application ID**: com.enbridge.electronicservices.resurvey
- **Features**: All features except ES Job Card Entry (hidden)

### 5. Gas Storage (gasStorage)

- **App Name**: Gas Storage
- **Icon**: Propane Tank (Material Design)
- **Color**: Purple (#9C27B0)
- **Application ID**: com.enbridge.electronicservices.gasstorage
- **Features**: All features except ES Job Card Entry (hidden)

## Implementation Details

### 1. Build Configuration (`app/build.gradle.kts`)

Added product flavors with dimension "variant":

```kotlin
flavorDimensions += "variant"
productFlavors {
    create("electronic") {
        dimension = "variant"
        applicationIdSuffix = ".electronic"
        versionNameSuffix = "-electronic"
        buildConfigField("String", "APP_VARIANT", "\"electronic\"")
        buildConfigField("String", "APP_NAME", "\"Electronic Services\"")
        resValue("string", "app_name", "Electronic Services")
    }
    // ... other flavors
}

buildFeatures {
    compose = true
    buildConfig = true  // Required for BuildConfig fields
}
```

### 2. Launcher Icons

Created variant-specific launcher icon resources:

**Directory Structure**:

```
app/src/
├── electronic/
│   └── res/
│       └── drawable/
│           ├── ic_launcher_foreground.xml
│           └── ic_launcher_background.xml
├── maintenance/
│   └── res/
│       └── drawable/
│           ├── ic_launcher_foreground.xml
│           └── ic_launcher_background.xml
├── construction/
│   └── res/
│       └── drawable/
│           ├── ic_launcher_foreground.xml
│           └── ic_launcher_background.xml
├── resurvey/
│   └── res/
│       └── drawable/
│           ├── ic_launcher_foreground.xml
│           └── ic_launcher_background.xml
└── gasStorage/
    └── res/
        └── drawable/
            ├── ic_launcher_foreground.xml
            └── ic_launcher_background.xml
```

Each variant has unique vector drawable icons using Material Design icon themes.

### 3. App Logo Component (`design-system/src/main/java/.../AppLogo.kt`)

Updated `AppLogo` to accept parameters:

```kotlin
@Composable
fun AppLogo(
    appName: String = stringResource(R.string.app_logo_text),
    icon: ImageVector = Icons.Default.Build,
    modifier: Modifier = Modifier
)
```

Added helper function for variant icon mapping:

```kotlin
fun getVariantIcon(variant: String): ImageVector {
    return when (variant) {
        "electronic" -> Icons.Default.Build
        "maintenance" -> Icons.Default.Handyman
        "construction" -> Icons.Default.Construction
        "resurvey" -> Icons.Default.LocationOn
        "gas-storage" -> Icons.Default.Propane
        else -> Icons.Default.Build
    }
}
```

### 4. Navigation Drawer (`design-system/src/main/java/.../ESNavigationDrawer.kt`)

Added conditional display of ES Job Card Entry:

```kotlin
@Composable
fun ESNavigationDrawerContent(
    // ... existing parameters
    showJobCardEntry: Boolean = true
) {
    // ... existing code
    
    // ES Job Card Entry - conditionally shown
    if (showJobCardEntry) {
        ESNavigationDrawerItem(
            icon = Icons.Default.List,
            label = stringResource(R.string.es_job_card_entry),
            // ... rest of implementation
        )
    }
}
```

### 5. Main Map Screen (`feature_map/src/main/java/.../MainMapScreen.kt`)

Updated to accept variant parameters:

```kotlin
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
    // Determine if Job Card Entry should be shown
    val showJobCardEntry = appVariant == "electronic"
    
    // Pass to navigation drawer
    ESNavigationDrawerContent(
        // ... other parameters
        showJobCardEntry = showJobCardEntry
    )
    
    // Use appName in top bar
    AppTopBar(
        title = appName,
        // ... rest of implementation
    )
}
```

### 6. Login Screen (`feature_auth/src/main/java/.../LoginScreen.kt`)

Updated to accept branding parameters:

```kotlin
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    appName: String = "Electronic Services",
    appIcon: ImageVector = Icons.Default.Build,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    // Use customizable logo
    AppLogo(
        appName = appName,
        icon = appIcon
    )
}
```

### 7. Navigation Graph (`app/src/main/java/.../navigation/NavGraph.kt`)

Updated to pass BuildConfig values:

```kotlin
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(/* ... */) {
        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { /* ... */ },
                onForgotPasswordClick = { /* ... */ },
                appName = BuildConfig.APP_NAME,
                appIcon = getVariantIcon(BuildConfig.APP_VARIANT)
            )
        }
        
        composable(route = Screen.Map.route) {
            MainMapScreen(
                onNavigateToJobCardEntry = { /* ... */ },
                onAddNewFeature = { /* ... */ },
                onSearchClick = { /* ... */ },
                onLogout = { /* ... */ },
                appName = BuildConfig.APP_NAME,
                appVariant = BuildConfig.APP_VARIANT
            )
        }
    }
}
```

### 8. String Resources

Created comprehensive string resource files:

**app/src/main/res/values/strings.xml**:

```xml
<resources>
    <string name="app_name">Electronic Services</string>
    <!-- Variant identifiers -->
    <string name="variant_electronic">electronic</string>
    <string name="variant_maintenance">maintenance</string>
    <!-- ... -->
</resources>
```

**design-system/src/main/res/values/strings.xml**:

```xml
<resources>
    <!-- Navigation Drawer -->
    <string name="task_list">Task List</string>
    <string name="collect_electronic_services">Collect Electronic Services</string>
    <string name="es_job_card_entry">ES Job Card Entry</string>
    <!-- ... common UI strings -->
</resources>
```

**feature_map/src/main/res/values/strings.xml**:

```xml
<resources>
    <!-- Map Screen -->
    <string name="map_screen_title">Electronic Services</string>
    <!-- Map Actions -->
    <string name="zoom_in">Zoom In</string>
    <!-- ... map-specific strings -->
</resources>
```

## Building and Running

### Build Specific Variant

```bash
# Build electronic variant (debug)
./gradlew assembleElectronicDebug

# Build maintenance variant (release)
./gradlew assembleMaintenanceRelease

# Install electronic variant directly to device
./gradlew installElectronicDebug
```

### Build All Variants

```bash
# Build all debug variants
./gradlew assembleDebug

# Build all release variants
./gradlew assembleRelease

# Build everything
./gradlew assemble
```

### Android Studio

1. Open **Build Variants** panel (View → Tool Windows → Build Variants)
2. Select desired variant from dropdown (e.g., "electronicDebug", "maintenanceRelease")
3. Click Run or Debug as normal

## Testing

### Run Tests for Specific Variant

```bash
# Run unit tests for electronic variant
./gradlew testElectronicDebugUnitTest

# Run instrumented tests for maintenance variant
./gradlew connectedMaintenanceDebugAndroidTest
```

## Side-by-Side Installation

Because each variant has a unique application ID suffix, all variants can be installed
simultaneously on the same device:

- com.enbridge.electronicservices.electronic
- com.enbridge.electronicservices.maintenance
- com.enbridge.electronicservices.construction
- com.enbridge.electronicservices.resurvey
- com.enbridge.electronicservices.gasstorage

This is useful for:

- Testing multiple variants on the same device
- QA validation
- Demo purposes

## Conditional Feature Logic

The primary conditional feature is ES Job Card Entry visibility:

```kotlin
// In MainMapScreen.kt
val showJobCardEntry = appVariant == "electronic"

// Pass to drawer
ESNavigationDrawerContent(
    showJobCardEntry = showJobCardEntry,
    // ... other params
)
```

To add more variant-specific features:

```kotlin
when (appVariant) {
    "electronic" -> {
        // Electronic-specific features
    }
    "maintenance" -> {
        // Maintenance-specific features
    }
    "construction" -> {
        // Construction-specific features
    }
    "resurvey" -> {
        // Resurvey-specific features
    }
    "gas-storage" -> {
        // Gas storage-specific features
    }
}
```

## Future Enhancements

### Variant-Specific Features

1. **Unique Data Models**: Each variant could have specific data structures
2. **Custom Workflows**: Different business logic per variant
3. **API Endpoints**: Variant-specific backend configurations
4. **Theme Customization**: Unique color schemes per variant

### Build Configuration

1. **Signing Configs**: Variant-specific signing keys
2. **ProGuard Rules**: Variant-specific obfuscation
3. **Build Types**: Combine with buildTypes for more combinations (electronicDebug,
   electronicRelease, etc.)

### Resource Management

1. **Localization**: Translate strings for each variant and language
2. **Assets**: Variant-specific images, fonts, or other assets
3. **Configurations**: Variant-specific configuration files

## Troubleshooting

### BuildConfig Not Found

If you see "Unresolved reference: BuildConfig", ensure:

1. `buildFeatures { buildConfig = true }` is set in `app/build.gradle.kts`
2. Project is synced after gradle changes
3. Clean and rebuild: `./gradlew clean build`

### Wrong Variant Selected

Check the Build Variants panel in Android Studio and ensure the correct variant is selected for the
app module.

### Icon Not Showing

1. Verify variant-specific drawable resources exist
2. Check file names match exactly: `ic_launcher_foreground.xml` and `ic_launcher_background.xml`
3. Clean project and rebuild

## Best Practices

1. **Always use BuildConfig** for variant-dependent logic rather than hardcoding
2. **Externalize all strings** to support future localization
3. **Test each variant** independently to ensure features work correctly
4. **Document variant differences** for team awareness
5. **Use consistent naming** for resources across variants

## Conclusion

The build variants implementation provides a flexible, maintainable way to create multiple
applications from a single codebase. Each variant has its own identity while sharing the core
functionality and architecture of the Electronic Services application.
