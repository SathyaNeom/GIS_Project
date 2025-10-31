# Testing Guide for Electronic Services

## Table of Contents

1. [Overview](#overview)
2. [Testing Strategy](#testing-strategy)
3. [Test Structure](#test-structure)
4. [Testing Patterns](#testing-patterns)
5. [Running Tests](#running-tests)
6. [Test Coverage](#test-coverage)
7. [Writing New Tests](#writing-new-tests)
8. [Test Reporting](#test-reporting)

## Overview

This project implements comprehensive testing across all layers using industry best practices. The
testing implementation follows the **Test Pyramid** approach with a focus on fast, reliable, and
maintainable tests.

### Test Types Implemented:

- **Unit Tests**: Testing individual components in isolation
- **Integration Tests**: Testing component interactions
- **UI Tests**: Testing user interfaces and interactions

### Testing Libraries Used:

- **JUnit 5 (Jupiter)**: Modern testing framework
- **MockK**: Kotlin-first mocking library
- **Turbine**: Flow and StateFlow testing
- **Compose UI Test**: Jetpack Compose UI testing
- **Robolectric**: Android framework mocking for unit tests
- **Hilt Testing**: Dependency injection testing support

## Testing Strategy

### Test Pyramid Distribution

```
       /\
      /  \ 10% UI Tests (Compose/Integration)
     /----\
    /      \ 20% Integration Tests  
   /--------\
  /          \ 70% Unit Tests
 /____________\
```

### Coverage Goals by Layer:

- **Domain Layer**: 95%+ (Use Cases)
- **Data Layer**: 90%+ (Repositories, Mappers)
- **Presentation Layer**: 90%+ (ViewModels)
- **UI Layer**: 60%+ (Compose Screens)

## Test Structure

### Module Organization

```
project/
├── domain/
│   ├── src/test/java/          # Unit tests for use cases
│   └── ...
├── data/
│   ├── src/test/java/          # Unit tests for repositories & mappers
│   └── ...
├── feature_auth/
│   ├── src/test/java/          # Unit tests for ViewModels
│   ├── src/androidTest/java/   # UI tests for Compose screens
│   └── ...
├── feature_jobs/
│   ├── src/test/java/          # Unit tests for ViewModels
│   ├── src/androidTest/java/   # UI tests for Compose screens
│   └── ...
└── feature_map/
    ├── src/test/java/          # Unit tests for ViewModels
    ├── src/androidTest/java/   # UI tests for Compose screens
    └── ...
```

## Testing Patterns

### 1. AAA Pattern (Arrange-Act-Assert)

All tests follow the Given-When-Then structure for clarity and readability.

```kotlin
@Test
fun `use case executes successfully with valid input`() = runTest {
    // Given (Arrange): Set up test data and mocks
    val testData = createTestData()
    coEvery { repository.getData() } returns Result.success(testData)
    
    // When (Act): Execute the code under test
    val result = useCase()
    
    // Then (Assert): Verify the expected outcome
    assertTrue(result.isSuccess)
    assertEquals(expectedValue, result.getOrNull())
    coVerify(exactly = 1) { repository.getData() }
}
```

### 2. Descriptive Test Names

Tests use backtick notation for human-readable test names:

```kotlin
@Test
fun `login with valid credentials returns success`()

@Test
fun `save job card entry with missing required fields returns error`()

@Test
fun `feature type selection navigates to edit attribute screen`()
```

### 3. Test Isolation

Each test is completely independent with its own setup and teardown:

```kotlin
@Before
fun setup() {
    // Initialize test dependencies
    mockRepository = mockk()
    viewModel = createViewModel()
}

@After
fun tearDown() {
    // Clean up resources
    clearAllMocks()
}
```

### 4. Coroutine Testing

Proper coroutine testing with TestDispatcher:

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun testSuspendFunction() = runTest {
        // Test code
        testDispatcher.scheduler.advanceUntilIdle()
        // Assertions
    }
}
```

### 5. Flow Testing with Turbine

Testing StateFlow emissions:

```kotlin
@Test
fun `state updates correctly on user action`() = runTest {
    viewModel.uiState.test {
        // Initial state
        val initialState = awaitItem()
        assertEquals(false, initialState.isLoading)
        
        // Trigger action
        viewModel.performAction()
        
        // Verify state update
        val updatedState = awaitItem()
        assertEquals(true, updatedState.success)
    }
}
```

## Running Tests

### Run All Tests

```bash
# Windows
.\gradlew.bat test
.\gradlew.bat connectedAndroidTest

# Linux/Mac
./gradlew test
./gradlew connectedAndroidTest
```

### Run Tests by Module

```bash
# Domain layer tests
.\gradlew.bat :domain:testDebugUnitTest

# Data layer tests
.\gradlew.bat :data:testDebugUnitTest

# Presentation layer tests (ViewModels)
.\gradlew.bat :feature_auth:testElectronicDebugUnitTest
.\gradlew.bat :feature_jobs:testDebugUnitTest
.\gradlew.bat :feature_map:testDebugUnitTest

# UI tests (requires emulator or device)
.\gradlew.bat :feature_auth:connectedElectronicDebugAndroidTest
.\gradlew.bat :feature_jobs:connectedDebugAndroidTest
.\gradlew.bat :feature_map:connectedDebugAndroidTest
```

### Run Specific Test Class

```bash
.\gradlew.bat :domain:testDebugUnitTest --tests "*LoginUseCaseTest"
.\gradlew.bat :feature_auth:testElectronicDebugUnitTest --tests "*LoginViewModelTest"
```

### Run Tests with Coverage Reports

Coverage reporting is configured using Jacoco. Run tests with coverage:

```bash
.\gradlew.bat testDebugUnitTest jacocoTestReport
```

Coverage reports will be generated in `build/reports/jacoco/` for each module.

## Test Coverage

### Current Test Coverage (As of Latest Build)

#### Domain Layer (50+ tests) - 95%+

- `LoginUseCase` - 7 tests
- `GetJobCardEntryUseCase` - 6 tests
- `SaveJobCardEntryUseCase` - 7 tests
- `GetFeatureTypesUseCase` - 6 tests
- `GetProjectSettingsUseCase` - 5 tests
- `SaveProjectSettingsUseCase` - 6 tests
- `GetWorkOrdersUseCase` - 7 tests
- All other use cases tested

#### Data Layer (69 tests) - 90%+

**Repositories (37 tests):**

- `AuthRepositoryImpl` - 7 tests
- `FeatureRepositoryImpl` - 9 tests
- `JobCardEntryRepositoryImpl` - 13 tests
- `ManageESRepositoryImpl` - 8 tests (existing)

**Mappers (38 tests):**

- `UserMapper` - 5 tests
- `JobCardMapper` - 10 tests
- `FeatureTypeMapper` - 13 tests
- `ProjectSettingsMapper` - 10 tests

#### Presentation Layer (55+ tests) - 90%+

- `LoginViewModel` - 14 tests
- `JobCardEntryViewModel` - 19 tests
- `CollectESViewModel` - 14 tests
- `ManageESViewModel` - 8 tests (existing)
- `ProjectSettingsViewModel` - existing tests

#### UI Layer (25+ tests) - 60%+

- `JobCardEntryScreen` - 11 tests
- `CollectESBottomSheet` - 14 tests
- `LoginScreen` - existing tests
- `ManageESBottomSheet` - existing tests

### **Total Tests Implemented: 200+ comprehensive tests!**

## Writing New Tests

### Domain Layer (Use Cases)

```kotlin
package com.enbridge.electronicservices.domain.usecase

/**
 * @author Your Name
 */

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for YourUseCase.
 *
 * Test scenarios:
 * - Success case with valid input
 * - Failure case with invalid input
 * - Error handling
 * - Edge cases
 */
class YourUseCaseTest {

    private lateinit var repository: YourRepository
    private lateinit var useCase: YourUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = YourUseCase(repository)
    }

    @Test
    fun `use case with valid input returns success`() = runTest {
        // Given
        val testData = createTestData()
        coEvery { repository.getData() } returns Result.success(testData)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(testData, result.getOrNull())
        coVerify(exactly = 1) { repository.getData() }
    }

    @Test
    fun `use case with repository failure returns error`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { repository.getData() } returns Result.failure(Exception(errorMessage))

        // When
        val result = useCase()

        // Then
        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
    }
}
```

### Data Layer (Repositories & Mappers)

```kotlin
package com.enbridge.electronicservices.data.repository

/**
 * @author Your Name
 */

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for YourRepositoryImpl.
 *
 * Test scenarios:
 * - CRUD operations
 * - DTO to domain mapping
 * - Error handling
 * - Network failures
 */
class YourRepositoryImplTest {

    private lateinit var apiService: YourApiService
    private lateinit var dao: YourDao
    private lateinit var repository: YourRepositoryImpl

    @Before
    fun setup() {
        apiService = mockk()
        dao = mockk()
        repository = YourRepositoryImpl(apiService, dao)
    }

    @Test
    fun `get data from API returns mapped domain entities`() = runTest {
        // Given
        val apiResponse = createApiResponse()
        coEvery { apiService.getData() } returns apiResponse

        // When
        val result = repository.getData()

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { apiService.getData() }
    }
}
```

### Presentation Layer (ViewModels)

```kotlin
package com.enbridge.electronicservices.feature.yourfeature

/**
 * @author Your Name
 */

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for YourViewModel.
 *
 * Test scenarios:
 * - Initial state
 * - User interactions
 * - State updates
 * - Loading/error states
 */
@OptIn(ExperimentalCoroutinesApi::class)
class YourViewModelTest {

    private lateinit var useCase: YourUseCase
    private lateinit var viewModel: YourViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        useCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() {
        // Given
        viewModel = YourViewModel(useCase)

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(null, state.error)
    }

    @Test
    fun `perform action updates state correctly`() = runTest {
        // Given
        val testData = createTestData()
        coEvery { useCase() } returns Result.success(testData)
        viewModel = YourViewModel(useCase)

        // When
        viewModel.performAction()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.success)
        coVerify(exactly = 1) { useCase() }
    }
}
```

### UI Layer (Compose Tests)

```kotlin
package com.enbridge.electronicservices.feature.yourfeature

/**
 * @author Your Name
 */

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for YourScreen.
 *
 * Test scenarios:
 * - Initial rendering
 * - User interactions
 * - Navigation
 * - Error/success states
 */
@RunWith(AndroidJUnit4::class)
class YourScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var onActionCalled = false

    @Before
    fun setup() {
        onActionCalled = false
    }

    @Test
    fun screen_initialRendering_displaysAllComponents() {
        // Given
        composeTestRule.setContent {
            YourScreen(
                onAction = { onActionCalled = true }
            )
        }

        // Then
        composeTestRule
            .onNodeWithText("Screen Title")
            .assertIsDisplayed()
    }

    @Test
    fun screen_clickButton_invokesCallback() {
        // Given
        composeTestRule.setContent {
            YourScreen(
                onAction = { onActionCalled = true }
            )
        }

        // When
        composeTestRule
            .onNodeWithText("Button")
            .performClick()

        // Then
        assert(onActionCalled)
    }
}
```

## Testing Patterns

### Testing Suspend Functions

```kotlin
@Test
fun `suspend function should return expected result`() = runTest {
    // Arrange
    coEvery { repository.suspendFunction() } returns expectedResult
    
    // Act
    val actual = useCase.invoke()
    
    // Assert
    assertEquals(expectedResult, actual)
}
```

### Testing StateFlow

```kotlin
@Test
fun `stateFlow should emit expected values`() = runTest {
    viewModel.uiState.test {
        // Initial emission
        assertEquals(initialState, awaitItem())
        
        // Trigger update
        viewModel.updateState()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Updated emission
        assertEquals(updatedState, awaitItem())
    }
}
```

### Testing Flow Collections

```kotlin
@Test
fun `flow should emit all expected items`() = runTest {
    // Arrange
    val testFlow = flowOf(1, 2, 3)
    
    // Act & Assert
    testFlow.test {
        assertEquals(1, awaitItem())
        assertEquals(2, awaitItem())
        assertEquals(3, awaitItem())
        awaitComplete()
    }
}
```

### Mocking with MockK

```kotlin
// Simple mock
val mock = mockk<YourClass>()

// Mock with return value
coEvery { mock.method() } returns expectedValue

// Mock suspend function
coEvery { mock.suspendMethod() } coAnswers { expectedValue }

// Mock with Unit return
coEvery { mock.voidMethod() } just runs

// Verify calls
coVerify(exactly = 1) { mock.method() }
coVerify(atLeast = 1) { mock.method() }
coVerify(exactly = 0) { mock.neverCalled() }
```

### Compose Testing Selectors

```kotlin
// Find by text
composeTestRule.onNodeWithText("Text")

// Find by content description
composeTestRule.onNodeWithContentDescription("Description")

// Find by test tag
composeTestRule.onNodeWithTag("TestTag")

// Find with matcher
composeTestRule.onNode(hasText("Text") and isEnabled())

// Find all matching nodes
composeTestRule.onAllNodesWithText("Text")
```

### Compose Testing Actions

```kotlin
// Assertions
.assertExists()
.assertIsDisplayed()
.assertIsEnabled()
.assertIsNotEnabled()
.assertTextEquals("Expected")
.assertContentDescriptionEquals("Description")

// Actions
.performClick()
.performTextInput("Input")
.performTextClearance()
.performScrollTo()
.performTouchInput { swipeLeft() }

// Wait operations
composeTestRule.waitForIdle()
composeTestRule.waitUntil(timeoutMillis = 1000) { condition }
```

## Best Practices

### Test Naming

Use descriptive test names that clearly state:

1. What is being tested
2. Under what conditions
3. What the expected outcome is

```kotlin
@Test
fun `login should update state to success when credentials are valid`()

@Test
fun `getData should return cached data when network is unavailable`()

@Test
fun `button should be disabled when form validation fails`()
```

### Test Structure

Follow the Arrange-Act-Assert (AAA) pattern:

```kotlin
@Test
fun testName() {
    // Arrange: Set up test data and mocks
    val testData = createTestData()
    coEvery { repository.method() } returns testData
    
    // Act: Execute the code under test
    val result = useCase.invoke()
    
    // Assert: Verify the expected outcome
    assertEquals(expectedResult, result)
}
```

### What to Test

**DO test:**

- Business logic correctness
- State management and transitions
- Error handling scenarios
- Edge cases and boundary conditions
- User interaction flows
- Data transformation accuracy

**DON'T test:**

- Framework functionality
- Third-party libraries
- Private methods directly
- Implementation details

### Test Independence

- Each test should be independent
- Tests should not rely on execution order
- Use `@Before` for setup, `@After` for cleanup
- Avoid shared mutable state between tests

### Mock Verification

Verify interactions with mocks when testing side effects:

```kotlin
@Test
fun `saveData should call repository and emit success`() = runTest {
    // Arrange
    coEvery { repository.save(any()) } returns Result.success(Unit)
    
    // Act
    viewModel.saveData(testData)
    
    // Assert
    coVerify(exactly = 1) { repository.save(testData) }
}
```

## Troubleshooting

### Common Issues

#### Unresolved References in Test Code

**Cause:** Dependencies not added to test scope

**Solution:** Add required dependencies to `build.gradle.kts`:

```kotlin
testImplementation(libs.junit)
testImplementation(libs.mockk)
testImplementation(libs.kotlinx.coroutines.test)
```

#### Hilt Dependency Injection Failures

**Cause:** Missing Hilt test setup

**Solution:**

1. Ensure `@HiltAndroidTest` annotation on test class
2. Add `HiltAndroidRule` as first rule
3. Call `hiltRule.inject()` in setup
4. Use custom `HiltTestRunner`

#### StateFlow Not Emitting in Tests

**Cause:** Dispatcher not advancing

**Solution:**

```kotlin
viewModel.action()
testDispatcher.scheduler.advanceUntilIdle()
```

#### Compose Test Not Finding Element

**Cause:** Element not rendered or wrong selector

**Solution:**

1. Use `composeTestRule.waitForIdle()`
2. Print UI tree: `composeTestRule.onRoot().printToLog("UI_TREE")`
3. Verify correct selector and test tag

## Test Reporting

The project uses **Jacoco 0.8.11** for comprehensive code coverage reporting with both HTML and XML
report generation.

### Quick Start - Generate Coverage Report

```bash
# Run tests and generate coverage report
.\gradlew.bat test jacocoTestReport

# View unified HTML report
start build\reports\jacoco\html\index.html

# Or view module-specific reports
start domain\build\reports\jacoco\test\html\index.html
start data\build\reports\jacoco\test\html\index.html
```

### Report Locations

#### Unified Coverage Report (All Modules)

- **HTML**: `build/reports/jacoco/html/index.html` - Interactive drill-down report
- **XML**: `build/reports/jacoco/jacocoTestReport.xml` - For CI/CD integration

#### Module-Specific Reports

- **Domain**: `domain/build/reports/jacoco/test/html/index.html`
- **Data**: `data/build/reports/jacoco/test/html/index.html`
- **Feature Jobs**: `feature_jobs/build/reports/jacoco/test/html/index.html`
- **Feature Map**: `feature_map/build/reports/jacoco/test/html/index.html`

### Coverage Report Commands

```bash
# Generate module-specific coverage
.\gradlew.bat :domain:testDebugUnitTest :domain:jacocoTestReport
.\gradlew.bat :data:testDebugUnitTest :data:jacocoTestReport

# Verify coverage meets thresholds (fails build if below minimum)
.\gradlew.bat jacocoCoverageVerification

# Clean and regenerate all reports
.\gradlew.bat clean test jacocoTestReport --rerun-tasks
```

### Coverage Thresholds

The project enforces minimum coverage thresholds:

| Metric               | Minimum | Current Achievement |
|----------------------|---------|---------------------|
| **Overall Coverage** | 80%     | **~90%** ✅          |
| **Branch Coverage**  | 70%     | **~85%** ✅          |

Build fails if coverage drops below these thresholds (via `jacocoCoverageVerification` task).

### Understanding Coverage Reports

#### HTML Report Features

The interactive HTML reports provide:

1. **Overall Statistics** - Total coverage percentages at project/module level
2. **Drill-Down Navigation** - Click through: Project → Module → Package → Class → Method
3. **Color Coding**:
    - 🟢 Green: Fully covered code
    - 🔴 Red: Not covered code
    - 🟡 Yellow: Partially covered branches
4. **Line-by-Line Highlighting** - See exact code coverage in source files
5. **Branch Coverage** - Tracks all conditional paths (if/else, when, etc.)
6. **Coverage Graphs** - Visual representation by package/class

#### Reading Coverage Metrics

```
Example Report for LoginUseCase.kt:
┌──────────────────────────────┐
│ Instructions:    95% (85/89) │  <- Bytecode instructions covered
│ Branches:        83% (5/6)   │  <- Conditional branches covered
│ Lines:           94% (32/34) │  <- Source lines covered
│ Methods:        100% (4/4)   │  <- Methods tested
└──────────────────────────────┘
```

### Coverage Exclusions

The following are automatically excluded from coverage reports:

**Generated Code:**

- Hilt/Dagger dependency injection code (`*_Factory`, `*_HiltModules`)
- Compose compiler generated code (`ComposableSingletons$*`)
- Android framework files (`*R.class`, `*BuildConfig.class`, `*Manifest*`)

**Test Files:**

- All test source sets excluded

**DTOs (Data Transfer Objects):**

- Simple data classes in `dto` packages

**Configuration:**
See `build.gradle.kts` files for full exclusion patterns in `jacocoTestReport` task configuration.

### CI/CD Integration

#### GitHub Actions

```yaml
name: Test & Coverage

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Run tests with coverage
        run: ./gradlew test jacocoTestReport
      
      - name: Verify coverage thresholds
        run: ./gradlew jacocoCoverageVerification
      
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          file: build/reports/jacoco/jacocoTestReport.xml
          fail_ci_if_error: true
```

#### GitLab CI

```yaml
test:
  stage: test
  script:
    - ./gradlew test jacocoTestReport
    - ./gradlew jacocoCoverageVerification
  coverage: '/Total.*?([0-9]{1,3})%/'
  artifacts:
    reports:
      junit: '**/build/test-results/test*/TEST-*.xml'
      coverage_report:
        coverage_format: cobertura
        path: build/reports/jacoco/jacocoTestReport.xml
    paths:
      - 'build/reports/jacoco/'
    expire_in: 1 week
```

#### Jenkins Pipeline

```groovy
stage('Test & Coverage') {
    steps {
        sh './gradlew test jacocoTestReport'
        sh './gradlew jacocoCoverageVerification'
    }
    post {
        always {
            junit '**/build/test-results/test*/TEST-*.xml'
            jacoco execPattern: '**/build/jacoco/*.exec',
                   classPattern: '**/build/classes',
                   sourcePattern: '**/src/main/java'
            publishHTML([
                reportDir: 'build/reports/jacoco/html',
                reportFiles: 'index.html',
                reportName: 'Jacoco Coverage Report'
            ])
        }
    }
}
```

### Troubleshooting Coverage Reports

#### Reports Not Generating

**Issue:** Jacoco reports are skipped or not generated

**Solution:**

```bash
# Force regeneration
.\gradlew.bat clean test jacocoTestReport --rerun-tasks

# Verify tests ran successfully first
.\gradlew.bat test --info
```

#### Coverage Lower Than Expected

**Possible Causes:**

1. Tests not actually executing code paths
2. Code in excluded packages
3. Generated code being counted

**Solution:**

- Review HTML report to identify uncovered code
- Check exclusion patterns in `build.gradle.kts`
- Add tests for uncovered branches

#### Build Fails on Coverage Verification

**Issue:** `jacocoCoverageVerification` fails build

**Cause:** Coverage dropped below minimum thresholds (80% overall, 70% branch)

**Solution:**

1. Review which module failed: Check build output
2. Generate coverage report for that module
3. Add tests to improve coverage
4. If threshold is too strict, adjust in `build.gradle.kts` (not recommended)

### Current Coverage Status

**As of Latest Build:**

✅ **200+ Tests Implemented**  
✅ **~90% Overall Coverage** (Exceeds 80% minimum)  
✅ **~85% Branch Coverage** (Exceeds 70% minimum)

#### Breakdown by Layer:

| Layer                             | Tests | Coverage | Status      |
|-----------------------------------|-------|----------|-------------|
| **Domain** (Use Cases)            | 50+   | ~95%     | ✅ Excellent |
| **Data** (Repositories & Mappers) | 69    | ~90%     | ✅ Excellent |
| **Presentation** (ViewModels)     | 55+   | ~90%     | ✅ Excellent |
| **UI** (Compose Screens)          | 25+   | ~60%     | ✅ Good      |

### Test Implementation History

For detailed information about test implementation, coverage achievements, and development history,
see:

**[TEST_IMPLEMENTATION_SUMMARY.md](TEST_IMPLEMENTATION_SUMMARY.md)**

This document provides:

- Complete implementation timeline
- Test counts by module
- Testing patterns used
- Build configuration details
- Historical context and decisions

## References

- [Kotlin Coroutines Testing](https://kotlinlang.org/docs/coroutines-testing.html)
- [Compose Testing Documentation](https://developer.android.com/jetpack/compose/testing)
- [MockK Documentation](https://mockk.io/)
- [Turbine - Flow Testing](https://github.com/cashapp/turbine)
- [Hilt Testing Guide](https://developer.android.com/training/dependency-injection/hilt-testing)
- [Jacoco Documentation](https://www.jacoco.org/jacoco/trunk/doc/)

---

**Note:** Maintain test quality and coverage as the codebase evolves. Regular testing ensures
application reliability and facilitates confident refactoring.
