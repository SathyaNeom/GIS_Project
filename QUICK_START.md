# Quick Start Guide

This guide provides step-by-step instructions to set up and run the GPS_Device_Proj Android
application.

## Prerequisites

Ensure the following tools are installed before proceeding:

- Android Studio Ladybug (2024.2.1) or later
- JDK 17 or higher
- Android SDK with API Level 34 or higher
- Git for version control
- ArcGIS Developer account for API key

## Initial Setup

### Step 1: Clone the Repository

```bash
git clone <repository-url>
cd GPS_Device_Proj
```

### Step 2: Obtain ArcGIS API Key

1. Navigate to [ArcGIS Developers](https://developers.arcgis.com)
2. Sign in or create a free account
3. Go to **API Keys** section
4. Create a new API key with the following privileges:
    - Location services - Basemaps
    - Location services - Geocoding
    - Location services - Routing
5. Copy the generated API key

### Step 3: Configure API Key

Create or edit the `local.properties` file in the project root directory:

```properties
sdk.dir=/path/to/android/sdk
ARCGIS_API_KEY=your_actual_api_key_here
```

Replace `your_actual_api_key_here` with the API key obtained in Step 2.

### Step 4: Configure JDK

If prompted about Gradle JDK configuration in Android Studio:

1. Navigate to **File → Settings** (Windows/Linux) or **Android Studio → Preferences** (macOS)
2. Go to **Build, Execution, Deployment → Build Tools → Gradle**
3. Set **Gradle JDK** to **Embedded JDK (jbr-17)**
4. Click **Apply** and **OK**

Alternatively, click the **Use Embedded JDK** button in the error banner if displayed.

### Step 5: Sync Gradle Dependencies

In Android Studio:

1. Click **File → Sync Project with Gradle Files**
2. Wait for synchronization to complete (2-5 minutes on first run)

Or via command line:

```bash
./gradlew --refresh-dependencies
```

### Step 6: Build the Project

```bash
./gradlew build
```

Verify the build completes successfully without errors.

## Running the Application

### Main Application

#### Using Android Studio:

1. Select **app** from the run configuration dropdown
2. Choose a target device (emulator or physical device)
3. Click **Run** or press `Shift+F10`

#### Using Command Line:

```bash
./gradlew installDebug
```

### Design System Catalog

To explore the UI component library:

#### Using Android Studio:

1. Select **app-catalog** from the run configuration dropdown
2. Choose a target device
3. Click **Run** or press `Shift+F10`

#### Using Command Line:

```bash
./gradlew :app-catalog:installDebug
```

## Device Configuration

### Emulator Setup

1. Open **Tools → Device Manager** in Android Studio
2. Click **Create Device**
3. Select a device definition (recommended: Pixel 8)
4. Select system image: **API 34** (UpsideDownCake) or higher
5. Configure hardware settings as needed
6. Click **Finish**

### Physical Device Setup

1. Enable **Developer Options** on the device:
    - Go to **Settings → About Phone**
    - Tap **Build Number** seven times
2. Enable **USB Debugging** in **Developer Options**
3. Connect device via USB
4. Accept the USB debugging authorization prompt
5. Verify device appears in Android Studio device selector

## Verification

### Application Launch Verification

After launching the application, verify the following:

1. **Login Screen** displays with username and password fields
2. Enter any credentials and tap **LOGIN**
3. **Map Screen** loads with ArcGIS map centered on Toronto
4. **Navigation Drawer** opens when tapping the menu icon
5. Navigation between **Jobs** and **Sync** screens functions correctly

### Design System Catalog Verification

When running the catalog application:

1. Scrollable list of component sections displays
2. Theme toggle button functions correctly
3. Interactive components respond to user input
4. Dialogs, bottom sheets, and snackbars can be triggered
5. Light and dark themes render properly

## Running Tests

The application includes comprehensive testing with 200+ tests achieving ~90% code coverage.

### Quick Test Commands

```bash
# Run all unit tests
./gradlew test

# Run unit tests for specific module
./gradlew :domain:testDebugUnitTest
./gradlew :data:testDebugUnitTest
./gradlew :feature_jobs:testDebugUnitTest

# Run all instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run tests with coverage report
./gradlew testDebugUnitTest jacocoTestReport
```

### Viewing Test Results

After running tests, view reports at:

- **Unit Test Reports**: `{module}/build/reports/tests/testDebugUnitTest/index.html`
- **Coverage Reports**: `{module}/build/reports/jacoco/test/html/index.html`
- **Unified Coverage**: `build/reports/jacoco/html/index.html`

### Test Coverage Overview

| Layer                             | Tests | Coverage |
|-----------------------------------|-------|----------|
| **Domain** (Use Cases)            | 50+   | ~95%     |
| **Data** (Repositories & Mappers) | 69    | ~90%     |
| **Presentation** (ViewModels)     | 55+   | ~90%     |
| **UI** (Compose Screens)          | 25+   | ~60%     |

For detailed testing information, see [docs/TESTING.md](docs/TESTING.md).

## Troubleshooting

### Gradle Sync Failures

**Issue:** Gradle sync fails with dependency resolution errors

**Solution:**
```bash
./gradlew clean
./gradlew --stop
./gradlew --refresh-dependencies
```

### ArcGIS Map Not Displaying

**Issue:** Map shows blank screen or fails to load

**Resolution:**

1. Verify API key is correctly set in `local.properties`
2. Confirm API key has required privileges enabled
3. Check internet connectivity
4. Review LogCat for ArcGIS-related errors

### Build Failures

**Issue:** Build fails with compilation errors

**Solution:**

1. **File → Invalidate Caches → Invalidate and Restart**
2. Ensure JDK 17 is properly configured
3. Verify all required SDK components are installed
4. Clean and rebuild:

```bash
./gradlew clean build
```

### JDK Configuration Errors

**Issue:** Invalid Gradle JDK configuration error

**Solution:**

1. Open **Settings → Build Tools → Gradle**
2. Set **Gradle JDK** to **Embedded JDK (jbr-17)**
3. Click **Apply** and sync project

### Device Not Detected

**Issue:** Physical device not appearing in device selector

**Resolution:**

1. Verify USB debugging is enabled
2. Try different USB cable or port
3. Install device-specific USB drivers if on Windows
4. Check device authorization prompt

### Test Failures

**Issue:** Tests failing to run or compile

**Solution:**

1. Ensure all test dependencies are synced:
   ```bash
   ./gradlew --refresh-dependencies
   ```
2. Check that Robolectric SDK is downloaded (for data layer tests)
3. For Android tests, ensure device/emulator is running Android 14+
4. See [docs/TESTING.md](docs/TESTING.md) troubleshooting section

## Configuration Files

### local.properties

This file contains environment-specific configuration and should not be committed to version
control:

```properties
sdk.dir=/path/to/android/sdk
ARCGIS_API_KEY=your_api_key
```

### gradle.properties

Project-wide Gradle properties:

```properties
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m
org.gradle.parallel=true
android.useAndroidX=true
kotlin.code.style=official
```

## Next Steps

After successful setup:

1. Review [docs/TESTING.md](docs/TESTING.md) for testing strategies and guidelines
2. Consult **ARCHITECTURE.md** for detailed architecture documentation
3. Explore **design-system/** for UI component reference
4. Review [docs/KNOWN_ISSUES.md](docs/KNOWN_ISSUES.md) for current limitations
5. Check [docs/FUTURE_UPGRADES.md](docs/FUTURE_UPGRADES.md) for roadmap

## Build Commands Reference

```bash
# Clean build
./gradlew clean

# Build all modules
./gradlew build

# Assemble debug APK
./gradlew assembleDebug

# Assemble release APK
./gradlew assembleRelease

# Install debug APK
./gradlew installDebug

# Run unit tests
./gradlew test

# Run unit tests with coverage
./gradlew testDebugUnitTest jacocoTestReport

# Run instrumented tests
./gradlew connectedAndroidTest

# Check dependencies
./gradlew :app:dependencies

# Gradle version
./gradlew -version
```

## Environment Verification

To verify your development environment:

```bash
# Check Gradle version (should be 8.12.3)
./gradlew -version

# Check JVM version (should be 17.x.x)
java -version

# List available tasks
./gradlew tasks

# Check project structure
./gradlew projects

# Run a quick test to verify setup
./gradlew :domain:testDebugUnitTest
```

## Additional Resources

- **Android Studio Documentation:** https://developer.android.com/studio
- **ArcGIS Maps SDK Documentation:** https://developers.arcgis.com/kotlin
- **Kotlin Documentation:** https://kotlinlang.org/docs/home.html
- **Jetpack Compose Documentation:** https://developer.android.com/jetpack/compose
- **Testing Documentation:** [docs/TESTING.md](docs/TESTING.md)

## Support

For technical issues or questions:

1. Check [docs/KNOWN_ISSUES.md](docs/KNOWN_ISSUES.md) for documented problems
2. Review LogCat output for error details
3. Consult project documentation in the `docs/` directory
4. Contact the development team for assistance

---

**Quick Links:**

- [Main README](README.md) - Comprehensive project overview
- [Testing Guide](docs/TESTING.md) - Complete testing documentation
- [Known Issues](docs/KNOWN_ISSUES.md) - Current limitations and workarounds
- [Future Upgrades](docs/FUTURE_UPGRADES.md) - Roadmap and planned enhancements

