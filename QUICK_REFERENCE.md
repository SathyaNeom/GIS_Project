# Quick Reference Guide - Clean Architecture Cheat Sheet

**Purpose:** Quick lookup for common tasks and questions  
**Keep this handy while coding!**

---

## 🎯 Quick Decision Tree

### "Where does my code go?"

```
START HERE
    ↓
Is it WHAT the user sees?
    ↓ YES
ui/[feature]/[Feature]Screen.kt
    │
    ↓ NO
Is it HOW the user sees it (state, logic)?
    ↓ YES
ui/[feature]/[Feature]ViewModel.kt
    │
    ↓ NO
Is it a BUSINESS RULE or validation?
    ↓ YES
domain/usecase/[Action]UseCase.kt
    │
    ↓ NO
Is it a DATA MODEL?
    ↓ YES
domain/entity/[Model].kt
    │
    ↓ NO
Is it HOW to GET/SAVE data?
    ↓ YES
data/repository/[Model]RepositoryImpl.kt
    │
    ↓ NO
Is it an API CALL?
    ↓ YES
data/api/ElectronicServicesApi.kt
    │
    ↓ NO
Is it DATABASE storage?
    ↓ YES
data/local/dao/[Model]Dao.kt
```

---

## 📂 File Naming Cheat Sheet

| What You're Creating     | File Name Pattern          | Example                      | Location             |
|--------------------------|----------------------------|------------------------------|----------------------|
| **Screen**               | `[Feature]Screen.kt`       | `LoginScreen.kt`             | `ui/auth/`           |
| **ViewModel**            | `[Feature]ViewModel.kt`    | `LoginViewModel.kt`          | `ui/auth/`           |
| **Use Case**             | `[Verb][Noun]UseCase.kt`   | `LoginUseCase.kt`            | `domain/usecase/`    |
| **Entity**               | `[Model].kt`               | `User.kt`                    | `domain/entity/`     |
| **Repository Interface** | `[Model]Repository.kt`     | `AuthRepository.kt`          | `domain/repository/` |
| **Repository Impl**      | `[Model]RepositoryImpl.kt` | `AuthRepositoryImpl.kt`      | `data/repository/`   |
| **DTO**                  | `[Model]Dto.kt`            | `LoginRequestDto.kt`         | `data/dto/`          |
| **Mapper**               | `[Model]Mapper.kt`         | `UserMapper.kt`              | `data/mapper/`       |
| **DAO**                  | `[Model]Dao.kt`            | `UserDao.kt`                 | `data/local/dao/`    |
| **Delegate**             | `[Purpose]Delegate.kt`     | `LocationManagerDelegate.kt` | `ui/map/delegates/`  |
| **Facade**               | `[Feature]Facade.kt`       | `ManageESFacade.kt`          | `domain/facade/`     |

---

## 🔍 Finding Things Fast

### Android Studio Shortcuts

| What You Want            | Windows            | Mac                | What It Does                          |
|--------------------------|--------------------|--------------------|---------------------------------------|
| **Find any class**       | `Ctrl + N`         | `Cmd + O`          | Search for class by name              |
| **Find any file**        | `Ctrl + Shift + N` | `Cmd + Shift + O`  | Search for file by name               |
| **Search in files**      | `Ctrl + Shift + F` | `Cmd + Shift + F`  | Find text in all files                |
| **Go to definition**     | `Ctrl + B`         | `Cmd + B`          | Jump to where it's defined            |
| **Find implementations** | `Ctrl + Alt + B`   | `Cmd + Option + B` | Find all implementations of interface |
| **Find usages**          | `Alt + F7`         | `Option + F7`      | See everywhere it's used              |
| **Go back**              | `Ctrl + Alt + ←`   | `Cmd + [`          | Navigate backwards                    |
| **Go forward**           | `Ctrl + Alt + →`   | `Cmd + ]`          | Navigate forwards                     |
| **Navigate to file**     | `Ctrl + Shift + N` | `Cmd + Shift + O`  | Quick file open                       |

### Finding Implementations

**Problem:** I see `AuthRepository` but where's the actual code?

**Solution:**

1. Click on `AuthRepository`
2. Press `Ctrl + Alt + B` (Windows) or `Cmd + Option + B` (Mac)
3. Android Studio shows `AuthRepositoryImpl`

OR just look for `[InterfaceName]Impl` in the same package!

---

## 🏗️ Layer Reference

### Quick Layer Guide

