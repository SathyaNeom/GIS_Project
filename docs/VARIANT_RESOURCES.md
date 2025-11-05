# Build Variant Resources Guide

## Overview

This document explains how build variants affect app resources, particularly launcher icons and
in-app branding components like `AppLogo`. The GdsGpsCollection application uses Android's product
flavors to provide different branding for each service type.

---

## Build Variants

The application has **5 product flavors** defined in `app/build.gradle.kts`:

| Variant | Application ID Suffix | App Name | Description |
|---------|----------------------|----------|-------------|
| `electronic` | `.electronic` | GDS GPS Collection | For electronic service operations |
| `maintenance` | `.maintenance` | Maintenance | For maintenance operations |
| `construction` | `.construction` | Construction | For construction operations |
| `resurvey` | `.resurvey` | Resurvey | For resurvey operations |
| `gasStorage` | `.gasstorage` | Gas Storage | For gas storage operations |

---

## How Variants Affect Launcher Icons

### Resource Resolution Mechanism

Android uses a **resource overlay system** where variant-specific resources override base resources:

```
Build Process:
1. Start with resources from app/src/main/res/
2. Overlay with resources from app/src/{variant}/res/
3. Variant-specific resources take precedence
```

### Launcher Icon Structure

Each variant has its own launcher icon defined in:

```
app/src/{variant}/res/drawable/
├── ic_launcher_background.xml    # Background layer
└── ic_launcher_foreground.xml    # Foreground layer (icon)
```

The launcher uses adaptive icons defined in:

```
app/src/main/res/mipmap-anydpi/
├── ic_launcher.xml
└── ic_launcher_round.xml
```

These reference `@drawable/ic_launcher_background` and `@drawable/ic_launcher_foreground`, which are
automatically resolved to the variant-specific versions.

### Variant-Specific Icons

#### Electronic Variant

