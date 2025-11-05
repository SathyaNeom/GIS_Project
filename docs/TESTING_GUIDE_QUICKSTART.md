# Testing Quick Start Guide

## Overview

This guide provides quick commands for running tests and viewing reports in the GDS GPS Collection
project.

## Test Types

The project has two types of tests:

### 1. Unit Tests (No device required)

- Run on the JVM using Robolectric
- Test ViewModels, Use Cases, Repositories, Mappers
- **Fast execution** (~10-30 seconds)
- Generate coverage reports

### 2. Instrumented Tests (Requires device/emulator)

- Run on Android device or emulator
- Test UI components (Compose screens)
- **Slower execution** (2-5 minutes)
- Require physical device or running emulator

---

## Quick Commands

### Run Unit Tests Only (Recommended)

For the `feature_map` module:

```bash
.\gradlew.bat :feature_map:testDebugUnitTest
```

**View test report:**

```
feature_map/build/reports/tests/testDebugUnitTest/index.html
```

### Run Unit Tests + Generate Coverage Report

```bash
.\gradlew.bat :feature_map:testDebugUnitTest :feature_map:jacocoTestReport
```

**View reports:**

- **Test Results:** `feature_map/build/reports/tests/testDebugUnitTest/index.html`
- **Coverage Report:** `feature_map/build/reports/jacoco/jacocoTestReport/html/index.html`

### Run ALL Module Unit Tests

```bash
.\gradlew.bat test
```

### Run ALL Module Tests + Unified Coverage Report

```bash
.\gradlew.bat test jacocoTestReport
```

**View unified report:**

```
build/reports/jacoco/html/index.html
```

---

## Running Instrumented Tests (UI Tests)

⚠️ **Prerequisites:**

1. Start an Android emulator from AVD Manager in Android Studio, OR
2. Connect a physical Android device via USB

### Run Instrumented Tests

```bash
# For feature_map module
.\gradlew.bat :feature_map:connectedDebugAndroidTest

# For feature_auth module  
.\gradlew.bat :feature_auth:connectedElectronicDebugAndroidTest

# For feature_jobs module
.\gradlew.bat :feature_jobs:connectedDebugAndroidTest
```

**View instrumented test report:**

```
feature_map/build/reports/androidTests/connected/debug/index.html
```

---

## Open Reports from Command Line

### Windows PowerShell

```powershell
# Test results
start feature_map/build/reports/tests/testDebugUnitTest/index.html

# Coverage report
start feature_map/build/reports/jacoco/jacocoTestReport/html/index.html

# Instrumented test results
start feature_map/build/reports/androidTests/connected/debug/index.html
```

### Windows CMD

```cmd
start feature_map\build\reports\tests\testDebugUnitTest\index.html
start feature_map\build\reports\jacoco\jacocoTestReport\html\index.html
```

---

## Module-Specific Commands

### Domain Layer

```bash
# Run tests
.\gradlew.bat :domain:testDebugUnitTest

# With coverage
.\gradlew.bat :domain:testDebugUnitTest :domain:jacocoTestReport
```

**Reports:**

- Tests: `domain/build/reports/tests/testDebugUnitTest/index.html`
- Coverage: `domain/build/reports/jacoco/test/html/index.html`

### Data Layer

```bash
# Run tests
.\gradlew.bat :data:testDebugUnitTest

# With coverage
.\gradlew.bat :data:testDebugUnitTest :data:jacocoTestReport
```

**Reports:**

- Tests: `data/build/reports/tests/testDebugUnitTest/index.html`
- Coverage: `data/build/reports/jacoco/test/html/index.html`

### Feature Auth

```bash
# Run tests (note the variant: Electronic)
.\gradlew.bat :feature_auth:testElectronicDebugUnitTest

# With coverage
.\gradlew.bat :feature_auth:testElectronicDebugUnitTest :feature_auth:jacocoTestReport

# Instrumented tests (requires device)
.\gradlew.bat :feature_auth:connectedElectronicDebugAndroidTest
```

### Feature Jobs

```bash
# Run tests
.\gradlew.bat :feature_jobs:testDebugUnitTest

# With coverage
.\gradlew.bat :feature_jobs:testDebugUnitTest :feature_jobs:jacocoTestReport

# Instrumented tests (requires device)
.\gradlew.bat :feature_jobs:connectedDebugAndroidTest
```

### Feature Map

```bash
# Run tests
.\gradlew.bat :feature_map:testDebugUnitTest

# With coverage
.\gradlew.bat :feature_map:testDebugUnitTest :feature_map:jacocoTestReport

# Instrumented tests (requires device)
.\gradlew.bat :feature_map:connectedDebugAndroidTest
```

---

