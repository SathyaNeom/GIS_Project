# Logger Utility Guide

## Overview

The `Logger` utility is a centralized logging system for the Electronic Services application. It
provides build-variant-aware logging that only outputs logs in debug builds, ensuring optimal
performance and security in release builds.

## Location

```
app/src/main/java/com/enbridge/gdsgpscollection/util/Logger.kt
```

## Features

- **Build-Variant Aware**: Automatically disables logging in release builds
- **Multiple Log Levels**: Supports VERBOSE, DEBUG, INFO, WARN, ERROR, and WTF
- **Exception Logging**: Built-in support for logging exceptions with stack traces
- **Method Tracing**: Helper methods for logging method entry/exit
- **Performance Optimized**: Zero overhead in release builds
- **Consistent Interface**: Simple, easy-to-use API across the entire codebase

## How It Works

The Logger checks `BuildConfig.DEBUG` at runtime. When the app is built with a debug build type,
logging is enabled. In release builds, all logging calls become no-ops with minimal performance
impact.

```kotlin
private val isLoggingEnabled: Boolean
    get() = BuildConfig.DEBUG
```

## Usage

### Basic Logging

```kotlin
import com.enbridge.gdsgpscollection.util.Logger

class MyViewModel @Inject constructor() : ViewModel() {
    companion object {
        private const val TAG = "MyViewModel"
    }
    
    fun someFunction() {
        // Debug message
        Logger.d(TAG, "Function called")
        
        // Info message
        Logger.i(TAG, "Important information")
        
        // Warning message
        Logger.w(TAG, "Warning: Something might be wrong")
        
        // Error message
        Logger.e(TAG, "An error occurred")
    }
}
```

### Logging with Exceptions

```kotlin
try {
    // Some operation that might fail
    performNetworkCall()
} catch (e: Exception) {
    Logger.e(TAG, "Network call failed", e)
}
```

### Method Tracing

```kotlin
fun complexOperation(param1: String, param2: Int) {
    Logger.entering(TAG, "complexOperation", "param1=$param1, param2=$param2")
    
    // Your logic here
    
    Logger.exiting(TAG, "complexOperation", "success")
}
```

### Formatted Messages

```kotlin
val username = "john_doe"
val userId = 12345

Logger.i(TAG, "User logged in - Username: $username, ID: $userId")

// Or use the formatMessage helper
val message = Logger.formatMessage("User Login", "Username: $username, ID: $userId")
Logger.i(TAG, message)
```

## Log Levels

### VERBOSE (v)

Use for extremely detailed tracing information. Typically used during development for deep
debugging.

```kotlin
Logger.v(TAG, "Processing item ${index + 1} of ${totalItems}")
```

### DEBUG (d)

Use for general debugging information, state changes, and flow tracking.

```kotlin
Logger.d(TAG, "Loading user preferences")
Logger.d(TAG, "Navigation: Login -> Dashboard")
```

### INFO (i)

Use for important informational messages about app state and user actions.

```kotlin
Logger.i(TAG, "Application started - Version: ${BuildConfig.VERSION_NAME}")
Logger.i(TAG, "User logged in successfully - Username: $username")
```

### WARN (w)

Use for potentially problematic situations that aren't errors.

```kotlin
Logger.w(TAG, "API response slow - took ${duration}ms")
Logger.w(TAG, "No network connection - using cached data")
```

### ERROR (e)

Use for error conditions and exceptions.

```kotlin
Logger.e(TAG, "Failed to save data", exception)
Logger.e(TAG, "Invalid state: user not authenticated")
```

### WTF (What a Terrible Failure)

Use for conditions that should never happen.

```kotlin
Logger.wtf(TAG, "Impossible state reached", exception)
```

## Best Practices

### 1. Use Consistent Tag Names

Always use the class name as the TAG for easy filtering in Logcat.

```kotlin
companion object {
    private const val TAG = "MyClassName"
}
```

### 2. Log Important State Transitions

```kotlin
// ViewModels
Logger.i(TAG, "MyViewModel initialized")
Logger.d(TAG, "Loading data...")
Logger.i(TAG, "Data loaded successfully - Count: ${items.size}")

// Activities
Logger.i(TAG, "MainActivity onCreate")
Logger.d(TAG, "MainActivity onResume - User active")
```

### 3. Log User Actions

```kotlin
Logger.i(TAG, "User clicked: Login button")
Logger.i(TAG, "User navigated to: Settings screen")
Logger.d(TAG, "User selected: $selectedOption")
```

### 4. Log Network Operations

```kotlin
Logger.d(TAG, "Sending API request: GET /users")
Logger.i(TAG, "API response received - Status: 200")
Logger.e(TAG, "API request failed", exception)
```

### 5. Log Data Operations

```kotlin
Logger.d(TAG, "Saving user data - ID: $userId")
Logger.i(TAG, "User data saved successfully")
Logger.w(TAG, "Data not found in cache - fetching from network")
```

### 6. Don't Log Sensitive Information

❌ **BAD**:

