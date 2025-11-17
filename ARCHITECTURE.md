# Architecture Guide

**Project:** GdsGpsCollection Android Application  
**Architecture Pattern:** Clean Architecture with MVVM  
**Last Updated:** November 2025  
**Version:** 1.1.0

---

## Table of Contents

- [Introduction](#introduction)
- [Architecture Overview](#architecture-overview)
- [Layer-by-Layer Explanation](#layer-by-layer-explanation)
    - [Presentation Layer](#presentation-layer)
    - [Domain Layer](#domain-layer)
    - [Data Layer](#data-layer)
- [Architectural Patterns](#architectural-patterns)
- [Component Responsibilities](#component-responsibilities)
- [Development Standards](#development-standards)
- [Scaling the Architecture](#scaling-the-architecture)
- [Best Practices](#best-practices)
- [Common Scenarios](#common-scenarios)
- [Testing Strategy](#testing-strategy)

---

## Introduction

### What is Clean Architecture?

Clean Architecture is a software design philosophy that separates code into distinct layers, each
with a specific responsibility. The primary goal is to create systems that are:

- **Independent of Frameworks**: The architecture does not depend on specific libraries or
  frameworks
- **Testable**: Business logic can be tested without UI, database, or external dependencies
- **Independent of UI**: UI can change without affecting business logic
- **Independent of Database**: Business logic is not bound to a specific database
- **Independent of External Services**: Business logic does not know about the outside world

### Why Clean Architecture?

**Benefits for This Project:**

1. **Separation of Concerns**: Each layer has a clear responsibility
2. **Testability**: 90% test coverage achieved through isolated component testing
3. **Maintainability**: Changes in one layer rarely affect others
4. **Scalability**: Easy to add new features without disrupting existing code
5. **Team Collaboration**: Multiple developers can work on different layers simultaneously

### Architecture Philosophy

This application follows the **Dependency Rule**: dependencies point inward. Outer layers depend on
inner layers, never the reverse.

```
Outer Layers → Inner Layers
UI → Presentation → Domain ← Data
```

**Domain Layer** is the core and knows nothing about outer layers.

---

## Architecture Overview

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                        │
│                     (UI, ViewModels, State)                      │
│                                                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Screens    │  │  ViewModels  │  │  Delegates   │          │
│  │  (Compose)   │  │    (MVVM)    │  │ (UI Logic)   │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│         │                  │                   │                 │
│         └──────────────────┴───────────────────┘                │
└────────────────────────────────┬────────────────────────────────┘
                                 │ Dependencies Flow Inward
┌────────────────────────────────▼────────────────────────────────┐
│                          DOMAIN LAYER                            │
│               (Business Logic, No Dependencies)                  │
│                                                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  Use Cases   │  │   Entities   │  │   Facades    │          │
│  │  (Actions)   │  │   (Models)   │  │  (Groups)    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│         │                                    │                   │
│         └────────────────────────────────────┘                  │
└────────────────────────────────┬────────────────────────────────┘
                                 │ Interface Contracts
┌────────────────────────────────▼────────────────────────────────┐
│                           DATA LAYER                             │
│              (Implementation Details, External APIs)             │
│                                                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Repositories │  │   Mappers    │  │  API/Database│          │
│  │    (Impl)    │  │  (DTO→Model) │  │   (Sources)  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
```

### Package Structure

```
com.enbridge.gdsgpscollection/
│
├── ui/                          # Presentation Layer
│   ├── auth/                    # Login feature
│   │   ├── LoginScreen.kt       # Compose UI
│   │   ├── LoginViewModel.kt    # State & Logic
│   │   └── LoginScreenState.kt  # UI State Holder
│   │
│   ├── map/                     # Map feature
│   │   ├── MainMapScreen.kt     # Compose UI
│   │   ├── MainMapViewModel.kt  # State & Logic
│   │   ├── delegates/           # UI Logic Delegates
│   │   └── components/          # Reusable UI Components
│   │
│   └── jobs/                    # Job Card feature
│       ├── JobCardEntryScreen.kt
│       └── JobCardEntryViewModel.kt
│
├── domain/                      # Domain Layer (Core Business Logic)
│   ├── entity/                  # Business Models
│   │   ├── User.kt              # Pure data classes
│   │   ├── JobCard.kt
│   │   └── ESDataDistance.kt
│   │
│   ├── repository/              # Repository Interfaces (Contracts)
│   │   ├── AuthRepository.kt
│   │   └── ManageESRepository.kt
│   │
│   ├── usecase/                 # Business Use Cases
│   │   ├── LoginUseCase.kt      # Single responsibility actions
│   │   ├── DownloadESDataUseCase.kt
│   │   └── SaveJobCardEntryUseCase.kt
│   │
│   ├── facade/                  # Use Case Groups (Facade Pattern)
│   │   ├── ManageESFacade.kt    # Interface
│   │   └── ManageESFacadeImpl.kt # Implementation
│   │
│   └── config/                  # Configuration Classes
│       ├── FeatureServiceConfiguration.kt
│       └── LocationFeatureFlags.kt
│
├── data/                        # Data Layer (Implementation)
│   ├── repository/              # Repository Implementations
│   │   ├── AuthRepositoryImpl.kt
│   │   └── ManageESRepositoryImpl.kt
│   │
│   ├── api/                     # Network API
│   │   ├── ElectronicServicesApi.kt      # Interface
│   │   └── MockElectronicServicesApi.kt  # Mock Implementation
│   │
│   ├── local/                   # Local Database (Room)
│   │   ├── AppDatabase.kt       # Database definition
│   │   ├── dao/                 # Data Access Objects
│   │   ├── entity/              # Room Entities
│   │   └── preferences/         # SharedPreferences
│   │
│   ├── mapper/                  # Data Mappers
│   │   ├── UserMapper.kt        # DTO ↔ Domain Entity
│   │   └── JobCardMapper.kt
│   │
│   └── dto/                     # Data Transfer Objects
│       ├── LoginRequestDto.kt   # API request/response models
│       └── LoginResponseDto.kt
│
├── designsystem/                # Design System (UI Components)
│   ├── components/              # Reusable UI components
│   └── theme/                   # Theme, colors, typography
│
├── di/                          # Dependency Injection (Hilt)
│   ├── DataModule.kt            # Data layer dependencies
│   ├── FacadeModule.kt          # Facade dependencies
│   └── DelegateModule.kt        # Delegate dependencies
│
├── navigation/                  # Navigation
│   └── NavGraph.kt              # Navigation graph definition
│
├── network/                     # Network Configuration
│   └── KtorClient.kt            # HTTP client setup
│
└── util/                        # Utilities
    ├── Logger.kt                # Logging utility
    ├── Constants.kt             # App constants
    ├── extensions/              # Kotlin extensions
    └── error/                   # Error handling utilities
```

---

## Layer-by-Layer Explanation

### Presentation Layer

The **Presentation Layer** handles user interaction and displays data. It consists of UI components
and ViewModels following the MVVM pattern.

#### Components

**1. Screens (Compose UI)**

Screens are composable functions that define the user interface using Jetpack Compose.

**Responsibilities:**

- Render UI based on state
- Handle user interactions (clicks, text input)
- Observe ViewModel state via StateFlow
- No business logic

**Example: LoginScreen.kt**

```kotlin
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    // Observe state from ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // UI renders based on state
    Column {
        TextField(
            value = uiState.username,
            onValueChange = viewModel::onUsernameChanged
        )
        
        Button(
            onClick = viewModel::onLoginClicked,
            enabled = uiState.isLoginButtonEnabled
        ) {
            Text("Login")
        }
        
        if (uiState.isLoading) {
            CircularProgressIndicator()
        }
    }
}
```

**Key Principles:**

- Screen is stateless (state comes from ViewModel)
- Screen does not call use cases directly
- All events flow through ViewModel

---

**2. ViewModels (Business Logic Orchestrators)**

ViewModels manage UI-related data and orchestrate business logic by calling use cases or facades.

**Responsibilities:**

- Hold UI state (using StateFlow)
- Handle UI events
- Call use cases/facades
- Transform domain models to UI state
- Survive configuration changes (screen rotation)

**Example: LoginViewModel.kt**

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    // Handle Events
    fun onUsernameChanged(username: String) {
        _uiState.update { it.copy(username = username) }
    }
    
    fun onLoginClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Call use case
            val result = loginUseCase(
                username = _uiState.value.username,
                password = _uiState.value.password
            )
            
            // Update state based on result
            result.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        loginSuccess = true
                    ) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = error.message
                    ) }
                }
            )
        }
    }
}
```

**Key Principles:**

- ViewModel depends only on domain layer (use cases/facades)
- ViewModel never references Android framework classes (except lifecycle-aware)
- State updates are immutable (using `copy()`)
- Coroutines launched in `viewModelScope` for automatic cancellation

---

**3. State Holders (Complex State Management)**

State holders group related state variables for better organization.

**Responsibilities:**

- Group related state into logical categories
- Reduce parameter proliferation
- Improve code readability

**Example: MainMapScreenState.kt**

```kotlin
/**
 * Groups all map-related state into logical categories.
 * Replaces 20+ individual state variables with 4 grouped holders.
 */
data class MainMapScreenState(
    val bottomSheetState: BottomSheetState = BottomSheetState(),
    val dialogState: DialogState = DialogState(),
    val mapInteractionState: MapInteractionState = MapInteractionState(),
    val coordinateState: CoordinateState = CoordinateState()
)

data class BottomSheetState(
    val showCollectES: Boolean = false,
    val showManageES: Boolean = false,
    val showTableOfContents: Boolean = false,
    val showProjectSettings: Boolean = false
)

data class DialogState(
    val showBasemapDialog: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val showFirstTimeGuidance: Boolean = false
)
```

**Benefits:**

- 80% reduction in state clutter
- Easier to understand state structure
- Better testability

---

**4. Delegates (Separation of Concerns)**

Delegates extract specific responsibilities from ViewModels to keep them focused.

**Responsibilities:**

- Handle one specific concern (Single Responsibility Principle)
- Encapsulate complex logic
- Improve testability

**Example: LocationManagerDelegate**

**Interface:**

```kotlin
/**
 * Manages location display and tracking on the map.
 * Extracted from MainMapViewModel to follow Single Responsibility Principle.
 */
interface LocationManagerDelegate {
    // State
    val isLocationEnabled: StateFlow<Boolean>
    val isLocationAvailable: StateFlow<Boolean>
    val currentLocation: StateFlow<Point?>
    val currentAutoPanMode: StateFlow<LocationDisplayAutoPanMode>
    
    // Actions
    fun createLocationDataSource(): LocationDataSource
    fun enableLocation()
    fun disableLocation()
    fun toggleLocationFollowing(): LocationDisplayAutoPanMode
    fun setAutoPanMode(mode: LocationDisplayAutoPanMode)
    fun updateCurrentLocation(location: Point?)
}
```

**Implementation:**

```kotlin
class LocationManagerDelegateImpl @Inject constructor(
    private val featureFlags: LocationFeatureFlags
) : LocationManagerDelegate {
    
    private val _isLocationEnabled = MutableStateFlow(false)
    override val isLocationEnabled = _isLocationEnabled.asStateFlow()
    
    override fun enableLocation() {
        _isLocationEnabled.value = true
        Logger.d(TAG, "Location display enabled")
    }
    
    // ... other implementations
}
```

**Common Delegates in This Project:**

| Delegate                      | Responsibility                                  |
|-------------------------------|-------------------------------------------------|
| `LayerManagerDelegate`        | Layer visibility, metadata, legend extraction   |
| `BasemapManagerDelegate`      | Basemap style, OSM toggle, map recreation       |
| `GeodatabaseManagerDelegate`  | Geodatabase lifecycle, loading, deletion        |
| `ExtentManagerDelegate`       | Map extent restrictions, viewpoint calculations |
| `NetworkConnectivityDelegate` | Network state monitoring                        |
| `LocationManagerDelegate`     | Location display, GPS tracking                  |

**Benefits:**

- ViewModel reduced from 800+ lines to ~400 lines
- Each delegate is independently testable
- Clear separation of concerns
- Easier to add new features

---

### Domain Layer

The **Domain Layer** is the core of the application containing business logic and rules. It has no
dependencies on outer layers.

#### Components

**1. Entities (Business Models)**

Entities are pure Kotlin data classes representing business concepts.

**Characteristics:**

- No Android dependencies
- No framework dependencies
- Pure data structures
- Can contain business logic methods

**Example: ESDataDistance.kt**

```kotlin
/**
 * Represents distance options for downloading ES data.
 * Domain model with no dependencies on external frameworks.
 */
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
        
        fun fromMeters(meters: Double): ESDataDistance? {
            return listOf(
                HUNDRED_METERS,
                FIVE_HUNDRED_METERS,
                ONE_KILOMETER,
                TWO_KILOMETERS,
                FIVE_KILOMETERS
            ).find { it.meters == meters }
        }
    }
}
```

**Example: User.kt**

```kotlin
/**
 * Domain entity representing an authenticated user.
 * Clean, framework-independent data class.
 */
data class User(
    val id: String,
    val username: String,
    val displayName: String
)
```

**Key Principles:**

- Entities are immutable (use `val`)
- Entities contain only business-relevant data
- No JSON annotations (that belongs in DTOs)
- Can contain business logic methods

---

**2. Repository Interfaces (Contracts)**

Repository interfaces define contracts for data operations without specifying implementation.

**Responsibilities:**

- Define data operation contracts
- Abstract away data sources
- Declare what operations are possible
- Return domain entities (not DTOs)

**Example: AuthRepository.kt**

```kotlin
/**
 * Contract for authentication operations.
 * Domain layer defines the interface; data layer implements it.
 */
interface AuthRepository {
    /**
     * Authenticates a user with username and password.
     *
     * @param username User's username
     * @param password User's password
     * @return Result containing User on success, error on failure
     */
    suspend fun login(username: String, password: String): Result<User>
}
```

**Why Interfaces?**

- **Dependency Inversion Principle**: Domain defines the contract, not the implementation
- **Testability**: Easy to create mock implementations
- **Flexibility**: Can swap implementations (mock API → real API) without changing domain logic

---

**3. Use Cases (Business Actions)**

Use cases represent single business actions or operations.

**Responsibilities:**

- Execute one specific business action
- Orchestrate calls to repositories
- Contain business rules
- Return domain entities

**Naming Convention:** `<Verb><Noun>UseCase`  
Examples: `LoginUseCase`, `SaveJobCardEntryUseCase`, `DownloadESDataUseCase`

**Example: LoginUseCase.kt**

```kotlin
/**
 * Use case for user authentication.
 * Follows Single Responsibility Principle: only handles login logic.
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Authenticates a user.
     *
     * @param username User's username
     * @param password User's password
     * @return Result<User> on success, error on failure
     */
    suspend operator fun invoke(
        username: String,
        password: String
    ): Result<User> {
        // Validation logic (business rules)
        if (username.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Username cannot be empty")
            )
        }
        
        if (password.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Password cannot be empty")
            )
        }
        
        // Delegate to repository
        return authRepository.login(username, password)
    }
}
```

**Key Principles:**

- One use case = one action
- Use cases depend only on repository interfaces
- Use cases are injected into ViewModels
- Use `operator fun invoke()` for clean syntax: `loginUseCase(username, password)`

---

**4. Facades (Use Case Groups)**

Facades group related use cases to simplify ViewModel dependencies.

**Problem:** ViewModels with many use case dependencies become cluttered:

```kotlin
// Before: 6 dependencies
class ManageESViewModel @Inject constructor(
    private val downloadESDataUseCase: DownloadESDataUseCase,
    private val postESDataUseCase: PostESDataUseCase,
    private val getChangedDataUseCase: GetChangedDataUseCase,
    private val deleteJobCardsUseCase: DeleteJobCardsUseCase,
    private val getSelectedDistanceUseCase: GetSelectedDistanceUseCase,
    private val saveSelectedDistanceUseCase: SaveSelectedDistanceUseCase
) : ViewModel()
```

**Solution:** Facade groups related use cases:

```kotlin
// After: 1 dependency (83% reduction)
class ManageESViewModel @Inject constructor(
    private val manageESFacade: ManageESFacade
) : ViewModel()
```

**Example: ManageESFacade.kt**

```kotlin
/**
 * Facade for managing Electronic Services data operations.
 * Groups related use cases to reduce ViewModel dependencies.
 *
 * Follows Facade pattern from SOLID principles.
 */
interface ManageESFacade {
    suspend fun downloadESData(extent: Envelope): Flow<ESDataDownloadProgress>
    suspend fun postESData(): Result<Boolean>
    suspend fun getChangedData(): Result<List<JobCard>>
    suspend fun deleteJobCards(): Result<Int>
    suspend fun getSelectedDistance(): ESDataDistance
    suspend fun saveSelectedDistance(distance: ESDataDistance)
    
    // Multi-service methods
    suspend fun downloadAllServices(extent: Envelope): Flow<MultiServiceDownloadProgress>
    suspend fun syncAllServices(): Result<Map<String, Boolean>>
    suspend fun loadAllGeodatabases(): Result<List<GeodatabaseInfo>>
}
```

**Implementation:**

```kotlin
class ManageESFacadeImpl @Inject constructor(
    private val downloadESDataUseCase: DownloadESDataUseCase,
    private val postESDataUseCase: PostESDataUseCase,
    private val getChangedDataUseCase: GetChangedDataUseCase,
    private val deleteJobCardsUseCase: DeleteJobCardsUseCase,
    private val getSelectedDistanceUseCase: GetSelectedDistanceUseCase,
    private val saveSelectedDistanceUseCase: SaveSelectedDistanceUseCase,
    private val downloadAllServicesUseCase: DownloadAllServicesUseCase,
    private val syncAllGeodatabasesUseCase: SyncAllGeodatabasesUseCase,
    private val loadAllGeodatabasesUseCase: LoadAllGeodatabasesUseCase
) : ManageESFacade {
    
    override suspend fun downloadESData(extent: Envelope) =
        downloadESDataUseCase(extent)
    
    override suspend fun postESData() =
        postESDataUseCase()
    
    // ... delegates to appropriate use cases
}
```

**Benefits:**

- Reduced ViewModel dependencies (6 → 1)
- Logical grouping of related operations
- Easier to mock for testing
- Cleaner ViewModel constructors

---

**5. Configuration Classes**

Configuration classes manage environment-specific settings.

**Example: FeatureServiceConfiguration.kt**

```kotlin
/**
 * Configuration manager for feature services based on environment.
 * 
 * Supports two environments:
 * - Wildfire: Single service for development/testing
 * - Project: Multiple services (Operations + Basemap) for production
 */
object FeatureServiceConfiguration {
    fun getCurrentEnvironment(): AppEnvironment {
        val environmentType = BuildConfig.ENVIRONMENT
        
        return when (environmentType.lowercase()) {
            "project" -> createProjectEnvironment()
            "wildfire" -> createWildfireEnvironment()
            else -> createWildfireEnvironment()
        }
    }
    
    private fun createProjectEnvironment(): AppEnvironment.Project {
        val services = listOf(
            FeatureServiceConfig(
                id = "operations",
                name = "Operations",
                url = PROJECT_OPERATIONS_URL,
                prefix = "OP_",
                displayOnMap = false
            ),
            FeatureServiceConfig(
                id = "basemap",
                name = "Basemap",
                url = PROJECT_BASEMAP_URL,
                prefix = "BM_",
                displayOnMap = true
            )
        )
        return AppEnvironment.Project(services)
    }
}
```

**Key Principles:**

- Centralized configuration management
- Environment-aware behavior
- No hardcoded values in business logic

---

### Data Layer

The **Data Layer** implements data operations defined by domain interfaces.

#### Components

**1. Repositories (Interface Implementations)**

Repositories implement domain repository interfaces and orchestrate data sources.

**Responsibilities:**

- Implement repository interfaces from domain layer
- Coordinate multiple data sources (API, database, preferences)
- Map DTOs to domain entities
- Handle data caching strategies
- Manage transactions

**Example: AuthRepositoryImpl.kt**

```kotlin
/**
 * Implementation of AuthRepository.
 * Coordinates API calls and data mapping.
 */
class AuthRepositoryImpl @Inject constructor(
    private val api: ElectronicServicesApi,
    private val userMapper: UserMapper
) : AuthRepository {
    
    override suspend fun login(
        username: String,
        password: String
    ): Result<User> {
        return try {
            // Call API
            val request = LoginRequestDto(username, password)
            val response = api.login(request)
            
            // Map DTO to domain entity
            val user = userMapper.toDomain(response)
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**Example: ManageESRepositoryImpl.kt (Complex)**

```kotlin
/**
 * Manages geodatabase operations including download, sync, and queries.
 * Coordinates ArcGIS SDK, file system, and preferences.
 */
class ManageESRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localEditDao: LocalEditDao,
    private val preferencesManager: PreferencesManager,
    private val networkMonitor: NetworkMonitor,
    private val storageUtil: StorageUtil,
    private val configuration: FeatureServiceConfiguration
) : ManageESRepository {
    
    override suspend fun downloadESData(
        extent: Envelope
    ): Flow<ESDataDownloadProgress> = channelFlow {
        // Pre-flight checks
        if (!networkMonitor.isCurrentlyConnected()) {
            send(ESDataDownloadProgress(
                0.0f,
                "No internet connection",
                true,
                "NO_INTERNET"
            ))
            return@channelFlow
        }
        
        // Check storage
        val storageCheck = storageUtil.checkStorageAvailability(estimatedSize)
        if (storageCheck is StorageCheckResult.CriticallyLow) {
            send(ESDataDownloadProgress(
                0.0f,
                "Insufficient storage",
                true,
                "STORAGE_CRITICALLY_LOW"
            ))
            return@channelFlow
        }
        
        // Download with progress
        val geodatabaseSyncTask = GeodatabaseSyncTask(FEATURE_SERVICE_URL)
        // ... download implementation
    }
}
```

**Key Principles:**

- Repositories never return DTOs (always domain entities)
- Handle all data source coordination
- Implement error handling and retries
- Manage resource cleanup (close file handles, etc.)

---

**2. Mappers (Data Transformation)**

Mappers convert between DTOs (data transfer objects) and domain entities.

**Responsibilities:**

- Convert DTO → Domain Entity
- Convert Domain Entity → DTO
- Handle data transformation logic

**Example: UserMapper.kt**

```kotlin
/**
 * Maps between UserDto (API) and User (Domain).
 * Keeps domain layer independent of API structure.
 */
class UserMapper @Inject constructor() {
    
    /**
     * Converts API DTO to domain entity.
     */
    fun toDomain(dto: LoginResponseDto): User {
        return User(
            id = dto.userId,
            username = dto.username,
            displayName = dto.displayName ?: dto.username
        )
    }
    
    /**
     * Converts domain entity to API DTO.
     */
    fun toDto(user: User): UserDto {
        return UserDto(
            userId = user.id,
            username = user.username,
            displayName = user.displayName
        )
    }
}
```

**Why Mappers?**

- **Separation**: API changes don't affect domain logic
- **Flexibility**: Can map multiple DTOs to one entity
- **Testability**: Easy to test transformation logic
- **Clean Domain**: Domain entities remain clean of JSON annotations

---

**3. DTOs (Data Transfer Objects)**

DTOs are data classes for API communication or database storage.

**Characteristics:**

- Contain serialization annotations (`@Serializable`, `@Entity`)
- Match external API structure
- Never exposed outside data layer

**Example: LoginResponseDto.kt**

```kotlin
/**
 * DTO for login API response.
 * Contains serialization annotations; never exposed to domain layer.
 */
@Serializable
data class LoginResponseDto(
    @SerialName("user_id")
    val userId: String,
    
    @SerialName("username")
    val username: String,
    
    @SerialName("display_name")
    val displayName: String?,
    
    @SerialName("access_token")
    val accessToken: String
)
```

**Example: Room Entity (Database DTO)**

```kotlin
/**
 * Room database entity for local edits.
 * Database-specific annotations kept in data layer.
 */
@Entity(tableName = "local_edits")
data class LocalEditEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "feature_id")
    val featureId: String,
    
    @ColumnInfo(name = "edit_type")
    val editType: String,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long
)
```

**Key Principles:**

- DTOs mirror external structure (API, database)
- Never used outside data layer
- Contain framework-specific annotations
- Mapped to clean domain entities before leaving data layer

---

**4. Data Sources**

Data sources provide access to external systems.

**API (Network Data Source)**

```kotlin
/**
 * API interface for Electronic Services backend.
 * Implemented by MockElectronicServicesApi for development.
 */
interface ElectronicServicesApi {
    suspend fun login(request: LoginRequestDto): LoginResponseDto
    suspend fun getJobCards(): List<JobCardDto>
    suspend fun getFeatureTypes(): List<FeatureTypeDto>
    // ... other endpoints
}
```

**Database (Local Data Source)**

```kotlin
/**
 * Room database definition.
 */
@Database(
    entities = [LocalEditEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun localEditDao(): LocalEditDao
}

/**
 * Data Access Object for local edits.
 */
@Dao
interface LocalEditDao {
    @Query("SELECT * FROM local_edits")
    suspend fun getAllEdits(): List<LocalEditEntity>
    
    @Insert
    suspend fun insertEdit(edit: LocalEditEntity)
    
    @Delete
    suspend fun deleteEdit(edit: LocalEditEntity)
}
```

**Preferences (Settings Data Source)**

```kotlin
/**
 * Manages SharedPreferences for app settings.
 */
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val preferences = context.getSharedPreferences(
        "app_preferences",
        Context.MODE_PRIVATE
    )
    
    fun getESDataDistance(): Double {
        return preferences.getFloat("es_data_distance", 1000.0f).toDouble()
    }
    
    fun saveESDataDistance(meters: Double) {
        preferences.edit()
            .putFloat("es_data_distance", meters.toFloat())
            .apply()
    }
}
```

---

## Architectural Patterns

### 1. Dependency Injection (Hilt)

**What is Dependency Injection?**

Dependency Injection is a design pattern where objects receive their dependencies from external
sources rather than creating them internally.

**Without DI:**

```kotlin
class LoginViewModel {
    // Creates dependencies internally (bad)
    private val api = MockElectronicServicesApi()
    private val mapper = UserMapper()
    private val repository = AuthRepositoryImpl(api, mapper)
    private val loginUseCase = LoginUseCase(repository)
}
```

**With DI (Hilt):**

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    // Dependencies injected by Hilt (good)
    private val loginUseCase: LoginUseCase
) : ViewModel()
```

**Benefits:**

- **Testability**: Easy to inject mock dependencies for testing
- **Flexibility**: Can swap implementations without changing code
- **Loose Coupling**: Classes don't know about concrete implementations

**How Hilt Works:**

**1. Module Definitions** (in `di/` package)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideLocalEditDao(database: AppDatabase): LocalEditDao {
        return database.localEditDao()
    }
}
```

**2. Injection Points**

```kotlin
// ViewModel injection
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel()

// Repository injection
class AuthRepositoryImpl @Inject constructor(
    private val api: ElectronicServicesApi,
    private val userMapper: UserMapper
) : AuthRepository
```

**3. Application Setup**

```kotlin
@HiltAndroidApp
class GdsGpsCollectionApp : Application()
```

---

### 2. Repository Pattern

**Purpose:** Abstract data sources behind a unified interface.

**Pattern Structure:**

```
ViewModel → Use Case → Repository Interface → Repository Implementation → Data Sources
```

**Benefits:**

- Single source of truth
- Centralized data caching logic
- Easy to swap data sources (API → Database)
- Testable business logic

**Example: Caching Strategy**

```kotlin
class JobCardRepository @Inject constructor(
    private val api: ElectronicServicesApi,
    private val database: AppDatabase,
    private val mapper: JobCardMapper
) : JobCardRepository {
    
    override suspend fun getJobCards(): Result<List<JobCard>> {
        return try {
            // Try database first (offline support)
            val cachedData = database.jobCardDao().getAllJobCards()
            if (cachedData.isNotEmpty()) {
                return Result.success(cachedData.map { mapper.toDomain(it) })
            }
            
            // Fetch from API if cache is empty
            val apiData = api.getJobCards()
            
            // Cache in database
            database.jobCardDao().insertAll(apiData.map { mapper.toEntity(it) })
            
            // Return domain entities
            Result.success(apiData.map { mapper.toDomain(it) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

### 3. MVVM (Model-View-ViewModel)

**Purpose:** Separate UI from business logic.

**Components:**

- **Model**: Domain entities and business logic
- **View**: Compose UI (screens)
- **ViewModel**: Mediates between View and Model

**Data Flow:**

```
User Action → View → ViewModel → Use Case → Repository → Data Source
                ↑                                           ↓
            State Update ← ViewModel ← Use Case ← Repository
```

**Key Principle: Unidirectional Data Flow**

- Actions flow down (User → ViewModel → Domain)
- State flows up (Domain → ViewModel → View)

---

### 4. Facade Pattern

**Purpose:** Simplify complex subsystems by providing a unified interface.

**Problem:**

```kotlin
// ViewModel with too many dependencies
class ManageESViewModel @Inject constructor(
    private val downloadUseCase: DownloadESDataUseCase,
    private val postUseCase: PostESDataUseCase,
    private val getChangedDataUseCase: GetChangedDataUseCase,
    private val deleteJobCardsUseCase: DeleteJobCardsUseCase,
    private val getDistanceUseCase: GetSelectedDistanceUseCase,
    private val saveDistanceUseCase: SaveSelectedDistanceUseCase,
    // ... 6 dependencies!
) : ViewModel()
```

**Solution: Facade**

```kotlin
// Simplified with facade
class ManageESViewModel @Inject constructor(
    private val manageESFacade: ManageESFacade
    // Only 1 dependency!
) : ViewModel() {
    
    fun onDownloadClicked() {
        viewModelScope.launch {
            manageESFacade.downloadESData(extent).collect { progress ->
                // Handle progress
            }
        }
    }
}
```

**Benefits:**

- Reduced dependencies (83% reduction: 6 → 1)
- Logical grouping of related operations
- Easier to test
- Cleaner code

---

### 5. Delegate Pattern

**Purpose:** Separate responsibilities into focused components.

**Problem:**

```kotlin
// MainMapViewModel: 800+ lines handling everything
class MainMapViewModel : ViewModel() {
    // Layer management logic (200 lines)
    // Basemap logic (150 lines)
    // Geodatabase logic (200 lines)
    // Extent logic (100 lines)
    // Network monitoring (50 lines)
    // Location logic (100 lines)
    // = 800+ lines of mixed concerns!
}
```

**Solution: Delegates**

```kotlin
// MainMapViewModel: 400 lines, focused on coordination
class MainMapViewModel @Inject constructor(
    private val layerManager: LayerManagerDelegate,
    private val basemapManager: BasemapManagerDelegate,
    private val geodatabaseManager: GeodatabaseManagerDelegate,
    private val extentManager: ExtentManagerDelegate,
    private val networkConnectivity: NetworkConnectivityDelegate,
    private val locationManager: LocationManagerDelegate
) : ViewModel() {
    
    // Delegate responsibilities
    fun toggleLayerVisibility(layerId: String, visible: Boolean) {
        layerManager.toggleVisibility(layerId, visible, featureLayers)
    }
    
    fun toggleOsmVisibility(visible: Boolean) {
        viewModelScope.launch {
            val newMap = basemapManager.createMapWithBasemapVisibility(visible, map)
            _map.value = newMap
        }
    }
}
```

**Benefits:**

- ViewModel reduced from 800+ to 400 lines (50% reduction)
- Each delegate is independently testable
- Clear separation of concerns
- Easier to add new features

---

## Component Responsibilities

### Quick Reference Table

| Component                | Layer        | Responsibilities                   | Dependencies           | Testing           |
|--------------------------|--------------|------------------------------------|------------------------|-------------------|
| **Screen**               | Presentation | Render UI, handle user input       | ViewModel              | UI tests          |
| **ViewModel**            | Presentation | Manage UI state, orchestrate logic | Use Cases/Facades      | Unit tests        |
| **Delegate**             | Presentation | Handle specific UI concern         | Other Delegates        | Unit tests        |
| **Entity**               | Domain       | Represent business concepts        | None                   | Unit tests        |
| **Use Case**             | Domain       | Execute single business action     | Repository Interface   | Unit tests        |
| **Facade**               | Domain       | Group related use cases            | Use Cases              | Unit tests        |
| **Repository Interface** | Domain       | Define data operation contracts    | None                   | N/A (interface)   |
| **Repository Impl**      | Data         | Implement data operations          | API, Database, Mappers | Unit tests        |
| **Mapper**               | Data         | Transform DTO ↔ Entity             | None                   | Unit tests        |
| **DTO**                  | Data         | API/Database data structure        | None                   | N/A (data class)  |
| **API**                  | Data         | Network communication              | None                   | Integration tests |
| **DAO**                  | Data         | Database access                    | Database               | Integration tests |

---

## Development Standards

### Naming Conventions

**Packages:**

- Lowercase, no underscores: `com.enbridge.gdsgpscollection.domain.usecase`

**Classes:**

- PascalCase: `LoginViewModel`, `UserMapper`
- Use Cases: `<Verb><Noun>UseCase` (e.g., `SaveJobCardEntryUseCase`)
- ViewModels: `<Feature>ViewModel` (e.g., `LoginViewModel`)
- Repositories: `<Entity>Repository` (e.g., `AuthRepository`)
- Mappers: `<Entity>Mapper` (e.g., `UserMapper`)
- DTOs: `<Entity>Dto` (e.g., `LoginRequestDto`)

**Files:**

- One public class per file
- File name matches class name: `LoginViewModel.kt`

**Variables:**

- camelCase: `loginUseCase`, `isLoading`
- Private StateFlows: `_uiState` (underscore prefix)
- Public StateFlows: `uiState` (no underscore)
- Constants: `UPPER_SNAKE_CASE`

### Code Organization

**File Structure:**

```kotlin
package com.enbridge.gdsgpscollection.ui.auth

// Imports (grouped: stdlib, android, third-party, project)
import androidx.lifecycle.ViewModel
import javax.inject.Inject
import com.enbridge.gdsgpscollection.domain.usecase.LoginUseCase

/**
 * KDoc comment explaining the class purpose.
 * 
 * @property loginUseCase Use case for user authentication
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    // Public properties first
    val uiState: StateFlow<LoginUiState>
    
    // Private properties
    private val _uiState = MutableStateFlow(LoginUiState())
    
    // Initialization block
    init {
        uiState = _uiState.asStateFlow()
    }
    
    // Public methods
    fun onLoginClicked() { }
    
    // Private methods
    private fun validateInput() { }
    
    // Companion object last
    companion object {
        private const val TAG = "LoginViewModel"
    }
}
```

### Documentation Standards

**KDoc for Public APIs:**

```kotlin
/**
 * Authenticates a user with username and password.
 *
 * This use case validates input and delegates to the AuthRepository
 * for actual authentication. It implements business rules for login.
 *
 * @param username User's username (must not be blank)
 * @param password User's password (must not be blank)
 * @return Result<User> containing authenticated user on success,
 *         or error with descriptive message on failure
 *
 * @throws IllegalArgumentException if username or password is blank
 *
 * Example usage:
 * ```kotlin
 * val result = loginUseCase(username = "john", password = "secret")
 * result.onSuccess { user ->
 *     println("Welcome ${user.displayName}")
 * }
 * ```

*/
suspend operator fun invoke(username: String, password: String): Result<User>

```

**Inline Comments for Complex Logic:**

```kotlin
// CRITICAL: Cancel progress monitoring before post-processing
// The generateJob.progress Flow may continue emitting values even after
// result() returns. Without cancellation, late-arriving updates can
// overwrite the 95% "processing" message, causing UI jumps.
progressMonitoringJob.cancel()
```

**Rationale Comments:**

```kotlin
// Use embedded JDK instead of system JDK to ensure consistent
// build environment across all developer machines
gradleJdk = EmbeddedJDK
```

### String Externalization

**Rule:** Never hardcode user-facing strings.

**Bad:**

```kotlin
Text("Login") // Hardcoded string
```

**Good:**

```kotlin
Text(stringResource(R.string.login_button))
```

**strings.xml:**

```xml
<string name="login_button">Login</string>
<string name="error_login_failed">Login failed. Please check your credentials.</string>
<string name="msg_welcome_user">Welcome, %1$s!</string>
```

**Benefits:**

- Localization ready
- Consistency across app
- Easy to update text
- Professional quality

### Error Handling

**Use Result Type:**

```kotlin
suspend fun login(username: String, password: String): Result<User> {
    return try {
        val user = api.login(username, password)
        Result.success(user)
    } catch (e: Exception) {
        Logger.e(TAG, "Login failed", e)
        Result.failure(e)
    }
}
```

**Handle Errors in ViewModel:**

```kotlin
result.fold(
    onSuccess = { user ->
        _uiState.update { it.copy(user = user, isLoading = false) }
    },
    onFailure = { error ->
        _uiState.update { it.copy(
            errorMessage = error.message ?: "Unknown error",
            isLoading = false
        ) }
    }
)
```

### Testing Standards

**Test Structure (AAA Pattern):**

```kotlin
@Test
fun `login with valid credentials should succeed`() = runTest {
    // Arrange
    val username = "john"
    val password = "secret"
    val expectedUser = User("1", "john", "John Doe")
    coEvery { repository.login(username, password) } returns Result.success(expectedUser)
    
    // Act
    viewModel.login(username, password)
    
    // Assert
    val state = viewModel.uiState.value
    assertTrue(state.loginSuccess)
    assertEquals(expectedUser, state.user)
}
```

**Test Coverage Goals:**

- Domain Layer: 95%+ (business logic critical)
- Data Layer: 90%+ (data operations important)
- Presentation Layer: 85%+ (UI logic testable)
- UI Layer: 60%+ (Compose UI tests expensive)

---

## Scaling the Architecture

### Adding a New Feature

**Example: Adding "Export Report" Feature**

**Step 1: Define Domain Entity**

```kotlin
// domain/entity/Report.kt
data class Report(
    val id: String,
    val title: String,
    val data: List<ReportItem>,
    val generatedAt: Long
)
```

**Step 2: Define Repository Interface**

```kotlin
// domain/repository/ReportRepository.kt
interface ReportRepository {
    suspend fun generateReport(jobCardId: String): Result<Report>
    suspend fun exportReport(report: Report, format: ExportFormat): Result<File>
}
```

**Step 3: Create Use Case**

```kotlin
// domain/usecase/ExportReportUseCase.kt
class ExportReportUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(
        jobCardId: String,
        format: ExportFormat
    ): Result<File> {
        // Business logic
        val reportResult = repository.generateReport(jobCardId)
        if (reportResult.isFailure) {
            return Result.failure(reportResult.exceptionOrNull()!!)
        }
        
        val report = reportResult.getOrNull()!!
        return repository.exportReport(report, format)
    }
}
```

**Step 4: Implement Repository**

```kotlin
// data/repository/ReportRepositoryImpl.kt
class ReportRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val jobCardDao: JobCardDao
) : ReportRepository {
    
    override suspend fun generateReport(jobCardId: String): Result<Report> {
        // Implementation
    }
    
    override suspend fun exportReport(
        report: Report,
        format: ExportFormat
    ): Result<File> {
        // Implementation
    }
}
```

**Step 5: Update ViewModel**

```kotlin
// ui/jobs/JobCardViewModel.kt
class JobCardViewModel @Inject constructor(
    // ... existing dependencies
    private val exportReportUseCase: ExportReportUseCase
) : ViewModel() {
    
    fun onExportClicked(format: ExportFormat) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            
            val result = exportReportUseCase(
                jobCardId = currentJobCardId,
                format = format
            )
            
            result.fold(
                onSuccess = { file ->
                    _uiState.update { it.copy(
                        isExporting = false,
                        exportedFile = file
                    ) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        isExporting = false,
                        errorMessage = error.message
                    ) }
                }
            )
        }
    }
}
```

**Step 6: Update UI**

```kotlin
// ui/jobs/JobCardScreen.kt
@Composable
fun JobCardScreen(
    viewModel: JobCardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // ... existing UI
    
    Button(
        onClick = { viewModel.onExportClicked(ExportFormat.PDF) },
        enabled = !uiState.isExporting
    ) {
        if (uiState.isExporting) {
            CircularProgressIndicator()
        } else {
            Text("Export Report")
        }
    }
}
```

**Step 7: Register Dependencies**

```kotlin
// di/DataModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    
    @Binds
    abstract fun bindReportRepository(
        impl: ReportRepositoryImpl
    ): ReportRepository
}
```

---

### Adding Multi-Module Support

If the app grows beyond 100K lines of code, consider multi-module architecture:

```
project/
├── app/                    # Application module
├── feature-auth/          # Login feature module
├── feature-map/           # Map feature module
├── feature-jobs/          # Job cards feature module
├── core-domain/           # Shared domain layer
├── core-data/             # Shared data layer
└── core-designsystem/     # Shared UI components
```

**Benefits:**

- Faster builds (parallel compilation)
- Clear feature boundaries
- Better encapsulation
- Easier to split work among teams

---

## Best Practices

### Do's

1. **Follow Single Responsibility Principle**
    - One class = one responsibility
    - Extract delegates when ViewModel exceeds 300 lines

2. **Use Immutable State**
   ```kotlin
   _uiState.update { it.copy(isLoading = true) }
   ```

3. **Use Coroutines for Async Operations**
   ```kotlin
   viewModelScope.launch {
       val result = useCase()
   }
   ```

4. **Externalize All Strings**
   ```kotlin
   Text(stringResource(R.string.login_button))
   ```

5. **Write Tests**
    - Unit tests for domain layer
    - Unit tests for ViewModels
    - Integration tests for repositories
    - UI tests for critical flows

6. **Use Meaningful Names**
   ```kotlin
   // Good
   fun onLoginButtonClicked()
   
   // Bad
   fun onClick()
   ```

7. **Add KDoc to Public APIs**
   ```kotlin
   /**
    * Authenticates a user.
    * @param username User's username
    * @return Result<User>
    */
   ```

### Don'ts

1. **Don't Put Business Logic in UI**
   ```kotlin
   // Bad: Business logic in Composable
   @Composable
   fun LoginScreen() {
       val username = remember { mutableStateOf("") }
       
       Button(onClick = {
           if (username.value.length < 3) {
               // Validation logic in UI - WRONG!
           }
       })
   }
   ```

2. **Don't Reference UI from Domain Layer**
   ```kotlin
   // Bad: Domain layer knows about Context
   class LoginUseCase(private val context: Context) // WRONG!
   ```

3. **Don't Create Circular Dependencies**
   ```kotlin
   // Bad: A depends on B, B depends on A
   class A(private val b: B)
   class B(private val a: A) // WRONG!
   ```

4. **Don't Hardcode Strings**
   ```kotlin
   // Bad
   Text("Login") // WRONG!
   
   // Good
   Text(stringResource(R.string.login_button))
   ```

5. **Don't Expose Mutable State**
   ```kotlin
   // Bad
   val uiState: MutableStateFlow<UiState> // WRONG!
   
   // Good
   private val _uiState = MutableStateFlow<UiState>()
   val uiState: StateFlow<UiState> = _uiState.asStateFlow()
   ```

---

## Common Scenarios

### Scenario 1: Calling an API

**Domain Layer:**

```kotlin
// Define interface
interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
}

// Create use case
class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String): Result<User> {
        return repository.login(username, password)
    }
}
```

**Data Layer:**

```kotlin
// Implement repository
class AuthRepositoryImpl @Inject constructor(
    private val api: ElectronicServicesApi,
    private val mapper: UserMapper
) : AuthRepository {
    
    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val response = api.login(LoginRequestDto(username, password))
            val user = mapper.toDomain(response)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**Presentation Layer:**

```kotlin
// Call from ViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    
    fun onLoginClicked() {
        viewModelScope.launch {
            val result = loginUseCase(username, password)
            // Handle result
        }
    }
}
```

---

### Scenario 2: Saving to Database

**Domain Layer:**

```kotlin
interface JobCardRepository {
    suspend fun saveJobCard(jobCard: JobCard): Result<Unit>
}

class SaveJobCardUseCase @Inject constructor(
    private val repository: JobCardRepository
) {
    suspend operator fun invoke(jobCard: JobCard): Result<Unit> {
        return repository.saveJobCard(jobCard)
    }
}
```

**Data Layer:**

```kotlin
class JobCardRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val mapper: JobCardMapper
) : JobCardRepository {
    
    override suspend fun saveJobCard(jobCard: JobCard): Result<Unit> {
        return try {
            val entity = mapper.toEntity(jobCard)
            database.jobCardDao().insert(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

### Scenario 3: Showing Progress

**Domain Layer:**

```kotlin
interface DownloadRepository {
    suspend fun downloadData(): Flow<DownloadProgress>
}

class DownloadDataUseCase @Inject constructor(
    private val repository: DownloadRepository
) {
    suspend operator fun invoke(): Flow<DownloadProgress> {
        return repository.downloadData()
    }
}
```

**Data Layer:**

```kotlin
class DownloadRepositoryImpl @Inject constructor() : DownloadRepository {
    
    override suspend fun downloadData(): Flow<DownloadProgress> = flow {
        emit(DownloadProgress(0.0f, "Starting..."))
        // Download logic
        emit(DownloadProgress(0.5f, "Halfway there..."))
        emit(DownloadProgress(1.0f, "Complete!"))
    }
}
```

**Presentation Layer:**

```kotlin
class DownloadViewModel @Inject constructor(
    private val downloadUseCase: DownloadDataUseCase
) : ViewModel() {
    
    fun startDownload() {
        viewModelScope.launch {
            downloadUseCase().collect { progress ->
                _uiState.update { it.copy(
                    downloadProgress = progress.progress,
                    downloadMessage = progress.message
                ) }
            }
        }
    }
}
```

---

## Testing Strategy

### Unit Testing

**Domain Layer Tests:**

```kotlin
class LoginUseCaseTest {
    
    private lateinit var useCase: LoginUseCase
    private lateinit var repository: AuthRepository
    
    @BeforeEach
    fun setup() {
        repository = mockk()
        useCase = LoginUseCase(repository)
    }
    
    @Test
    fun `login with valid credentials should succeed`() = runTest {
        // Arrange
        val expectedUser = User("1", "john", "John Doe")
        coEvery { repository.login("john", "secret") } returns Result.success(expectedUser)
        
        // Act
        val result = useCase("john", "secret")
        
        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedUser, result.getOrNull())
    }
    
    @Test
    fun `login with blank username should fail`() = runTest {
        // Act
        val result = useCase("", "secret")
        
        // Assert
        assertTrue(result.isFailure)
    }
}
```

**ViewModel Tests:**

```kotlin
class LoginViewModelTest {
    
    private lateinit var viewModel: LoginViewModel
    private lateinit var loginUseCase: LoginUseCase
    
    @BeforeEach
    fun setup() {
        loginUseCase = mockk()
        viewModel = LoginViewModel(loginUseCase)
    }
    
    @Test
    fun `login success should update state`() = runTest {
        // Arrange
        val user = User("1", "john", "John Doe")
        coEvery { loginUseCase(any(), any()) } returns Result.success(user)
        
        // Act
        viewModel.onLoginClicked()
        advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.loginSuccess)
        assertFalse(state.isLoading)
    }
}
```

### Integration Testing

**Repository Tests:**

```kotlin
@RunWith(AndroidJUnit4::class)
class AuthRepositoryImplTest {
    
    private lateinit var repository: AuthRepositoryImpl
    private lateinit var api: MockElectronicServicesApi
    
    @Before
    fun setup() {
        api = MockElectronicServicesApi()
        repository = AuthRepositoryImpl(api, UserMapper())
    }
    
    @Test
    fun `login should return user on success`() = runTest {
        // Act
        val result = repository.login("testuser", "testpass")
        
        // Assert
        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals("testuser", user?.username)
    }
}
```

### UI Testing

**Compose UI Tests:**

```kotlin
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {
    
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun loginButton_shouldBeDisabledWhenFieldsEmpty() {
        composeTestRule.setContent {
            LoginScreen(onLoginSuccess = {})
        }
        
        composeTestRule
            .onNodeWithText("Login")
            .assertIsNotEnabled()
    }
    
    @Test
    fun loginButton_shouldBeEnabledWhenFieldsFilled() {
        composeTestRule.setContent {
            LoginScreen(onLoginSuccess = {})
        }
        
        composeTestRule
            .onNodeWithText("Username")
            .performTextInput("testuser")
        
        composeTestRule
            .onNodeWithText("Password")
            .performTextInput("testpass")
        
        composeTestRule
            .onNodeWithText("Login")
            .assertIsEnabled()
    }
}
```

---

## Summary

This architecture guide provides a comprehensive understanding of how the GdsGpsCollection Android
application is structured using Clean Architecture principles with MVVM pattern.

**Key Takeaways:**

1. **Separation of Concerns**: Each layer has clear responsibilities
2. **Dependency Rule**: Dependencies flow inward (UI → Domain ← Data)
3. **Testability**: 90% test coverage through isolated components
4. **Scalability**: Easy to add features following established patterns
5. **Maintainability**: Changes isolated to specific layers

**Next Steps:**

- Review [TESTING_GUIDE.md](TESTING_GUIDE.md) for testing practices
- Check [QUICK_START.md](QUICK_START.md) for development setup
- Consult [docs/KNOWN_ISSUES.md](docs/KNOWN_ISSUES.md) for current limitations
- Reference [docs/FUTURE_UPGRADES.md](docs/FUTURE_UPGRADES.md) for planned enhancements

**Questions?**

Refer to code examples in the project or contact the development team for clarification.

---

**Last Updated:** November 2025  
**Maintained By:** Development Team  
**Review Frequency:** Quarterly or before major releases
