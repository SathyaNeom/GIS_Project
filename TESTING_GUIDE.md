# Testing Guide for GDS GPS Collection App

## Overview

This project has two types of tests:

1. **Unit Tests** - Run on your local JVM (no device needed)
2. **UI/Instrumented Tests** - Run on Android device/emulator (device required)

## Test Structure

```
app/
├── src/
│   ├── test/                          # Unit Tests (JVM)
│   │   └── java/com/enbridge/gdsgpscollection/
│   │       ├── data/                  # Data layer tests
│   │       │   ├── mapper/           # DTO to Entity mapping tests
│   │       │   └── repository/       # Repository tests with MockK
│   │       ├── domain/               # Domain layer tests
│   │       │   └── usecase/          # Use case tests
│   │       └── ui/                   # ViewModel tests
│   │
│   └── androidTest/                  # UI Tests (Android)
│       └── java/com/enbridge/gdsgpscollection/
│           ├── HiltTestRunner.kt     # Custom test runner for Hilt
│           └── ui/                   # UI component tests
│               ├── auth/             # Login screen tests
│               ├── jobs/             # Job card tests
│               └── map/              # Map screen tests
```

## Running Unit Tests

### ✅ From Android Studio (Recommended for Development)

**Run All Unit Tests:**

1. Right-click on `app/src/test` folder
2. Select "Run 'Tests in 'GDSGPSCollection.app.test''"

**Run Specific Test Class:**

1. Open the test file (e.g., `LoginViewModelTest.kt`)
2. Click the green arrow ▶️ next to the class name
3. Select "Run 'LoginViewModelTest'"

**Run Single Test Method:**

1. Click the green arrow ▶️ next to the test method
2. Select "Run 'testMethodName()'"

### 📟 From Command Line

```powershell
# Run all unit tests
.\gradlew.bat test

# Run tests for a specific variant
.\gradlew.bat :app:testElectronicDebugUnitTest

# Run a specific test class
.\gradlew.bat test --tests "com.enbridge.gdsgpscollection.ui.auth.LoginViewModelTest"

# Run a specific test method
.\gradlew.bat test --tests "com.enbridge.gdsgpscollection.ui.auth.LoginViewModelTest.login_withValidCredentials_shouldSucceed"
```

### 📊 View Test Results

After running tests via Gradle:

```
app/build/reports/tests/testElectronicDebugUnitTest/index.html
```

## Running UI/Instrumented Tests

### ⚠️ Prerequisites

**You MUST have one of the following running:**

- Android Emulator (API 28+)
- Physical Android Device connected via USB with USB Debugging enabled

### ✅ From Android Studio

**Option 1: Right-Click Method**

1. Ensure emulator/device is running and visible in device dropdown
2. Right-click on the test file (e.g., `LoginScreenTest.kt`)
3. Select "Run 'LoginScreenTest'"

**Option 2: Create Run Configuration (Recommended)**

1. Click dropdown next to Run button → "Edit Configurations..."
2. Click "+" → "Android Instrumented Tests"
3. Configure:
    - **Name**: `UI Tests - Login`
   - **Module**: `GDSGPSCollection.app.electronicDebug`
    - **Test**: Select "Class" → Choose `LoginScreenTest`
4. Click "Apply" → "OK"
5. Select your configuration from dropdown
6. Click Run ▶️

### 📟 From Command Line

```powershell
# IMPORTANT: Start emulator or connect device first!

# Run all instrumented tests
.\gradlew.bat connectedElectronicDebugAndroidTest

# Run specific test class
.\gradlew.bat connectedElectronicDebugAndroidTest --tests "com.enbridge.gdsgpscollection.ui.auth.LoginScreenTest"

# Run specific test method
.\gradlew.bat connectedElectronicDebugAndroidTest --tests "com.enbridge.gdsgpscollection.ui.auth.LoginScreenTest.loginScreen_shouldDisplayCustomAppName"
```

### View Test Results

After running instrumented tests:

```
app/build/reports/androidTests/connected/index.html
```

## Troubleshooting

### Issue: "Given component holder class does not implement GeneratedComponent"

**Cause**: Running UI test without proper Hilt setup or without device.

**Solution**:

1. Make sure a device/emulator is running
2. Sync Gradle: File → Sync Project with Gradle Files
3. Clean and rebuild: Build → Clean Project → Rebuild Project
4. Run test again

