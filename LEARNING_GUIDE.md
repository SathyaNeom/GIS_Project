# Learning Guide - Understanding the Clean Architecture Pattern

**Author:** AI Assistant  
**Date:** November 2025  
**Audience:** Developers new to Clean Architecture  
**Project:** GdsGpsCollection Android Application

---

## Table of Contents

1. [Introduction - Why This Might Feel Confusing](#1-introduction---why-this-might-feel-confusing)
2. [The Big Picture - How Everything Connects](#2-the-big-picture---how-everything-connects)
3. [Layer by Layer Walkthrough](#3-layer-by-layer-walkthrough)
4. [Following a Real Request - Login Example](#4-following-a-real-request---login-example)
5. [Where to Add Business Logic](#5-where-to-add-business-logic)
6. [Finding Implementations](#6-finding-implementations)
7. [Switching Environments (Wildfire to Project)](#7-switching-environments-wildfire-to-project)
8. [Common Tasks and Where to Do Them](#8-common-tasks-and-where-to-do-them)
9. [Tips for Navigation](#9-tips-for-navigation)
10. [Practice Exercises](#10-practice-exercises)

---

## 1. Introduction - Why This Might Feel Confusing

### Why So Many Files?

You might be thinking: "Why is there a `LoginUseCase`, `AuthRepository`, `AuthRepositoryImpl`, and
`LoginViewModel`? Why can't we just put everything in one place?"

**The Answer:** Clean Architecture prioritizes:

- **Testability** - Each piece can be tested independently
- **Maintainability** - Changes to one layer don't break others
- **Flexibility** - Easy to swap implementations (e.g., mock API → real API)
- **Team Collaboration** - Multiple developers can work on different layers simultaneously

### The Mental Model

Think of it like a restaurant:

```
┌────────��────────────────────────────────┐
│  Customer (UI Layer)                    │  ← What the user sees and interacts with
│  - Takes your order                     │
│  - Shows you the menu                   │
└────────────┬────────────────────────────┘
             │
┌────────────▼────────────────────────────┐
│  Waiter (ViewModel/Use Cases)           │  ← Coordinates everything
│  - Takes your request                   │
│  - Knows the kitchen rules              │
└────────────┬────────────────────────────┘
             │
┌────────────▼────────────────────────────┐
│  Chef (Repository)                      │  ← Does the actual work
│  - Prepares the food                    │
│  - Gets ingredients from storage        │
└────────────┬────────────────────────────┘
             │
┌────────────▼────────────────────────────┐
│  Storage/Suppliers (API/Database)       │  ← Where data comes from
│  - Raw ingredients                      │
│  - Fresh supplies                       │
└─────────────────────────────────────────┘
```

---

## 2. The Big Picture - How Everything Connects

### The Three Main Layers

```
┌──────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                         │
│                   (What the user sees)                        │
│                                                               │
│  Location: app/src/main/java/.../ui/                         │
│                                                               │
│  ┌──────────────┐         ┌──────────────┐                  │
│  │   Screen     │ ◄────── │  ViewModel   │                  │
│  │  (Compose)   │         │              │                  │
│  └──────────────┘         └──────────────┘                  │
│       Shows                    Manages                       │
│       UI                       State                         │
└───────────────────────────────┬──────────────────────────────┘
                                │ Calls
┌───────────────────────────────▼──────────────────────────────┐
│                      DOMAIN LAYER                             │
│                  (Business Logic/Rules)                       │
│                                                               │
│  Location: app/src/main/java/.../domain/                     │
│                                                               │
│  ┌──────────────┐         ┌──────────────┐                  │
│  │  Use Cases   │         │   Entities   │                  │
│  │  (Actions)   │         │   (Models)   │                  │
│  └──────────────┘         └──────────────┘                  │
│     What you                 What data                       │
│     can do                   looks like                      │
└───────────────────────────────┬──────────────────────────────┘
                                │ Calls
┌───────────────────────────────▼──────────────────────────────┐
│                       DATA LAYER                              │
│              (How to get/save data)                           │
│                                                               │
│  Location: app/src/main/java/.../data/                       │
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Repository   │  │     API      │  │   Database   │      │
│  │    (Impl)    │  │              │  │              │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│    Coordinates        Network          Local                │
│    data sources       calls            storage              │
└──────────────────────────────────────────────────────────────┘
```

### Key Rule: Dependencies Flow Inward

- **Presentation** knows about → **Domain**
- **Data** knows about → **Domain**
- **Domain** knows about → Nothing (it's independent!)

This is called the **Dependency Rule** and it's crucial.

---

## 3. Layer by Layer Walkthrough

### Presentation Layer (UI Package)

**Location:** `app/src/main/java/com/enbridge/gdsgpscollection/ui/`

**What it does:** Everything the user sees and interacts with.

**Main Components:**

```
ui/
├── auth/
│   ├── LoginScreen.kt          ← The actual UI (buttons, text fields)
│   └── LoginViewModel.kt       ← Manages state, handles user actions
├── jobs/
│   ├── JobCardEntryScreen.kt
│   └── JobCardEntryViewModel.kt
└── map/
    ├── MainMapScreen.kt
    ├── MainMapViewModel.kt
    └── delegates/               ← Helpers that reduce ViewModel complexity
        ├── LocationManagerDelegate.kt
        ├── LayerManagerDelegate.kt
        └── ...
```

**When do I work here?**

- Adding new screens
- Changing UI layout
- Adding buttons or forms
- Displaying data to users
- Handling user interactions (clicks, text input)

---

### Domain Layer (Business Logic)

**Location:** `app/src/main/java/com/enbridge/gdsgpscollection/domain/`

**What it does:** Contains all business rules and logic. No Android dependencies!

**Main Components:**

```
domain/
├── entity/                     ← Data models (what data looks like)
│   ├── User.kt
│   ├── JobCard.kt
│   └── ESDataDistance.kt
├── repository/                 ← Contracts (what operations are possible)
│   ├── AuthRepository.kt       ← Interface only
│   └── ManageESRepository.kt   ← Interface only
├── usecase/                    ← Business actions (what you can do)
│   ├── LoginUseCase.kt
│   ├── DownloadESDataUseCase.kt
│   └── SaveJobCardEntryUseCase.kt
├── facade/                     ← Groups of related use cases
│   ├── ManageESFacade.kt       ← Interface
│   └── ManageESFacadeImpl.kt   ← Implementation
└── config/                     ← Configuration
    └── FeatureServiceConfiguration.kt
```

**When do I work here?**

- Adding new business rules
- Creating new data models
- Defining what operations the app can perform
- Validation logic
- Business calculations

---

### Data Layer (Data Sources)

**Location:** `app/src/main/java/com/enbridge/gdsgpscollection/data/`

**What it does:** Actually fetches and saves data (API calls, database operations).

**Main Components:**

```
data/
├── repository/                 ← Implementations (how to get data)
│   ├── AuthRepositoryImpl.kt
│   └── ManageESRepositoryImpl.kt
├── api/                        ← Network calls
│   ├── ElectronicServicesApi.kt
│   └── MockElectronicServicesApi.kt
├── local/                      ← Database
│   ├── AppDatabase.kt
│   └── dao/
├── mapper/                     ← Convert between API format and app format
│   ├── UserMapper.kt
│   └── JobCardMapper.kt
└── dto/                        ← API data structures
    ├── LoginRequestDto.kt
    └── LoginResponseDto.kt
```

**When do I work here?**

- Connecting to new APIs
- Adding database tables
- Changing how data is fetched/saved
- Handling network errors
- Caching strategies

---

## 4. Following a Real Request - Login Example

Let's trace what happens when a user clicks the "Login" button:

### Step 1: User Clicks Login (UI Layer)

```kotlin
// File: ui/auth/LoginScreen.kt
@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // User enters username and password...
    
    Button(
        onClick = { 
            viewModel.login(username, password)  // ← STARTS HERE
        }
    ) {
        Text("Login")
    }
}
```

**What happens:** User action triggers ViewModel method

---

### Step 2: ViewModel Handles the Action (Presentation Layer)

```kotlin
// File: ui/auth/LoginViewModel.kt
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase  // ← Injected by Hilt
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }  // Show loading
            
            loginUseCase(username, password)  // ← CALL USE CASE
                .onSuccess { user ->
                    _uiState.update { it.copy(loginSuccess = true) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }
}
```

**What happens:**

- Updates UI state to show loading
- Calls the use case
- Updates UI state with result

---

### Step 3: Use Case Executes Business Logic (Domain Layer)

```kotlin
// File: domain/usecase/LoginUseCase.kt
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository  // ← Interface, not implementation!
) {
    suspend operator fun invoke(
        username: String, 
        password: String
    ): Result<User> {
        // Could add validation here:
        // if (username.isBlank()) return Result.failure(...)
        
        return authRepository.login(username, password)  // ← CALL REPOSITORY
    }
}
```

**What happens:**

- Could validate input (business rules)
- Delegates to repository
- Returns result

---

### Step 4: Repository Gets the Data (Data Layer)

```kotlin
// File: data/repository/AuthRepositoryImpl.kt
class AuthRepositoryImpl @Inject constructor(
    private val api: ElectronicServicesApi
) : AuthRepository {  // ← Implements the interface from domain layer

    override suspend fun login(
        username: String, 
        password: String
    ): Result<User> {
        return try {
            val request = LoginRequestDto(username, password)
            val response = api.login(request)  // ← ACTUAL API CALL
            val user = response.toDomain()     // Convert to domain model
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**What happens:**

- Makes actual network request
- Converts API response to domain model
- Handles errors

---

### Step 5: API Makes the Network Call (Data Layer)

```kotlin
// File: data/api/MockElectronicServicesApi.kt
class MockElectronicServicesApi : ElectronicServicesApi {
    override suspend fun login(request: LoginRequestDto): Result<LoginResponseDto> {
        // For now, returns mock data
        // In production, this would be replaced with real HTTP calls
        return Result.success(LoginResponseDto(...))
    }
}
```

**What happens:**

- In development: Returns mock data
- In production: Makes real HTTP request

---

### The Complete Flow Diagram

```
User clicks Login
       ↓
LoginScreen calls viewModel.login()
       ↓
LoginViewModel.login() 
  - Updates state to loading
  - Calls loginUseCase(username, password)
       ↓
LoginUseCase.invoke()
  - Could validate input
  - Calls authRepository.login(username, password)
       ↓
AuthRepositoryImpl.login()
  - Creates LoginRequestDto
  - Calls api.login(request)
  - Converts response to User entity
  - Returns Result<User>
       ↓
Result flows back up:
       ↓
LoginUseCase returns Result<User>
       ↓
LoginViewModel receives Result
  - onSuccess: Update state with user
  - onFailure: Update state with error
       ↓
LoginScreen observes state change
  - Shows success message or error
  - Navigates to next screen
```

---

## 5. Where to Add Business Logic

### Question: "I need to add a feature. Where does the code go?"

Use this decision tree:

```
Is it related to how data looks on screen?
├── YES → Presentation Layer (ViewModel)
└── NO ↓

Is it a business rule or validation?
├── YES → Domain Layer (Use Case)
└── NO ↓

Is it about fetching or saving data?
├── YES → Data Layer (Repository)
└── NO ↓

Is it a UI component?
└── YES → Presentation Layer (Screen/Composable)
```

### Practical Examples

#### Example 1: Validate that password is at least 8 characters

**Where:** Domain Layer (Use Case)

```kotlin
// File: domain/usecase/LoginUseCase.kt
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String): Result<User> {
        // ADD VALIDATION HERE ← Business logic
        if (password.length < 8) {
            return Result.failure(
                IllegalArgumentException("Password must be at least 8 characters")
            )
        }
        
        return authRepository.login(username, password)
    }
}
```

---

#### Example 2: Show a loading spinner during login

**Where:** Presentation Layer (ViewModel + Screen)

```kotlin
// File: ui/auth/LoginViewModel.kt
fun login(username: String, password: String) {
    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }  // ← ADD THIS
        
        loginUseCase(username, password)
            .onSuccess { user ->
                _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
            }
            .onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
    }
}

// File: ui/auth/LoginScreen.kt
@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // ADD THIS ↓
    if (uiState.isLoading) {
        CircularProgressIndicator()
    }
    
    Button(onClick = { viewModel.login(username, password) }) {
        Text("Login")
    }
}
```

---

#### Example 3: Cache user data locally after login

**Where:** Data Layer (Repository)

```kotlin
// File: data/repository/AuthRepositoryImpl.kt
class AuthRepositoryImpl @Inject constructor(
    private val api: ElectronicServicesApi,
    private val userDao: UserDao  // ← Add database dependency
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val request = LoginRequestDto(username, password)
            val response = api.login(request)
            val user = response.toDomain()
            
            // ADD THIS: Cache user in database
            userDao.insertUser(user.toEntity())
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

#### Example 4: Add a "Remember Me" checkbox

**Where:** Multiple layers (this is a feature, not just logic)

1. **UI (Presentation):**

```kotlin
// File: ui/auth/LoginScreen.kt
Checkbox(
    checked = rememberMe,
    onCheckedChange = { viewModel.onRememberMeChanged(it) }
)
```

2. **ViewModel (Presentation):**

```kotlin
// File: ui/auth/LoginViewModel.kt
fun onRememberMeChanged(checked: Boolean) {
    _uiState.update { it.copy(rememberMe = checked) }
}
```

3. **Use Case (Domain):**

```kotlin
// File: domain/usecase/LoginUseCase.kt
suspend operator fun invoke(
    username: String, 
    password: String,
    rememberMe: Boolean  // ← Add parameter
): Result<User> {
    val result = authRepository.login(username, password)
    
    if (result.isSuccess && rememberMe) {
        // Save credentials for next time
        preferencesRepository.saveUsername(username)
    }
    
    return result
}
```

4. **Repository (Data):**

```kotlin
// File: data/repository/PreferencesRepositoryImpl.kt
class PreferencesRepositoryImpl @Inject constructor(
    private val preferences: SharedPreferences
) : PreferencesRepository {
    
    override fun saveUsername(username: String) {
        preferences.edit()
            .putString("username", username)
            .apply()
    }
}
```

---

## 6. Finding Implementations

### Problem: "I see `AuthRepository` in the use case, but where's the actual code?"

Clean Architecture uses **interfaces** (contracts) and **implementations**.

### How to Find Implementations

#### Method 1: Naming Convention

- Interface: `AuthRepository`
- Implementation: `AuthRepositoryImpl`

Just add `Impl` to the end!

#### Method 2: Using Android Studio

1. **Go to Declaration:**
    - Click on `AuthRepository` in the use case
    - Press `Ctrl + B` (Windows) or `Cmd + B` (Mac)
    - This takes you to the interface

2. **Find Implementations:**
    - Click on the interface name
    - Press `Ctrl + Alt + B` (Windows) or `Cmd + Option + B` (Mac)
    - Shows all implementations

3. **Find Usages:**
    - Click on any class/method
    - Press `Alt + F7` (Windows) or `Option + F7` (Mac)
    - Shows everywhere it's used

#### Method 3: Using Dependency Injection Modules

Look in the `di/` package to see what implementation is bound:

```kotlin
// File: di/DataModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    
    @Binds
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl  // ← This is the implementation
    ): AuthRepository              // ← For this interface
}
```

### Visual Guide to Interfaces vs Implementations

```
Domain Layer defines WHAT (Interface/Contract):

┌─────────────────────────────────────┐
│ domain/repository/AuthRepository.kt │
├─────────────────────────────────────┤
│ interface AuthRepository {          │
│     suspend fun login(              │
│         username: String,           │
│         password: String            │
│     ): Result<User>                 │
│ }                                   │
└─────────────────────────────────────┘
              ↑
              │ Implements
              │
Data Layer defines HOW (Implementation):

┌──────────────────────────────────────────┐
│ data/repository/AuthRepositoryImpl.kt    │
├──────────────────────────────────────────┤
│ class AuthRepositoryImpl @Inject         │
│ constructor(                             │
│     private val api: ElectronicServicesApi│
│ ) : AuthRepository {                     │
│                                          │
│     override suspend fun login(          │
│         username: String,                │
│         password: String                 │
│     ): Result<User> {                    │
│         // ACTUAL IMPLEMENTATION HERE    │
│     }                                    │
│ }                                        │
└──────────────────────────────────────────┘
```

---

## 7. Switching Environments (Wildfire to Project)

### Understanding Environments

The app supports two environments:

1. **Wildfire** (Development)
    - Single public ArcGIS service
    - No CA Certificate required
    - Good for testing

2. **Project** (Production)
    - Multiple services (Operations + Basemap)
    - Requires project server access
    - Used in production

### How to Switch Environments

#### Option 1: Change Build Configuration (Compile-Time)

**Location:** `app/build.gradle.kts`

```kotlin
buildTypes {
    debug {
        // CHANGE THIS LINE ↓
        buildConfigField("String", "ENVIRONMENT", "\"wildfire\"")
        //                                              ↑
        // Change to "project" for production environment
    }

    release {
        // CHANGE THIS LINE ↓
        buildConfigField("String", "ENVIRONMENT", "\"wildfire\"")
        //                                              ↑
        // TODO: Change to "project" when deploying to production
    }
}
```

**Steps:**

1. Open `app/build.gradle.kts`
2. Find the `buildTypes` section
3. Change `"wildfire"` to `"project"`
4. Sync Gradle (click the elephant icon in Android Studio)
5. Rebuild the app

---

#### Option 2: Configure Service URLs

**Location:** `domain/config/FeatureServiceConfiguration.kt`

```kotlin
object FeatureServiceConfiguration {

    // TODO: Replace with actual project server URLs when deploying to production
    private const val PROJECT_OPERATIONS_URL =
        "https://your-project-server.com/arcgis/rest/services/Operations/FeatureServer"
        //  ↑↑↑ CHANGE THIS URL
        
    private const val PROJECT_BASEMAP_URL =
        "https://your-project-server.com/arcgis/rest/services/Basemap/FeatureServer"
        //  ↑↑↑ CHANGE THIS URL

    // This one is fine for development
    private const val WILDFIRE_URL =
        "https://sampleserver6.arcgisonline.com/arcgis/rest/services/Sync/WildfireSync/FeatureServer"
}
```

**Steps:**

1. Get the actual ArcGIS Feature Server URLs from your IT department
2. Replace `PROJECT_OPERATIONS_URL` with the Operations service URL
3. Replace `PROJECT_BASEMAP_URL` with the Basemap service URL
4. Make sure URLs end with `/FeatureServer`

---

### Where Two URLs Are Used (Project Mode)

When in **Project mode**, the app uses TWO services:

```kotlin
private fun createProjectEnvironment(): AppEnvironment.Project {
    val services = listOf(
        // SERVICE 1: Operations
        FeatureServiceConfig(
            id = "operations",
            name = "Operations",
            url = PROJECT_OPERATIONS_URL,  // ← First URL here
            prefix = "OP_",
            displayOnMap = false,  // Only in Table of Contents
            syncEnabled = true
        ),
        
        // SERVICE 2: Basemap
        FeatureServiceConfig(
            id = "basemap",
            name = "Basemap",
            url = PROJECT_BASEMAP_URL,  // ← Second URL here
            prefix = "BM_",
            displayOnMap = true,  // Shows on map
            syncEnabled = true
        )
    )
    
    return AppEnvironment.Project(services)
}
```

**Purpose of Each Service:**

| Service | Purpose | Displayed on Map? | In Table of Contents? |
|---------|---------|-------------------|----------------------|
| **Operations** | Business data (job cards, work orders) | ❌ No | ✅ Yes |
| **Basemap** | Geographic context (roads, boundaries) | ✅ Yes | ✅ Yes |

---

### Testing Environment Switch

After changing environment:

1. **Build and run** the app
2. **Check logs** for environment detection:
   ```
   I/FeatureServiceConfiguration: Determining environment configuration: project
   I/FeatureServiceConfiguration: Project environment: 2 services configured
   ```

3. **Download data**:
    - Tap "Manage ES" button
    - Select distance
    - Tap "Download"
    - You should see progress for multiple services

4. **Verify geodatabases** created:
    - Should create `Operations.geodatabase`
    - Should create `Basemap.geodatabase`

---

## 8. Common Tasks and Where to Do Them

### Task Matrix

| Task | Where to Do It | File Example |
|------|---------------|--------------|
| **Add a new screen** | Presentation Layer | `ui/newfeature/NewScreen.kt` |
| **Add a button** | Presentation Layer | `ui/existingfeature/ExistingScreen.kt` |
| **Validate user input** | Domain Layer | `domain/usecase/ValidateInputUseCase.kt` |
| **Make an API call** | Data Layer | `data/api/ElectronicServicesApi.kt` |
| **Save to database** | Data Layer | `data/local/dao/SomeDao.kt` |
| **Add a new data model** | Domain Layer | `domain/entity/NewModel.kt` |
| **Change button color** | Design System | `designsystem/theme/Color.kt` |
| **Add configuration** | Domain Layer | `domain/config/NewConfiguration.kt` |
| **Handle network errors** | Data Layer | `data/repository/SomeRepositoryImpl.kt` |
| **Show error message** | Presentation Layer | `ui/feature/FeatureViewModel.kt` |
| **Add a new use case** | Domain Layer | `domain/usecase/NewActionUseCase.kt` |
| **Group multiple use cases** | Domain Layer | `domain/facade/NewFeatureFacade.kt` |

---

### Adding a New Feature: Step-by-Step

Let's say you want to add a "Forgot Password" feature:

#### Step 1: Define the Domain Model (if needed)

```kotlin
// File: domain/entity/PasswordResetRequest.kt
data class PasswordResetRequest(
    val email: String,
    val timestamp: Long
)
```

#### Step 2: Define Repository Interface

```kotlin
// File: domain/repository/AuthRepository.kt
interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
    
    // ADD THIS ↓
    suspend fun requestPasswordReset(email: String): Result<Unit>
}
```

#### Step 3: Create Use Case

```kotlin
// File: domain/usecase/RequestPasswordResetUseCase.kt
class RequestPasswordResetUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        // Validation
        if (!email.contains("@")) {
            return Result.failure(IllegalArgumentException("Invalid email"))
        }
        
        return authRepository.requestPasswordReset(email)
    }
}
```

#### Step 4: Implement Repository

```kotlin
// File: data/repository/AuthRepositoryImpl.kt
class AuthRepositoryImpl @Inject constructor(
    private val api: ElectronicServicesApi
) : AuthRepository {
    
    override suspend fun login(...): Result<User> { ... }
    
    // ADD THIS ↓
    override suspend fun requestPasswordReset(email: String): Result<Unit> {
        return try {
            api.requestPasswordReset(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

#### Step 5: Update API Interface

```kotlin
// File: data/api/ElectronicServicesApi.kt
interface ElectronicServicesApi {
    suspend fun login(request: LoginRequestDto): Result<LoginResponseDto>
    
    // ADD THIS ↓
    suspend fun requestPasswordReset(email: String): Result<Unit>
}
```

#### Step 6: Create ViewModel

```kotlin
// File: ui/auth/ForgotPasswordViewModel.kt
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val requestPasswordResetUseCase: RequestPasswordResetUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()
    
    fun requestReset(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            requestPasswordResetUseCase(email)
                .onSuccess {
                    _uiState.update { it.copy(
                        isLoading = false,
                        success = true
                    ) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = error.message
                    ) }
                }
        }
    }
}

data class ForgotPasswordUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)
```

#### Step 7: Create UI Screen

```kotlin
// File: ui/auth/ForgotPasswordScreen.kt
@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    
    Column {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        
        Button(
            onClick = { viewModel.requestReset(email) },
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Reset Password")
            }
        }
        
        if (uiState.success) {
            Text("Check your email for reset instructions")
        }
        
        uiState.error?.let { error ->
            Text(error, color = Color.Red)
        }
    }
}
```

#### Step 8: Add Navigation

```kotlin
// File: navigation/NavGraph.kt
NavHost(navController = navController, startDestination = "login") {
    composable("login") { LoginScreen(...) }
    
    // ADD THIS ↓
    composable("forgot-password") { 
        ForgotPasswordScreen() 
    }
}

// In LoginScreen, add a TextButton:
TextButton(onClick = { navController.navigate("forgot-password") }) {
    Text("Forgot Password?")
}
```

---

## 9. Tips for Navigation

### Finding Your Way Around the Codebase

#### By Feature

```
ui/
├── auth/          ← Everything related to login/logout
├── jobs/          ← Everything related to job cards
└── map/           ← Everything related to map and GIS
```

#### By Layer

```
Start with what you can see:
ui/ → ViewModels → Use Cases → Repositories → API/Database
```

#### Android Studio Shortcuts

| Action | Windows | Mac |
|--------|---------|-----|
| Go to class | `Ctrl + N` | `Cmd + O` |
| Go to file | `Ctrl + Shift + N` | `Cmd + Shift + O` |
| Find in files | `Ctrl + Shift + F` | `Cmd + Shift + F` |
| Go to declaration | `Ctrl + B` | `Cmd + B` |
| Find usages | `Alt + F7` | `Option + F7` |
| Go back | `Ctrl + Alt + ←` | `Cmd + [` |
| Go forward | `Ctrl + Alt + →` | `Cmd + ]` |

### Understanding File Naming

| Pattern | Meaning | Example |
|---------|---------|---------|
| `*Screen.kt` | Composable UI screen | `LoginScreen.kt` |
| `*ViewModel.kt` | State manager for screen | `LoginViewModel.kt` |
| `*UseCase.kt` | Single business action | `LoginUseCase.kt` |
| `*Repository.kt` | Interface (contract) | `AuthRepository.kt` |
| `*RepositoryImpl.kt` | Implementation | `AuthRepositoryImpl.kt` |
| `*Dto.kt` | API data structure | `LoginRequestDto.kt` |
| `*Entity.kt` | Domain model | Database entity |
| `*Mapper.kt` | Converts between formats | `UserMapper.kt` |
| `*Delegate.kt` | Helper for ViewModel | `LocationManagerDelegate.kt` |
| `*Facade.kt` | Group of use cases | `ManageESFacade.kt` |

---

## 10. Practice Exercises

### Exercise 1: Trace a Feature

**Goal:** Understand the flow by following existing code.

**Task:** Trace the "Download ES Data" feature from UI to API.

**Steps:**

1. Open `ui/map/components/ManageESBottomSheet.kt`
2. Find the "Download" button
3. Find what method it calls in the ViewModel
4. Open `ui/map/ManageESViewModel.kt`
5. Find that method
6. See what use case it calls
7. Open that use case
8. See what repository it calls
9. Find the repository implementation
10. See what it actually does

**Answer Key:**

```
ManageESBottomSheet.kt: onDownloadClicked()
    ↓
ManageESViewModel.kt: downloadESData()
    ↓
ManageESFacade.downloadESData()
    ↓
DownloadESDataUseCase.invoke()
    ↓
ManageESRepository.downloadESData()
    ↓
ManageESRepositoryImpl.downloadESData()
    ↓
ArcGIS SDK: GeodatabaseSyncTask.generateGeodatabase()
```

---

### Exercise 2: Add a Simple Validation

**Goal:** Add business logic to an existing feature.

**Task:** Add validation to ensure username is at least 3 characters long.

**Where to add:**

```kotlin
// File: domain/usecase/LoginUseCase.kt
suspend operator fun invoke(username: String, password: String): Result<User> {
    // ADD YOUR VALIDATION HERE
    if (username.length < 3) {
        return Result.failure(
            IllegalArgumentException("Username must be at least 3 characters")
        )
    }
    
    return authRepository.login(username, password)
}
```

**Test it:**

1. Run the app
2. Try to login with username "ab"
3. Should see error message

---

### Exercise 3: Add a New UI Element

**Goal:** Modify the UI to show additional information.

**Task:** Add a "Welcome" message at the top of the main map screen.

**Where to add:**

```kotlin
// File: ui/map/MainMapScreen.kt
@Composable
fun MainMapScreen(...) {
    Scaffold(...) { paddingValues ->
        Box(...) {
            // Existing map view...
            
            // ADD THIS AT THE TOP
            Text(
                text = "Welcome to GDS GPS Collection!",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
```

---

### Exercise 4: Find Implementation

**Goal:** Practice finding where interfaces are implemented.

**Task:** Find the implementation of `ManageESRepository`.

**Method 1 - Naming:**

- Interface: `ManageESRepository.kt`
- Implementation: Look for `ManageESRepositoryImpl.kt`

**Method 2 - Android Studio:**

1. Open `domain/repository/ManageESRepository.kt`
2. Click on the interface name
3. Press `Ctrl + Alt + B` (Windows) or `Cmd + Option + B` (Mac)

**Method 3 - DI Module:**

1. Open `di/DataModule.kt` or check the data layer module
2. Look for `@Binds` or `@Provides` methods

---

### Exercise 5: Add a New Distance Option

**Goal:** Extend existing configuration.

**Task:** Add "10 Kilometers" as a download distance option.

**Where to modify:**

```kotlin
// File: domain/entity/ESDataDistance.kt
data class ESDataDistance(
    val meters: Double,
    val displayText: String
) {
    companion object {
        val HUNDRED_METERS = ESDataDistance(100.0, "100 Meters")
        val FIVE_HUNDRED_METERS = ESDataDistance(500.0, "500 Meters")
        val ONE_KILOMETER = ESDataDistance(1000.0, "1 Kilometer")
        val TWO_KILOMETERS = ESDataDistance(2000.0, "2 Kilometers")
        val FIVE_KILOMETERS = ESDataDistance(5000.0, "5 Kilometers")
        
        // ADD THIS ↓
        val TEN_KILOMETERS = ESDataDistance(10000.0, "10 Kilometers")
        
        fun allDistances(): List<ESDataDistance> {
            return listOf(
                HUNDRED_METERS,
                FIVE_HUNDRED_METERS,
                ONE_KILOMETER,
                TWO_KILOMETERS,
                FIVE_KILOMETERS,
                TEN_KILOMETERS  // ← Add here too
            )
        }
    }
}
```

**Result:** The new option will automatically appear in the distance selector!

---

## Summary

### Key Takeaways

1. **Three Main Layers:**
    - **Presentation (UI):** What users see and interact with
    - **Domain:** Business logic and rules (no Android dependencies!)
    - **Data:** How to fetch and save data

2. **Flow of Data:**
   ```
   User Action → Screen → ViewModel → Use Case → Repository → API/Database
                                                        ↓
   UI Update ← Screen ← ViewModel ← Use Case ← Repository
   ```

3. **Where to Add Code:**
    - UI changes → Presentation Layer
    - Business rules → Domain Layer
    - Data operations → Data Layer

4. **Finding Implementations:**
    - Add `Impl` to interface name
    - Use `Ctrl + Alt + B` in Android Studio
    - Check DI modules

5. **Environment Switching:**
    - Change in `build.gradle.kts`: `buildConfigField("String", "ENVIRONMENT", "wildfire")`
    - Update URLs in `FeatureServiceConfiguration.kt`

### Next Steps

1. **Read the architecture docs:** See `ARCHITECTURE.md` for comprehensive details
2. **Explore a feature:** Pick one feature and trace it through all layers
3. **Make a small change:** Try the practice exercises
4. **Ask questions:** Don't hesitate to reach out to the team

### Resources

- **Architecture Guide:** `ARCHITECTURE.md`
- **Quick Start:** `QUICK_START.md`
- **Testing Guide:** `docs/TESTING_GUIDE.md`
- **Known Issues:** `docs/KNOWN_ISSUES.md`

---

**Remember:** It's okay to feel confused at first! Clean Architecture has a learning curve, but once
you understand the pattern, you'll appreciate how organized and maintainable the code is.

Good luck with your development! 🚀
