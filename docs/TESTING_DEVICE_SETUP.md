# Device Setup for Instrumented Tests

## Overview

This guide explains how to set up your Android device for running instrumented tests (UI tests)
successfully.

## Requirements

### Minimum API Level

This app requires **API Level 28 (Android 9.0)** or higher due to the ArcGIS Maps SDK dependency.

**Supported Devices:**

- Android 9.0 (API 28) and above
- Android 15.0 (API 35) ✅ - Like your Xiaomi 24094RAD4I

### Device Compatibility Check

Your device shows:

```
Device: Xiaomi 24094RAD4I
Android: 15.0 ("VanillaCream")
API Level: 35-ext19
Type: Physical
Status: ✅ Compatible (API 35 > minSdk 28)
```

---

## Common Issues and Solutions

### Issue 1: "No compatible devices connected"

**Symptoms:**

```
> No compatible devices connected.[TestRunner] FAILED
Found 1 connected device(s), 0 of which were compatible.
```

**Causes:**

1. **API Level Too Low**: Your device API < minSdk (28)
2. **Device Not Detected**: USB debugging not enabled
3. **ABI Mismatch**: Device architecture not supported

**Solutions:**

#### Solution A: Check API Level

```bash
# Check device API level
adb shell getprop ro.build.version.sdk
```

If the result is less than 28, you need a newer device or emulator.

#### Solution B: Enable USB Debugging

1. Go to **Settings → About Phone**
2. Tap **Build Number** 7 times to enable Developer Options
3. Go to **Settings → Developer Options**
4. Enable **USB Debugging**
5. Reconnect device and accept the USB debugging prompt

#### Solution C: Verify Device Connection

```bash
# List connected devices
adb devices
```

Expected output:

```
List of devices attached
24094RAD4I    device
```

If you see "unauthorized", accept the prompt on your device.

---

### Issue 2: "INSTALL_FAILED_USER_RESTRICTED"

**Symptoms:**

```
Error: INSTALL_FAILED_USER_RESTRICTED: Install canceled by user
Failed to commit install session
```

**This is your current issue!**

**Cause:** Xiaomi devices (MIUI) have strict security settings that block app installations from ADB
by default.

**Solutions for Xiaomi/MIUI Devices:**

#### Solution A: Enable "Install via USB" (MIUI 12+)

1. Go to **Settings → Additional Settings**
2. Select **Developer Options**
3. Find and enable **"Install via USB"** or **"USB Debugging (Security Settings)"**
4. You may need to **sign in with your Mi Account** for this option to appear
5. Accept any security warnings

#### Solution B: Turn Off MIUI Optimization

1. Go to **Settings → Additional Settings → Developer Options**
2. Scroll to bottom
3. **Disable "MIUI Optimization"**
4. Reboot device
5. Try test again

#### Solution C: Disable Install Restrictions

1. Go to **Settings → Passwords & Security**
2. Select **Privacy → Special Permissions**
3. Find **"Install Unknown Apps"**
4. Enable for **"ADB"** or **"Settings"**

#### Solution D: Use Wireless Debugging (Android 11+)

1. Enable **Wireless Debugging** in Developer Options
2. Connect via `adb connect <ip>:<port>`
3. Try tests again

#### Solution E: Use an Emulator (Recommended Alternative)

If Xiaomi restrictions persist, use an Android Studio emulator:

1. Open **AVD Manager** in Android Studio
2. Create a new virtual device (Pixel 7, API 35)
3. Start the emulator
4. Run tests

```bash
.\gradlew.bat :feature_map:connectedDebugAndroidTest
```

---

### Issue 3: Build Fails with Manifest Merger Error

**Symptoms:**

```
uses-sdk:minSdkVersion X cannot be smaller than version Y declared in library [...]
```

**Cause:** A dependency requires a higher minSdk than your project.

**Solution:** Update minSdk in `build.gradle.kts` files:

```kotlin
defaultConfig {
    minSdk = 28  // Required for ArcGIS Maps SDK
}
```

This has been fixed in your project.

---

### Issue 4: Kapt/Dagger Warnings

**Symptoms:**

```
The following options were not recognized by any processor: 'dagger.fastinit...'
w: Kapt currently doesn't support language version 2.0+. Falling back to 1.9.
```

**Cause:** These are warnings, not errors. Kapt doesn't fully support Kotlin 2.0 yet.

**Impact:** None - tests will still run. These can be safely ignored.

---

## Step-by-Step: Running Instrumented Tests

### 1. Prepare Device

**For Physical Device (Xiaomi):**