- **Background**: Yellow (#FFC107)
- **Foreground**: Build/Wrench icon
- **File**: `app/src/electronic/res/drawable/ic_launcher_foreground.xml`

#### Maintenance Variant

- **Background**: Blue (#2196F3)
- **Foreground**: Gear/Settings icon
- **File**: `app/src/maintenance/res/drawable/ic_launcher_foreground.xml`

#### Construction Variant

- **Background**: Deep Orange (#FF5722)
- **Foreground**: Hammer/Traffic cone icon
- **File**: `app/src/construction/res/drawable/ic_launcher_foreground.xml`

#### Resurvey Variant

- **Background**: Custom color
- **Foreground**: Survey/Location icon
- **File**: `app/src/resurvey/res/drawable/ic_launcher_foreground.xml`

#### Gas Storage Variant

- **Background**: Custom color
- **Foreground**: Gas tank/Propane icon
- **File**: `app/src/gasStorage/res/drawable/ic_launcher_foreground.xml`

---

## AppLogo Component Implementation

### Architecture

The `AppLogo` component has been designed to use variant-specific icons optimized for in-app
display:

```kotlin
@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    showAppName: Boolean = true
)
```

### Key Features

1. **Automatic Variant Detection**
   - Uses `BuildConfig.APP_NAME` for the app name
   - Uses `R.drawable.ic_app_logo` for the icon
   - Both automatically resolve based on the current build variant

2. **Resource-Based Icon**
   - Uses `painterResource(id = R.drawable.ic_app_logo)`
   - Android's resource system automatically picks the correct variant-specific drawable
   - Optimized for in-app display (48dp square icon)
   - Note: We use `ic_app_logo` instead of `ic_launcher_foreground` because launcher icons are 108dp
     with safe zones and don't render well in Compose UI

3. **Theme Integration**
   - Icon tint uses `MaterialTheme.colorScheme.primary`
   - Text color uses `MaterialTheme.colorScheme.onBackground`
   - Fully integrated with Material Design 3 theming

### Icon Resources

Each variant has two icon resources:

1. **`ic_launcher_foreground.xml`** - For launcher icons (108dp with safe zones)
2. **`ic_app_logo.xml`** - For in-app display (48dp optimized icon)

**Location**: `app/src/{variant}/res/drawable/ic_app_logo.xml`

### Usage Examples

#### Basic Usage (Automatic variant detection)

```kotlin
AppLogo()
```

#### Without App Name

```kotlin
AppLogo(showAppName = false)
```

#### With Custom Name (for special cases)

```kotlin
AppLogoWithCustomName(appName = "Custom Title")
```

---

## BuildConfig Fields

Each variant defines the following BuildConfig fields:

```kotlin
buildConfigField("String", "APP_VARIANT", "\"electronic\"")
buildConfigField("String", "APP_NAME", "\"Electronic Services\"")
```

These can be accessed in code:

```kotlin
val variantName = BuildConfig.APP_VARIANT  // e.g., "electronic"
val appName = BuildConfig.APP_NAME         // e.g., "Electronic Services"
```

---

## Resource Values

Each variant also defines resource values:

```kotlin
resValue("string", "app_name", "GDS GPS Collection")
```

This creates a string resource that can be accessed via:

```kotlin
stringResource(R.string.app_name)
```

**Note**: The `app_name` string resource is used by Android for the launcher label.

---

## Adding New Variants

To add a new variant:

### 1. Define the Product Flavor

Add to `app/build.gradle.kts`:

```kotlin
create("newVariant") {
    dimension = "variant"
    applicationIdSuffix = ".newvariant"
    versionNameSuffix = "-new-variant"
    buildConfigField("String", "APP_VARIANT", "\"new-variant\"")
    buildConfigField("String", "APP_NAME", "\"New Variant\"")
    resValue("string", "app_name", "New Variant")
}
```

### 2. Create Variant Resources

Create the directory structure:

```
app/src/newVariant/res/drawable/
```

### 3. Add Custom Icons

Create two files:

**ic_launcher_background.xml**:

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#YOUR_COLOR"
        android:pathData="M0,0h108v108h-108z" />
</vector>
```

**ic_launcher_foreground.xml**:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <!-- Background circle -->
    <path
        android:fillColor="#YOUR_ACCENT_COLOR"
        android:pathData="M54,54m-42,0a42,42 0,1 1,84 0a42,42 0,1 1,-84 0" />
    <!-- Your custom icon paths -->
    <path
        android:fillColor="#FFFFFF"
        android:pathData="YOUR_ICON_PATH_DATA" />
</vector>
```

### 4. Sync and Build

```bash
.\gradlew.bat :app:assembleNewVariantDebug
```

The `AppLogo` component will automatically use the new variant's icon without any code changes!

---

## Benefits of This Approach

### ✅ Consistency

- Launcher icon and in-app branding always match
- No need to manually maintain separate icon sets

### ✅ Maintainability

- Single source of truth for each variant's branding
- Changes to launcher icon automatically apply to AppLogo

### ✅ Scalability

- Easy to add new variants
- No code changes needed in UI components

### ✅ Type Safety

- Uses Android's resource system
- Compile-time validation of resources

### ✅ Flexibility

- Can override with custom names when needed
- Supports showing/hiding app name

---

## Best Practices

1. **Keep Icons Consistent**
    - Use the same style/theme across all variants
    - Maintain similar complexity and visual weight

2. **Test All Variants**
    - Build and test each variant to ensure icons render correctly
    - Check on different screen densities

3. **Use Vector Drawables**
    - Always use `<vector>` XML for launcher icons
    - Ensures crisp rendering at all sizes

4. **Color Accessibility**
    - Ensure sufficient contrast between background and foreground
    - Test with different launcher styles (themed icons, etc.)

5. **Documentation**
    - Document the meaning/purpose of each variant's icon
    - Include design rationale in comments

---

## Troubleshooting

### Issue: AppLogo shows wrong icon

**Solution**: Clean and rebuild the project

```bash
.\gradlew.bat clean
.\gradlew.bat :app:assembleElectronicDebug
```

### Issue: Launcher icon not updating

**Solution**: Uninstall the app and reinstall

```bash
.\gradlew.bat :app:uninstallElectronicDebug
.\gradlew.bat :app:installElectronicDebug
```

### Issue: Icon appears too small/large

**Solution**: Adjust the `Modifier.size()` parameter in AppLogo:

```kotlin
modifier = Modifier.size(120.dp)  // Adjust as needed
```

---

## Technical Details

### Resource Merging Priority (highest to lowest)

1. Build variant specific (e.g., `app/src/electronic/res/`)
2. Build type specific (e.g., `app/src/debug/res/`)
3. Main source set (e.g., `app/src/main/res/`)

### BuildConfig Generation

At build time, Gradle generates `BuildConfig.java` with variant-specific constants:

```java
public final class BuildConfig {
  public static final String APP_VARIANT = "electronic";
  public static final String APP_NAME = "Electronic Services";
  // ...
}
```

This is accessible from Kotlin as `BuildConfig.APP_NAME`.

---

## Related Files

- `app/build.gradle.kts` - Variant definitions
- `app/src/main/java/com/enbridge/gdsgpscollection/designsystem/components/AppLogo.kt` - AppLogo
  component
- `app/src/{variant}/res/drawable/ic_launcher_foreground.xml` - Variant-specific icons
- `app/src/main/AndroidManifest.xml` - References to launcher icons

---

## Additional Resources

- [Android Product Flavors Documentation](https://developer.android.com/studio/build/build-variants)
- [Adaptive Icons Guide](https://developer.android.com/guide/practices/ui_guidelines/icon_design_adaptive)
- [Vector Drawable Reference](https://developer.android.com/reference/android/graphics/drawable/VectorDrawable)

---

**Last Updated**: November 2025  
**Author**: Development Team