```
┌─────────────────────────────────────────────────────────────┐
│ PRESENTATION (ui/)                                          │
│ • What: User interface and state management                 │
│ • Can depend on: Domain                                     │
│ • Contains: Screens, ViewModels, Delegates                  │
│ • Example: LoginScreen.kt, LoginViewModel.kt                │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ DOMAIN (domain/)                                            │
│ • What: Business logic and rules                            │
│ • Can depend on: NOTHING (independent!)                     │
│ • Contains: Entities, Use Cases, Repository interfaces      │
│ • Example: User.kt, LoginUseCase.kt, AuthRepository.kt      │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ DATA (data/)                                                │
│ • What: Data operations and external sources                │
│ • Can depend on: Domain                                     │
│ • Contains: Repository implementations, API, Database       │
│ • Example: AuthRepositoryImpl.kt, ElectronicServicesApi.kt  │
└─────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Common Code Patterns

### 1. Creating a New Screen

```kotlin
// File: ui/newfeature/NewFeatureScreen.kt
@Composable
fun NewFeatureScreen(
    viewModel: NewFeatureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Your UI here
    Column {
        Text(uiState.someData)
        Button(onClick = viewModel::onButtonClick) {
            Text("Click Me")
        }
    }
}
```

### 2. Creating a ViewModel

```kotlin
// File: ui/newfeature/NewFeatureViewModel.kt
@HiltViewModel
class NewFeatureViewModel @Inject constructor(
    private val someUseCase: SomeUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NewFeatureUiState())
    val uiState: StateFlow<NewFeatureUiState> = _uiState.asStateFlow()
    
    fun onButtonClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            someUseCase()
                .onSuccess { data ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        data = data
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

data class NewFeatureUiState(
    val isLoading: Boolean = false,
    val data: String? = null,
    val error: String? = null
)
```

### 3. Creating a Use Case

```kotlin
// File: domain/usecase/DoSomethingUseCase.kt
class DoSomethingUseCase @Inject constructor(
    private val repository: SomeRepository
) {
    suspend operator fun invoke(param: String): Result<Data> {
        // Validation (business rules)
        if (param.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Parameter required")
            )
        }
        
        // Delegate to repository
        return repository.doSomething(param)
    }
}
```

### 4. Creating a Repository

**Interface (Domain):**

```kotlin
// File: domain/repository/SomeRepository.kt
interface SomeRepository {
    suspend fun doSomething(param: String): Result<Data>
}
```

**Implementation (Data):**

```kotlin
// File: data/repository/SomeRepositoryImpl.kt
class SomeRepositoryImpl @Inject constructor(
    private val api: ElectronicServicesApi
) : SomeRepository {
    
    override suspend fun doSomething(param: String): Result<Data> {
        return try {
            val response = api.callEndpoint(param)
            val data = response.toDomain()
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**Bind in DI Module:**

```kotlin
// File: di/DataModule.kt (or create if not exists)
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    
    @Binds
    abstract fun bindSomeRepository(
        impl: SomeRepositoryImpl
    ): SomeRepository
}
```

---

## 🌍 Environment Switching

### Quick Switch: Wildfire ↔ Project

**File:** `app/build.gradle.kts`

```kotlin
buildTypes {
    debug {
        // CHANGE THIS LINE ↓
        buildConfigField("String", "ENVIRONMENT", "\"wildfire\"")
        //                                              ↑
        // Options: "wildfire" or "project"
    }
    
    release {
        // CHANGE THIS LINE ↓
        buildConfigField("String", "ENVIRONMENT", "\"wildfire\"")
        //                                              ↑
        // Options: "wildfire" or "project"
    }
}
```

**After changing:**

1. Click 🐘 "Sync Now"
2. Rebuild project
3. Run the app

### Configure Project URLs

**File:** `domain/config/FeatureServiceConfiguration.kt`

```kotlin
private const val PROJECT_OPERATIONS_URL =
    "https://your-project-server.com/arcgis/rest/services/Operations/FeatureServer"
    //  ↑ CHANGE THIS

private const val PROJECT_BASEMAP_URL =
    "https://your-project-server.com/arcgis/rest/services/Basemap/FeatureServer"
    //  ↑ CHANGE THIS
```

### Environment Comparison

| Feature                     | Wildfire    | Project      |
|-----------------------------|-------------|--------------|
| **Services**                | 1           | 2            |
| **CA CERTIFICATE Required** | No          | Yes          |
| **Use Case**                | Development | Production   |
| **Data**                    | Sample      | Real         |
| **Prefix**                  | `GDB_`      | `OP_`, `BM_` |

---

## 🎨 UI State Management

### State Pattern

```kotlin
// 1. Define state
data class MyUiState(
    val isLoading: Boolean = false,
    val data: List<Item> = emptyList(),
    val error: String? = null,
    val selectedItem: Item? = null
)

// 2. Create state holder in ViewModel
private val _uiState = MutableStateFlow(MyUiState())
val uiState: StateFlow<MyUiState> = _uiState.asStateFlow()

// 3. Update state (always use copy())
_uiState.update { it.copy(isLoading = true) }

// 4. Observe in UI
@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    if (uiState.isLoading) {
        CircularProgressIndicator()
    }
    
    // Use uiState.data, etc.
}
```

---

## 🧪 Testing Patterns

### Unit Test Template

```kotlin
class MyUseCaseTest {
    
    private lateinit var useCase: MyUseCase
    private lateinit var repository: MyRepository
    
    @BeforeEach
    fun setup() {
        repository = mockk()
        useCase = MyUseCase(repository)
    }
    
    @Test
    fun `operation with valid input should succeed`() = runTest {
        // Arrange
        val input = "valid"
        val expected = Data("result")
        coEvery { repository.doSomething(input) } returns Result.success(expected)
        
        // Act
        val result = useCase(input)
        
        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }
    
    @Test
    fun `operation with invalid input should fail`() = runTest {
        // Act
        val result = useCase("")
        
        // Assert
        assertTrue(result.isFailure)
    }
}
```

---

## 🔧 Dependency Injection Quick Guide

### Adding a New Dependency

**1. Add @Inject to constructor:**

```kotlin
class MyClass @Inject constructor(
    private val dependency: SomeDependency
)
```

**2. If it's an interface, bind it:**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class MyModule {
    
    @Binds
    abstract fun bindMyInterface(
        impl: MyImplementation
    ): MyInterface
}
```

**3. Hilt does the rest!**

### Scopes

| Scope              | Lifetime           | Use For                           |
|--------------------|--------------------|-----------------------------------|
| `@Singleton`       | App lifetime       | Database, API client, Preferences |
| `@ViewModelScoped` | ViewModel lifetime | ViewModel dependencies            |
| `@ActivityScoped`  | Activity lifetime  | Activity-specific dependencies    |

---

## 📋 Checklist for New Features

### Adding a Complete Feature

- [ ] **1. Define entity** (if needed)
    - [ ] Create in `domain/entity/[Model].kt`
    - [ ] Pure data class, no Android dependencies

- [ ] **2. Define repository interface**
    - [ ] Create in `domain/repository/[Model]Repository.kt`
    - [ ] Define what operations are possible

- [ ] **3. Create use case**
    - [ ] Create in `domain/usecase/[Action]UseCase.kt`
    - [ ] Add business logic/validation
    - [ ] Call repository interface

- [ ] **4. Implement repository**
    - [ ] Create in `data/repository/[Model]RepositoryImpl.kt`
    - [ ] Implement interface
    - [ ] Handle API/database calls
    - [ ] Map DTOs to entities

- [ ] **5. Update API (if needed)**
    - [ ] Add method to `data/api/ElectronicServicesApi.kt`
    - [ ] Create DTOs in `data/dto/`

- [ ] **6. Create ViewModel**
    - [ ] Create in `ui/[feature]/[Feature]ViewModel.kt`
    - [ ] Define UI state
    - [ ] Handle UI events
    - [ ] Call use cases

- [ ] **7. Create Screen**
    - [ ] Create in `ui/[feature]/[Feature]Screen.kt`
    - [ ] Build UI with Compose
    - [ ] Observe ViewModel state
    - [ ] Handle user interactions

- [ ] **8. Add navigation**
    - [ ] Update `navigation/NavGraph.kt`
    - [ ] Add route and composable

- [ ] **9. Wire dependencies**
    - [ ] Add @Binds in DI module if needed
    - [ ] Use @Inject constructors

- [ ] **10. Test!**
    - [ ] Write use case tests
    - [ ] Write ViewModel tests
    - [ ] Write repository tests
    - [ ] Test UI

---

## 🚨 Common Mistakes

### ❌ Don't Do This

```kotlin
// DON'T: Access Android Context in Domain layer
class LoginUseCase(
    private val context: Context  // ❌ WRONG!
)

// DON'T: Create dependencies manually
class LoginViewModel {
    private val repository = AuthRepositoryImpl()  // ❌ WRONG!
}

// DON'T: Put business logic in UI
@Composable
fun LoginScreen() {
    Button(onClick = {
        if (username.length < 3) {  // ❌ WRONG! Business logic in UI
            // validation
        }
    })
}

// DON'T: Return DTOs from Repository
override suspend fun getUser(): Result<UserDto> {  // ❌ WRONG!
    return api.getUser()  // Returns DTO
}
```

### ✅ Do This Instead

```kotlin
// ✅ Domain layer has no Android dependencies
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository  // Interface
)

// ✅ Let Hilt inject dependencies
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel()

// ✅ Business logic in Use Case
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String): Result<User> {
        if (username.length < 3) {  // ✅ Validation here!
            return Result.failure(Exception("Too short"))
        }
        return authRepository.login(username)
    }
}

