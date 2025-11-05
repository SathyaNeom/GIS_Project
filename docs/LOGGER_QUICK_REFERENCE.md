# Logger Quick Reference Card

## Import

```kotlin
import com.enbridge.gdsgpscollection.util.Logger
```

## Setup (in your class)

```kotlin
companion object {
    private const val TAG = "YourClassName"
}
```

## Basic Usage

| Level | Method | When to Use | Example |
|-------|--------|-------------|---------|
| **VERBOSE** | `Logger.v(TAG, msg)` | Detailed tracing | `Logger.v(TAG, "Item $i of $total")` |
| **DEBUG** | `Logger.d(TAG, msg)` | General debugging | `Logger.d(TAG, "Loading data...")` |
| **INFO** | `Logger.i(TAG, msg)` | Important events | `Logger.i(TAG, "User logged in")` |
| **WARN** | `Logger.w(TAG, msg)` | Warnings | `Logger.w(TAG, "No network - using cache")` |
| **ERROR** | `Logger.e(TAG, msg, ex)` | Errors | `Logger.e(TAG, "Save failed", exception)` |

## Common Patterns

### ViewModel Lifecycle

```kotlin
init {
    Logger.i(TAG, "MyViewModel initialized")
}

override fun onCleared() {
    super.onCleared()
    Logger.d(TAG, "MyViewModel cleared")
}
```

### Function Entry/Exit

```kotlin
fun myFunction(param: String) {
    Logger.entering(TAG, "myFunction", "param=$param")
    // Your code
    Logger.exiting(TAG, "myFunction", "success")
}
```

### Error Handling

```kotlin
try {
    performOperation()
} catch (e: Exception) {
    Logger.e(TAG, "Operation failed - Context info", e)
}
```

### User Actions

```kotlin
Logger.i(TAG, "User clicked: Submit button")
Logger.d(TAG, "User selected: $selectedOption")
```

### Navigation

```kotlin
Logger.i(TAG, "Navigation: Login -> Dashboard")
Logger.d(TAG, "Screen: Settings - User on settings screen")
```

### Data Operations

```kotlin
Logger.d(TAG, "Saving user data - ID: $userId")
Logger.i(TAG, "Data saved successfully - Count: ${items.size}")
```

## Tips

✅ **DO**

- Use class name as TAG
- Include context in messages
- Log user actions
- Log state changes
- Log errors with exceptions

❌ **DON'T**

- Log sensitive data (passwords, API keys)
- Log in tight loops (use VERBOSE sparingly)
- Use generic error messages
- Forget to include context

## Logcat Filters

```
tag:YourClassName          # Filter by tag
level:ERROR                # Show errors only
package:com.enbridge       # Show app logs
tag:YourClassName level:INFO  # Combine filters
```

## Remember

🔒 **Logs only appear in DEBUG builds**  
🚀 **Zero overhead in RELEASE builds**  
📱 **Safe for production**

For detailed guide: See `docs/LOGGER_UTILITY_GUIDE.md`