```
✅ Enable USB Debugging
✅ Enable "Install via USB" (MIUI specific)
✅ Connect device via USB
✅ Accept USB debugging prompt on device
```

**For Emulator:**

```
✅ Start emulator from AVD Manager
✅ Wait for boot to complete
```

### 2. Verify Device Connection

```bash
# Check connected devices
adb devices

# Should show:
# List of devices attached
# 24094RAD4I    device         (or emulator-5554)
```

### 3. Run Tests

```bash
# For feature_map module
.\gradlew.bat :feature_map:connectedDebugAndroidTest

# For feature_auth module
.\gradlew.bat :feature_auth:connectedElectronicDebugAndroidTest

# For feature_jobs module
.\gradlew.bat :feature_jobs:connectedDebugAndroidTest
```

### 4. View Results

**Test Report:**

```
feature_map/build/reports/androidTests/connected/debug/index.html
```

**Coverage Report:**

```
feature_map/build/outputs/code_coverage/debugAndroidTest/connected/coverage.ec
```

---

## Troubleshooting Commands

### Check Device Properties

```bash
# API Level
adb shell getprop ro.build.version.sdk

# Android Version
adb shell getprop ro.build.version.release

# Device Model
adb shell getprop ro.product.model

# ABI
adb shell getprop ro.product.cpu.abi
```

### Clear App Data

```bash
# Uninstall test APK
adb uninstall com.enbridge.electronicservices.feature.map.test

# Uninstall main APK
adb uninstall com.enbridge.electronicservices.debug.electronic
```

### Install APK Manually

```bash
# Install main APK
adb install -r app/build/outputs/apk/debug/app-electronic-debug.apk

# Install test APK
adb install -r feature_map/build/outputs/apk/androidTest/debug/feature_map-debug-androidTest.apk
```

### Check Logcat

```bash
# View real-time logs
adb logcat | findstr "TestRunner"

# Clear logs
adb logcat -c
```

---

## Device-Specific Settings

### Xiaomi/MIUI

- **Install via USB**: Settings → Developer Options
- **MIUI Optimization**: Disable for testing
- **Mi Account**: May be required for "Install via USB"

### Samsung/One UI

- **USB Debugging**: Settings → Developer Options
- **Install Unknown Apps**: May need to enable for ADB

### Google Pixel/Stock Android

- **USB Debugging**: Settings → System → Developer Options
- Usually works without additional settings

### OnePlus/OxygenOS

- **USB Debugging**: Settings → System → Developer Options
- May require disabling battery optimization

---

## Best Practices

### For Daily Development

1. **Use Unit Tests** (`testDebugUnitTest`) for fast feedback
2. **Run instrumented tests** only when testing UI components
3. **Use emulators** for consistent test environments
4. **Keep physical device** for manual testing and performance validation

### For CI/CD

1. **Use emulators** (Google Cloud Test Lab, Firebase Test Lab, or local emulators)
2. **Avoid physical devices** in CI pipelines due to connection reliability
3. **Run instrumented tests** on every PR or nightly builds

---

## Summary: Your Situation

### What Happened

1. ✅ Device connected (Xiaomi 24094RAD4I, API 35)
2. ❌ Initial error: API mismatch (minSdk 34, but ArcGIS requires 28)
3. ✅ Fixed: Updated minSdk to 28 in all modules
4. ❌ **Current error**: `INSTALL_FAILED_USER_RESTRICTED`
    - Xiaomi MIUI security blocks ADB installations

### What to Do Next

**Option 1: Fix Xiaomi Settings** (if you need physical device testing)

1. Go to Settings → Developer Options
2. Enable **"Install via USB"**
3. Sign in with Mi Account if prompted
4. Run tests again

**Option 2: Use Emulator** (recommended)

1. Open Android Studio → AVD Manager
2. Create new device: Pixel 7, API 35
3. Start emulator
4. Run: `.\gradlew.bat :feature_map:connectedDebugAndroidTest`

**Option 3: Run Unit Tests Instead** (fastest)

```bash
# No device needed, tests ViewModels
.\gradlew.bat :feature_map:testDebugUnitTest :feature_map:jacocoTestReport
```

---

## Additional Resources

- [Android Debug Bridge (ADB) Documentation](https://developer.android.com/tools/adb)
- [Test from the command line](https://developer.android.com/studio/test/command-line)
- [Xiaomi MIUI Developer Options](https://xiaomi.eu/community/threads/how-to-enable-developer-options.65866/)

---

**Last Updated:** October 30, 2025