```kotlin
Logger.d(TAG, "User password: $password")
Logger.i(TAG, "API Key: $apiKey")
```

✅ **GOOD**:

```kotlin
Logger.d(TAG, "User authenticated successfully")
Logger.i(TAG, "API Key configured (${apiKey.take(5)}...)")
```

### 7. Include Context in Error Logs

❌ **BAD**:

```kotlin
Logger.e(TAG, "Error", exception)
```

✅ **GOOD**:

```kotlin
Logger.e(TAG, "Failed to load user profile - User ID: $userId", exception)
```

## Current Implementation Coverage

The Logger utility has been integrated throughout the application:

### Application Layer

- ✅ `GdsGpsCollectionApp` - Application lifecycle and ArcGIS initialization
- ✅ `MainActivity` - Activity lifecycle events

### Navigation

- ✅ `NavGraph` - Screen navigation and user actions

### ViewModels

- ✅ `LoginViewModel` - Authentication flow
- ✅ `JobCardEntryViewModel` - Job card operations
- ✅ `ManageESViewModel` - ES data management
- ✅ `ProjectSettingsViewModel` - Project settings operations
- ✅ `CollectESViewModel` - Feature collection
- ✅ `MainMapViewModel` - Map interactions

### Repositories

- ✅ `AuthRepositoryImpl` - Authentication operations
- ✅ `ManageESRepositoryImpl` - ES data operations
- ✅ `ProjectSettingsRepositoryImpl` - Project settings operations
- ✅ `FeatureRepositoryImpl` - Feature type operations
- ✅ `JobCardEntryRepositoryImpl` - Job card data operations

### API Layer

- ✅ `MockElectronicServicesApi` - Mock API operations

### Use Cases

- ✅ `LoginUseCase` - Login business logic

## Viewing Logs

### Android Studio Logcat

1. Open Logcat window (View → Tool Windows → Logcat)
2. Filter by tag: Enter tag name in filter box
3. Filter by log level: Select level from dropdown
4. Search: Use search box for specific messages

### Common Filters

```
// View all logs from the app
package:com.enbridge.gdsgpscollection

// View specific component
tag:LoginViewModel

// View errors only
level:ERROR

// Combine filters
tag:LoginViewModel level:ERROR
```

### ADB Command Line

```bash
# View all app logs
adb logcat | grep "com.enbridge.gdsgpscollection"

# View specific tag
adb logcat | grep "LoginViewModel"

# View errors only
adb logcat *:E

# Clear logcat
adb logcat -c
```

## Testing the Logger

### Verify Debug Build Logging

1. Build the app in debug mode
2. Open Logcat
3. Launch the app
4. You should see logs like:
   ```
   I/GdsGpsCollectionApp: ========================================
   I/GdsGpsCollectionApp: Application Starting
   I/GdsGpsCollectionApp: App Variant: electronic
   I/GdsGpsCollectionApp: Build Type: DEBUG
   ```

### Verify Release Build (No Logs)

1. Build the app in release mode
2. Open Logcat
3. Launch the app
4. Application logs should NOT appear (only system logs)

## Troubleshooting

### Logs Not Appearing

1. **Check Build Type**: Ensure you're running a debug build
   ```kotlin
   // Verify in code
   Logger.d("Test", "Debug mode: ${BuildConfig.DEBUG}")
   ```

2. **Check Logcat Filters**: Remove all filters to see all logs

3. **Check Device Settings**: Ensure USB debugging is enabled

4. **Restart ADB**:
   ```bash
   adb kill-server
   adb start-server
   ```

### Too Many Logs

1. **Use Tag Filtering**: Filter by specific component tags
2. **Adjust Log Level**: Filter to show only WARN and ERROR
3. **Use More Specific Tags**: Break down large components

## Performance Considerations

### Debug Builds

- Minimal overhead (simple string concatenation)
- No impact on user experience
- Full logging enabled

### Release Builds

- Zero overhead (all calls are no-ops)
- No string concatenation
- No log output
- ProGuard/R8 may remove unused code

## Future Enhancements

Potential improvements for the Logger utility:

1. **Remote Logging**: Send ERROR logs to crash reporting service (Firebase Crashlytics)
2. **Log File Writing**: Save logs to file for debugging production issues
3. **Log Levels Per Module**: Configure different log levels for different components
4. **Structured Logging**: JSON formatted logs for better parsing
5. **Performance Metrics**: Automatic performance logging for critical operations

## Related Documentation

- [TESTING.md](TESTING.md) - Using logs for test debugging
- [KNOWN_ISSUES.md](KNOWN_ISSUES.md) - Common issues and their log signatures
- [Android Logging Best Practices](https://developer.android.com/studio/debug/am-logcat)

## Summary

The Logger utility provides a simple, efficient, and safe way to add logging to your code:

- ✅ Only logs in debug builds
- ✅ Multiple log levels for different scenarios
- ✅ Exception logging support
- ✅ Method tracing helpers
- ✅ Consistent API across the codebase
- ✅ Zero performance impact in release

Use it liberally during development to make debugging easier, knowing that it won't affect your
production builds.