// ✅ Return domain entities from Repository
override suspend fun getUser(): Result<User> {
    val dto = api.getUser()
    return Result.success(dto.toDomain())  // Convert to domain entity
}
```

---

## 📖 Quick Glossary

| Term           | What It Means                      | Example                         |
|----------------|------------------------------------|---------------------------------|
| **Entity**     | Business data model                | `User`, `JobCard`               |
| **DTO**        | Data Transfer Object (API format)  | `LoginRequestDto`               |
| **Repository** | Data access abstraction            | `AuthRepository`                |
| **Use Case**   | Single business action             | `LoginUseCase`                  |
| **ViewModel**  | UI state manager                   | `LoginViewModel`                |
| **Facade**     | Group of related use cases         | `ManageESFacade`                |
| **Delegate**   | Extracted ViewModel responsibility | `LocationManagerDelegate`       |
| **Mapper**     | Converts between data formats      | `UserMapper`                    |
| **DAO**        | Database Access Object             | `UserDao`                       |
| **Composable** | Jetpack Compose UI function        | `@Composable fun LoginScreen()` |

---

## 🔗 Quick Links

| Document                 | What's In It                          |
|--------------------------|---------------------------------------|
| **LEARNING_GUIDE.md**    | Complete learning guide with examples |
| **VISUAL_FLOW_GUIDE.md** | Visual diagrams and flowcharts        |
| **ARCHITECTURE.md**      | Comprehensive architecture details    |
| **QUICK_START.md**       | Setup and configuration guide         |
| **TESTING_GUIDE.md**     | How to write and run tests            |
| **docs/KNOWN_ISSUES.md** | Current limitations and workarounds   |

---

## 💡 Pro Tips

1. **Use Android Studio's structure view** (`Alt + 7`) to see class members
2. **Double-tap Shift** for "Search Everywhere"
3. **Use TODO comments** to mark things to come back to
4. **Follow the naming conventions** - makes code predictable
5. **When lost, trace backwards** from UI → ViewModel → Use Case → Repository
6. **Keep DTOs in data layer** - never expose them to domain or presentation
7. **Use StateFlow** for UI state, not LiveData
8. **Always use `copy()`** when updating state
9. **Test your use cases first** - they're the easiest to test
10. **Read the logs** - they show the flow clearly

---

## 🆘 When You're Stuck

### "I can't find where X is implemented"

1. Click on the interface name
2. Press `Ctrl + Alt + B` (Windows) or `Cmd + Option + B` (Mac)
3. Or look for `[InterfaceName]Impl`

### "I don't know which layer this belongs in"

- Is it UI? → Presentation (`ui/`)
- Is it a rule? → Domain (`domain/`)
- Is it data fetching? → Data (`data/`)

### "Build is failing with dependency errors"

1. Click 🐘 "Sync Now"
2. Clean project: `Build → Clean Project`
3. Rebuild: `Build → Rebuild Project`
4. Check if you added `@Inject` and `@Binds`

### "I added code but nothing happens"

1. Check LogCat for errors
2. Verify you called the ViewModel method
3. Check if you're observing the state: `collectAsStateWithLifecycle()`
4. Make sure you updated the state: `_uiState.update { ... }`

---

## 🎯 Remember

**The Pattern is:**

```
User Action → Screen → ViewModel → Use Case → Repository → Data Source
                 ↑                                           ↓
            State Update ← ViewModel ← Result ← Repository
```

**Key Rules:**

1. UI depends on Domain
2. Data depends on Domain
3. Domain depends on NOTHING
4. Always use interfaces in Domain
5. Implement interfaces in Data

---

**Keep this guide handy!** 📌

For detailed explanations, see `LEARNING_GUIDE.md` and `VISUAL_FLOW_GUIDE.md`.