## Troubleshooting

### Issue: "No compatible devices connected"

**Problem:** Trying to run `connectedDebugAndroidTest` without a device/emulator

**Solution:**

- **Option 1:** Run unit tests instead: `.\gradlew.bat :feature_map:testDebugUnitTest`
- **Option 2:** Start an emulator in Android Studio (AVD Manager)
- **Option 3:** Connect a physical device via USB with USB debugging enabled

### Issue: Build fails with dagger/kapt errors

**Problem:** Gradle configuration errors

**Solution:**

```bash
# Clean and rebuild
.\gradlew.bat clean
.\gradlew.bat :feature_map:testDebugUnitTest --rerun-tasks
```

### Issue: Reports not generating

**Problem:** Tests are cached, Jacoco skips report generation

**Solution:**

```bash
# Force fresh test run and report
.\gradlew.bat :feature_map:testDebugUnitTest :feature_map:jacocoTestReport --rerun-tasks
```

### Issue: Can't find report files

**Problem:** Reports are in different locations than expected

**Solution:** Check these locations:

**Unit Test Reports:**

- `[module]/build/reports/tests/testDebugUnitTest/index.html`

**Coverage Reports:**

- `[module]/build/reports/jacoco/jacocoTestReport/html/index.html` (feature modules)
- `[module]/build/reports/jacoco/test/html/index.html` (domain, data modules)

**Instrumented Test Reports:**

- `[module]/build/reports/androidTests/connected/debug/index.html`

---

## Understanding Test Results

### Test Report (HTML)

Shows:

- Total tests run
- Passed/Failed counts
- Execution time
- Failed test details with stack traces

### Coverage Report (HTML)

Shows:

- **Overall Coverage %** - Percentage of code executed by tests
- **Branch Coverage %** - Percentage of conditional branches tested
- **Drill-down navigation** - Click through packages → classes → methods
- **Color coding:**
    - 🟢 Green: Covered code
    - 🔴 Red: Not covered
    - 🟡 Yellow: Partially covered branches

---

## Current Test Coverage

As documented in `TESTING.md`:

| Layer                             | Tests | Coverage | Status      |
|-----------------------------------|-------|----------|-------------|
| **Domain** (Use Cases)            | 50+   | ~95%     | ✅ Excellent |
| **Data** (Repositories & Mappers) | 69    | ~90%     | ✅ Excellent |
| **Presentation** (ViewModels)     | 55+   | ~90%     | ✅ Excellent |
| **UI** (Compose Screens)          | 25+   | ~60%     | ✅ Good      |

**Total:** 200+ comprehensive tests across all layers

---

## Best Practices

### Daily Development

1. **Run unit tests frequently** during development:
   ```bash
   .\gradlew.bat :feature_map:testDebugUnitTest
   ```

2. **Check coverage** before committing:
   ```bash
   .\gradlew.bat :feature_map:testDebugUnitTest :feature_map:jacocoTestReport
   ```

3. **Run full test suite** before creating PR:
   ```bash
   .\gradlew.bat test jacocoTestReport
   ```

### Before Release

1. **Run ALL tests** including instrumented:
   ```bash
   .\gradlew.bat test
   .\gradlew.bat connectedAndroidTest
   ```

2. **Verify coverage thresholds**:
   ```bash
   .\gradlew.bat jacocoCoverageVerification
   ```

3. **Review coverage reports** to ensure critical paths are tested

---

## Additional Resources

For detailed testing information, see:

- **[TESTING.md](TESTING.md)** - Comprehensive testing guide
- **[TEST_IMPLEMENTATION_SUMMARY.md](TEST_IMPLEMENTATION_SUMMARY.md)** - Implementation details
- **[docs/QUICK_START.md](../QUICK_START.md)** - General project quick start

---

## Summary for Your Situation

Based on your screenshot showing `connectedDebugAndroidTest` failure:

### ❌ What you were doing (incorrect):

```bash
.\gradlew.bat :feature_map:connectedDebugAndroidTest
```

- This requires a device/emulator
- Takes longer to run
- Tests UI components only

### ✅ What you should do instead:

```bash
# Option 1: Unit tests only (fast)
.\gradlew.bat :feature_map:testDebugUnitTest

# Option 2: Unit tests + coverage report (recommended)
.\gradlew.bat :feature_map:testDebugUnitTest :feature_map:jacocoTestReport
```

- No device required
- Tests ViewModels (most of your logic)
- Generates coverage reports
- Fast execution

**View reports:**

- Test results: `feature_map/build/reports/tests/testDebugUnitTest/index.html`
- Coverage: `feature_map/build/reports/jacoco/jacocoTestReport/html/index.html`

---

**Last Updated:** October 30, 2025