### Issue: Test runs but UI elements not found

**Cause**: Timing issues or incorrect text matching.

**Solution**:

- Add `composeTestRule.waitForIdle()` before assertions
- Use `assertIsDisplayed()` instead of `assertExists()` for visible elements
- Check if text is in string resources (use actual string, not resource ID)

### Issue: "No connected devices"

**Cause**: No emulator/device available for instrumented tests.

**Solution**:

1. Start an emulator from AVD Manager
2. OR connect a physical device with USB debugging
3. Verify device is visible in Android Studio device dropdown

### Issue: Tests are slow

**This is normal for instrumented tests:**

- Unit tests: ~5-10 seconds for full suite ✅
- Instrumented tests: ~1-2 minutes for full suite ⏱️

**To speed up:**

- Run only the tests you're working on
- Use unit tests when possible (faster)
- Use a physical device (faster than emulator)

## Test Configuration

### Hilt Test Runner

Location: `app/src/androidTest/java/com/enbridge/gdsgpscollection/HiltTestRunner.kt`

Configured in `app/build.gradle.kts`:

```kotlin
android {
    defaultConfig {
        testInstrumentationRunner = "com.enbridge.gdsgpscollection.HiltTestRunner"
    }
}
```

### Dependencies

**Unit Testing:**

- JUnit 4
- MockK (for mocking)
- Kotlin Coroutines Test
- Turbine (for Flow testing)

**Instrumented Testing:**

- AndroidX Test (JUnit, Espresso)
- Compose Testing
- Hilt Testing

## Test Results Summary

### ✅ Unit Tests: 172 tests passing

- **Data Mappers**: 38 tests
- **Repositories**: 36 tests
- **Use Cases**: 59 tests
- **ViewModels**: 68 tests

### ✅ UI Tests: 53 tests (compilation verified)

- **LoginScreenTest**: 17 tests
- **JobCardEntryScreenTest**: 10 tests
- **CollectESBottomSheetTest**: 13 tests
- **ManageESBottomSheetTest**: 13 tests

## Best Practices

### For Unit Tests:

1. **Mock dependencies** using MockK
2. **Test one thing** per test method
3. **Use descriptive names**: `methodName_condition_expectedResult`
4. **Test both success and failure** scenarios

### For UI Tests:

1. **Wait for UI to settle**: Use `composeTestRule.waitForIdle()`
2. **Use semantic properties**: Content descriptions, test tags
3. **Test user interactions**: Clicks, text input, navigation
4. **Keep tests isolated**: Each test should be independent

## Example Test

### Unit Test Example:

```kotlin
@Test
fun login_withValidCredentials_shouldSucceed() = runTest {
    // Given
    val username = "testuser"
    val password = "testpass"
    coEvery { repository.login(username, password) } returns Result.success(mockUser)
    
    // When
    viewModel.login(username, password)
    
    // Then
    assertTrue(viewModel.uiState.value.loginSuccess)
}
```

### UI Test Example:

```kotlin
@Test
fun loginScreen_loginButtonShouldBeEnabledWhenBothFieldsFilled() {
    // Given
    composeTestRule.setContent {
        LoginScreen(onLoginSuccess = {}, onForgotPasswordClick = {})
    }
    
    // When
    composeTestRule.onNodeWithText("Username").performTextInput("testuser")
    composeTestRule.onNodeWithText("Password").performTextInput("testpass")
    composeTestRule.waitForIdle()
    
    // Then
    composeTestRule.onNodeWithText("Login").assertIsEnabled()
}
```

## Continuous Integration

For CI/CD pipelines:

```yaml
# GitHub Actions / GitLab CI example
- name: Run Unit Tests
  run: ./gradlew test

- name: Run Instrumented Tests (with emulator)
  uses: reactivecircus/android-emulator-runner@v2
  with:
    api-level: 29
    script: ./gradlew connectedElectronicDebugAndroidTest
```

## Questions?

If you encounter issues not covered here:

1. Check Android Studio's "Run" window for detailed error messages
2. Look at the test report HTML files
3. Ensure all dependencies are synced: File → Sync Project with Gradle Files
4. Clean and rebuild the project

---

**Last Updated**: November 5, 2025  
**Project**: GDS GPS Collection Android App

