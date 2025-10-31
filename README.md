# Electronic Services - Android Application

[![Coverage](https://img.shields.io/badge/coverage-90%25-brightgreen)](docs/TESTING.md)
[![Tests](https://img.shields.io/badge/tests-200%2B-success)](docs/TESTING.md)
[![Android](https://img.shields.io/badge/android-14%2B-blue)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/kotlin-2.1.0-purple)](https://kotlinlang.org)

An enterprise-grade Android application built with Jetpack Compose and Clean Architecture principles
for managing electronic services operations and GIS functionality.

## Overview

Electronic Services is a modular Android application designed for field operations management, work
order tracking, and geographic information system integration. The application implements modern
Android development practices with a comprehensive design system, ensuring consistency,
accessibility, and maintainability.

### Key Highlights

- **Clean Architecture** with MVVM pattern
- **Multi-Module** structure for scalability
- **Jetpack Compose** with Material 3
- **5 Build Variants** for different business units
- **ArcGIS Integration** for GIS functionality
- **200+ Tests** with 90% code coverage
- **Comprehensive Design System** (36+ components)
- **Release Ready** with ProGuard configuration

## Features

### Authentication

- Secure login functionality
- State management with loading and error handling
- Credential validation
- Variant-specific branding with custom app logo

### Map Functionality (ArcGIS Integration)

- Interactive ArcGIS map with multiple basemap styles
- Toronto-centered default viewpoint
- Navigation drawer with variant-specific menu items
- Floating action buttons for quick actions
- Touch gesture support for pan, zoom, and rotate
- Real-time coordinate and scale display
- Feature identification and measurement tools

### Work Order Management

- Job card listing with status indicators
- Comprehensive job card entry with 70+ fields across 3 tabs
    - **Job Card Tab**: Service details, pipe specifications, test information
    - **Measurements Tab**: Distance measurements, positioning data
    - **Meter Info Tab**: Meter and regulator details
- Empty state handling
- Pull-to-refresh functionality
- Detailed work order information display

### ES Management

- Collect ES (Electronic Services) data with feature types
- Manage ES edits with distance-based filtering
- Project settings configuration
- Sync operations

### Design System

- **36+ reusable UI components**
- Consistent spacing system (4dp grid)
- Light and dark theme support
- WCAG AA accessibility compliance
- Interactive component catalog application
- Comprehensive string resource management

## Architecture

The application follows **Clean Architecture** with **MVVM pattern**, organized into distinct layers
with clear separation of concerns.

### Architectural Layers

```
┌─────────────────────────────────────┐
│        Presentation Layer           │
│   (ViewModels, UI State, Screens)  │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│          Domain Layer               │
│   (Use Cases, Entities, Repository  │
│         Interfaces)                 │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│           Data Layer                │
│  (Repositories, API, Database, DTOs)│
└─────────────────────────────────────┘
```

### Module Structure

The project is organized into multiple modules for better separation of concerns and build
performance:

#### Core Modules

- **`app`** - Main application module with dependency injection and navigation
- **`core`** - Shared utilities, network configuration, and common code
- **`design-system`** - Comprehensive UI component library with Material 3 theming

#### Business Logic

- **`domain`** - Business logic layer with entities, use cases, and repository interfaces
- **`data`** - Data layer with repository implementations, Room database, and API clients

#### Features

- **`feature_auth`** - Authentication and login features
- **`feature_map`** - Map screen with ArcGIS Maps SDK integration
- **`feature_jobs`** - Job and work order management

#### Utilities

- **`app-catalog`** - Standalone design system showcase application

### Design Patterns

- **MVVM** - Model-View-ViewModel for presentation layer
- **Repository Pattern** - Abstract data sources from business logic
- **Use Case Pattern** - Single-responsibility business operations
- **Dependency Injection** - Hilt for dependency management
- **Unidirectional Data Flow** - StateFlow for reactive state updates
- **Clean Architecture** - Clear separation between layers

### Key Architectural Decisions

1. **Multi-Module Structure**: Enables parallel builds, clear boundaries, and code reusability
2. **Clean Architecture**: Ensures testability, maintainability, and independence from frameworks
3. **Jetpack Compose**: Modern, declarative UI with better performance
4. **Kotlin Coroutines & Flow**: Structured concurrency and reactive streams
5. **Hilt Dependency Injection**: Compile-time safety and automatic injection

## Technology Stack

### Core Technologies

- **Language**: Kotlin 2.1.0
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: Clean Architecture with MVVM
- **Min SDK**: API 34 (Android 14)
- **Target SDK**: API 36

### Key Libraries

#### UI & Navigation

- **Jetpack Compose BOM**: 2024.09.00
- **Material 3**: Latest components and theming
- **Navigation Compose**: 2.8.5

#### Dependency Injection

- **Hilt**: 2.52

#### Networking

- **Ktor Client**: 3.0.3
- **Kotlin Serialization**: 1.7.3

#### Database

- **Room**: 2.6.1 with KSP

#### Maps

- **ArcGIS Maps SDK**: 200.6.0

#### Asynchronous

- **Kotlin Coroutines**: 1.9.0
- **Kotlin Flow**: Reactive streams

#### Testing

- **JUnit 5 Jupiter**: 5.11.4
- **MockK**: 1.13.14
- **Turbine**: 1.2.0 (Flow testing)
- **Compose UI Test**: Latest
- **Jacoco**: 0.8.11 (Coverage reporting)

#### Build System

- **Gradle**: 8.12.3
- **Android Gradle Plugin**: 8.7.3
- **Kotlin DSL**: For type-safe build scripts

## Testing

### Test Coverage Status

The project has **enterprise-grade test coverage** with **200+ comprehensive tests**:

| Layer            | Tests    | Coverage | Status           |
|------------------|----------|----------|------------------|
| **Domain**       | 50+      | 95%+     | Excellent        |
| **Data**         | 69       | 90%+     | Excellent        |
| **Presentation** | 55+      | 90%+     | Excellent        |
| **UI**           | 25+      | 60%+     | Good             |
| **Overall**      | **200+** | **~90%** | Production Ready |

### Testing Infrastructure

- **Unit Tests**: JUnit 5, MockK, Turbine
- **Integration Tests**: Room database, repository tests
- **UI Tests**: Compose UI testing framework
- **Coverage Reports**: Jacoco with HTML & XML output
- **Threshold Enforcement**: 80% minimum coverage
- **CI/CD Ready**: XML reports for automation

### Running Tests

```bash
# Run all tests
.\gradlew.bat test

# Run with coverage report
.\gradlew.bat test jacocoTestReport

# View coverage report
start build\reports\jacoco\html\index.html

# Verify coverage thresholds
.\gradlew.bat jacocoCoverageVerification
```

### Testing Documentation

For comprehensive testing information, see:

- **[docs/TESTING.md](docs/TESTING.md)** - Complete testing guide
- **[docs/TEST_IMPLEMENTATION_SUMMARY.md](docs/TEST_IMPLEMENTATION_SUMMARY.md)** - Implementation
  details

## Getting Started

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1) or later
- **JDK**: 17 or higher
- **Android SDK**: API Level 34 or higher
- **ArcGIS API Key**: Required for map functionality

### Quick Setup

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd ElectronicServices
   ```

2. **Configure ArcGIS API key** in `local.properties`:
   ```properties
   ARCGIS_API_KEY=your_api_key_here
   ```

3. **Sync Gradle dependencies**:
   ```bash
   .\gradlew.bat --refresh-dependencies
   ```

4. **Build the project**:
   ```bash
   .\gradlew.bat build
   ```

5. **Run on device or emulator** (Android 14+):
   ```bash
   .\gradlew.bat installElectronicDebug
   ```

For detailed setup instructions, see **[QUICK_START.md](QUICK_START.md)**.

## Build Variants

The application supports **5 product flavors**, each tailored for different business units:

### Available Variants

1. **Electronic Services** (`electronic`)
    - Full-featured variant with all menu options
    - Includes ES Job Card Entry
    - Default variant for electronic services operations

2. **Maintenance** (`maintenance`)
    - Maintenance-focused operations
    - ES Job Card Entry hidden

3. **Construction** (`construction`)
    - Construction project management
    - ES Job Card Entry hidden

4. **Resurvey** (`resurvey`)
    - Survey and mapping operations
    - ES Job Card Entry hidden

5. **Gas Storage** (`gasStorage`)
    - Gas storage facility management
    - ES Job Card Entry hidden

### Variant Features

Each variant includes:

- Unique app name and launcher icon
- Custom Material Design icon theme
- Conditional feature visibility
- Separate application ID suffix for side-by-side installation
- Variant-specific branding

### Building Variants

```bash
# Build specific variant (debug)
.\gradlew.bat assembleElectronicDebug
.\gradlew.bat assembleMaintenanceDebug

# Build specific variant (release)
.\gradlew.bat assembleElectronicRelease

# Build all variants
.\gradlew.bat assemble
```

## Project Structure

```
ElectronicServices/
├── README.md                         # This file
├── QUICK_START.md                    # Setup guide
│
├── docs/                             # Documentation
│   ├── TESTING.md                    # Complete testing guide
│   ├── FUTURE_UPGRADES.md            # Roadmap and planned features
│   ├── KNOWN_ISSUES.md               # Current limitations
│   └── TEST_IMPLEMENTATION_SUMMARY.md # Testing implementation details
│
├── app/                              # Main application
│   ├── navigation/                   # Navigation graph
│   ├── di/                           # Dependency injection modules
│   ├── src/
│   │   ├── main/                     # Shared source set
│   │   ├── electronic/               # Electronic variant resources
│   │   ├── maintenance/              # Maintenance variant resources
│   │   ├── construction/             # Construction variant resources
│   │   ├── resurvey/                 # Resurvey variant resources
│   │   └── gasStorage/               # Gas Storage variant resources
│   └── build.gradle.kts
│
├── core/                             # Core utilities
│   ├── network/                      # HTTP client configuration
│   ├── util/                         # Utility functions
│   └── build.gradle.kts
│
├── design-system/                    # UI component library
│   ├── components/                   # 36+ reusable components
│   ├── theme/                        # Theme, colors, typography
│   ├── res/values/                   # String resources
│   └── build.gradle.kts
│
├── domain/                           # Business logic
│   ├── entity/                       # Domain models
│   ├── repository/                   # Repository interfaces
│   ├── usecase/                      # Business use cases
│   ├── src/test/                     # 50+ unit tests
│   └── build.gradle.kts
│
├── data/                             # Data layer
│   ├── api/                          # API services (Ktor)
│   ├── local/                        # Room database
│   ├── repository/                   # Repository implementations
│   ├── mapper/                       # DTO to entity mappers
│   ├── src/test/                     # 69 unit tests
│   └── build.gradle.kts
│
├── feature_auth/                     # Authentication
│   ├── login/                        # Login screen
│   ├── res/values/                   # Auth-specific strings
│   ├── src/test/                     # Unit tests
│   ├── src/androidTest/              # UI tests
│   └── build.gradle.kts
│
├── feature_jobs/                     # Work orders
│   ├── entry/                        # Job card entry screen
│   ├── list/                         # Job list screen
│   ├── res/values/                   # 200+ string resources
│   ├── src/test/                     # Unit tests
│   ├── src/androidTest/              # UI tests
│   └── build.gradle.kts
│
├── feature_map/                      # Map and GIS
│   ├── map/                          # Main map screen
│   ├── components/                   # Bottom sheets, dialogs
│   ├── res/values/                   # Map-specific strings
│   ├── src/test/                     # Unit tests
│   ├── src/androidTest/              # UI tests
│   └── build.gradle.kts
│
├── app-catalog/                      # Component showcase
│   ├── src/main/                     # Catalog application
│   └── build.gradle.kts
│
├── proguard-rules.pro                # ProGuard configuration
├── keystore.properties.template      # Keystore config template
├── build.gradle.kts                  # Root build file
├── settings.gradle.kts               # Module configuration
└── gradle/                           # Gradle wrapper
    └── libs.versions.toml            # Centralized dependencies
```

## Testing & Quality

### Comprehensive Test Suite

The project implements **enterprise-grade testing** across all architectural layers:

#### Domain Layer Tests (50+ tests, 95% coverage)

- `LoginUseCase` - 7 tests
- `GetJobCardEntryUseCase` - 6 tests
- `SaveJobCardEntryUseCase` - 7 tests
- `GetFeatureTypesUseCase` - 6 tests
- `GetProjectSettingsUseCase` - 5 tests
- `SaveProjectSettingsUseCase` - 6 tests
- `GetWorkOrdersUseCase` - 7 tests
- All other use cases fully tested

#### Data Layer Tests (69 tests, 90% coverage)

**Repository Tests (37 tests)**:

- `AuthRepositoryImpl` - 7 tests
- `FeatureRepositoryImpl` - 9 tests
- `JobCardEntryRepositoryImpl` - 13 tests
- `ManageESRepositoryImpl` - 8 tests

**Mapper Tests (38 tests)**:

- `UserMapper` - 5 tests
- `JobCardMapper` - 10 tests (70+ fields)
- `FeatureTypeMapper` - 13 tests
- `ProjectSettingsMapper` - 10 tests

#### Presentation Layer Tests (55+ tests, 90% coverage)

- `LoginViewModel` - 14 tests
- `JobCardEntryViewModel` - 19 tests
- `CollectESViewModel` - 14 tests
- `ManageESViewModel` - 8 tests
- `ProjectSettingsViewModel` - Fully tested

#### UI Layer Tests (25+ tests, 60% coverage)

- `JobCardEntryScreen` - 11 tests
- `CollectESBottomSheet` - 14 tests
- `LoginScreen` - Fully tested
- `ManageESBottomSheet` - Fully tested

### Testing Patterns Used

- **AAA Pattern** (Arrange-Act-Assert) for clarity
- **Descriptive Test Names** using backtick notation
- **MockK** for Kotlin-first mocking
- **Turbine** for Flow and StateFlow testing
- **Proper Coroutine Testing** with TestDispatcher
- **Compose UI Testing** with semantic matchers

### Code Quality Tools

- **Jacoco**: Code coverage reporting (90%+ coverage)
- **ProGuard/R8**: Code shrinking and obfuscation
- **KSP**: Faster annotation processing
- **Lint**: Static code analysis
- **Unit Tests**: Comprehensive test suite

### Coverage Thresholds

- **Minimum Overall Coverage**: 80%
- **Minimum Branch Coverage**: 70%
- **Build fails** if coverage drops below thresholds

## Release Build

### Release Readiness

The application is **RELEASE READY** with comprehensive security and optimization:

#### Security Enhancements

- **ProGuard/R8 Configuration**: Comprehensive obfuscation rules
- **API Key Security**: Secure loading from `local.properties`
- **Signing Configuration**: Template and instructions provided
- **Enhanced .gitignore**: All sensitive files excluded
- **Minification Enabled**: Code and resource shrinking

#### Code Quality

- **String Externalization**: All UI strings in resources (250+ strings)
- **Localization Ready**: Multi-language support prepared
- **Build Optimization**: Kotlin compiler optimizations enabled
- **200+ Tests**: Comprehensive test coverage
- **ProGuard Tested**: Release builds verified

### Building Release APK

#### Prerequisites

1. **Generate Keystore** (first time only):
   ```bash
   keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release-key
   ```

2. **Create keystore.properties**:
   ```bash
   cp keystore.properties.template keystore.properties
   # Edit with your keystore details
   ```

#### Build Commands

```bash
# Debug build
.\gradlew.bat assembleElectronicDebug

# Release build
.\gradlew.bat assembleElectronicRelease

# Android App Bundle (for Google Play)
.\gradlew.bat bundleElectronicRelease

# All variants
.\gradlew.bat assemble
```

#### Output Locations

- **Debug APK**: `app/build/outputs/apk/electronic/debug/`
- **Release APK**: `app/build/outputs/apk/electronic/release/`
- **App Bundle**: `app/build/outputs/bundle/electronicRelease/`
- **ProGuard Mapping**: `app/build/outputs/mapping/electronicRelease/mapping.txt`

### Security Notes

Before Production Release:

1. **ArcGIS API Key**: Currently in `local.properties`
    - Use CI/CD environment variables for production
    - Consider backend API proxy
    - Implement key rotation strategy

2. **API Endpoints**: Update placeholder URLs
    - Configure production endpoints
    - Use different URLs for dev/staging/production

3. **Keystore Management**:
    - **Never commit** keystore files
    - Back up keystore securely
    - Use password manager for credentials

4. **Testing**:
    - Test release build on physical devices
    - Verify ProGuard didn't break functionality
    - Check APK size and performance

## Documentation

### Primary Documentation

- **[README.md](README.md)** - This file (project overview)
- **[QUICK_START.md](QUICK_START.md)** - Setup and configuration guide

### Detailed Documentation

- **[docs/TESTING.md](docs/TESTING.md)** - Comprehensive testing guide
    - Test structure and organization
    - Running tests and generating reports
    - Writing new tests
    - Testing patterns and best practices
    - CI/CD integration examples

- **[docs/FUTURE_UPGRADES.md](docs/FUTURE_UPGRADES.md)** - Roadmap and planned features
    - Planned enhancements by phase
    - Technical debt items
    - Feature enhancements
    - Technology upgrades

- **[docs/KNOWN_ISSUES.md](docs/KNOWN_ISSUES.md)** - Current limitations and workarounds
    - Known issues by module
    - Workarounds and solutions
    - Issue reporting guidelines

- **[docs/TEST_IMPLEMENTATION_SUMMARY.md](docs/TEST_IMPLEMENTATION_SUMMARY.md)** - Testing
  implementation record
    - Historical record of test implementation
    - All tests created with details
    - Coverage achievements

## String Resource Management

The project follows Android best practices for string externalization:

- **`app/res/values/strings.xml`** - App-level strings and variant names
- **`design-system/res/values/strings.xml`** - Common UI component strings
- **`feature_auth/res/values/strings.xml`** - Login and authentication strings
- **`feature_jobs/res/values/strings.xml`** - Job card entry strings (200+ strings)
- **`feature_map/res/values/strings.xml`** - Map and ES management strings

All user-facing strings are externalized for:

- Localization support (ready for translation)
- Consistency across the app
- Easier maintenance and updates
- Format strings with proper placeholders

## Contributing

When contributing to this project:

### Code Standards

1. Follow Kotlin coding conventions
2. Maintain clean architecture principles
3. Ensure all tests pass before submitting
4. Update documentation for new features
5. Follow the established design system guidelines
6. Externalize all user-facing strings to resources

### Testing Requirements

1. Write tests for new features (aim for 85%+ coverage)
2. Follow AAA pattern (Arrange-Act-Assert)
3. Use descriptive test names with backticks
4. Test all success and failure scenarios
5. Run tests locally before pushing:
   ```bash
   .\gradlew.bat test jacocoTestReport
   ```

### Pull Request Process

1. Create feature branch from `main`
2. Implement changes with tests
3. Ensure all tests pass
4. Update relevant documentation
5. Submit PR with clear description
6. Address review feedback

### Build Verification

1. Test release builds before merging
2. Verify ProGuard configuration
3. Check for lint warnings
4. Update version numbers appropriately

## Development Commands

### Build Commands

```bash
# Clean build
.\gradlew.bat clean

# Build all modules
.\gradlew.bat build

# Assemble debug APK
.\gradlew.bat assembleElectronicDebug

# Assemble release APK
.\gradlew.bat assembleElectronicRelease

# Install debug APK
.\gradlew.bat installElectronicDebug
```

### Testing Commands

```bash
# Run all unit tests
.\gradlew.bat test

# Run specific module tests
.\gradlew.bat :domain:testDebugUnitTest
.\gradlew.bat :data:testDebugUnitTest

# Run with coverage
.\gradlew.bat test jacocoTestReport

# Run instrumented tests
.\gradlew.bat connectedAndroidTest

# Verify coverage thresholds
.\gradlew.bat jacocoCoverageVerification
```

### Utility Commands

```bash
# Check dependencies
.\gradlew.bat :app:dependencies

# Gradle version
.\gradlew.bat -version

# List available tasks
.\gradlew.bat tasks

# Check project structure
.\gradlew.bat projects
```

## Recent Updates

### Testing Implementation (January 2025)

- **200+ Tests**: Comprehensive test suite across all layers
- **90% Coverage**: Exceeds industry standards
- **Jacoco Reporting**: Interactive HTML and XML reports
- **Coverage Thresholds**: Automatic verification
- **Documentation**: Complete testing guide

### Release Readiness (December 2024)

- **Security**: Enhanced ProGuard rules, API key documentation
- **Build System**: Signing configuration, minification
- **Code Quality**: String externalization (250+ strings)
- **Documentation**: Comprehensive release checklist

### Build Variants Implementation (November 2024)

- **5 Product Flavors**: electronic, maintenance, construction, resurvey, gasStorage
- **Variant-Specific Branding**: Unique names, icons, and features
- **Conditional Features**: ES Job Card Entry only in electronic variant
- **Side-by-Side Installation**: Different application IDs

## Support & Resources

### Documentation Links

- [Android Studio Documentation](https://developer.android.com/studio)
- [ArcGIS Maps SDK Documentation](https://developers.arcgis.com/kotlin)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)

### Project Documentation

- **Quick Start**: [QUICK_START.md](QUICK_START.md)
- **Testing**: [docs/TESTING.md](docs/TESTING.md)
- **Known Issues**: [docs/KNOWN_ISSUES.md](docs/KNOWN_ISSUES.md)
- **Roadmap**: [docs/FUTURE_UPGRADES.md](docs/FUTURE_UPGRADES.md)

### Getting Help

1. Check [docs/KNOWN_ISSUES.md](docs/KNOWN_ISSUES.md) for documented problems
2. Review LogCat output for error details
3. Consult project documentation
4. Contact the development team

## License

Copyright 2025 Enbridge. All rights reserved.

## Contact

For questions or support, please contact the development team.

## Last Updated

October 2025  
**Version**: 1.0.0  
**Status**: Production Ready 
