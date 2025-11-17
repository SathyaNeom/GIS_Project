# Release Checklist for GDS GPS Collection Android App

This comprehensive checklist ensures your app is ready for production release. Follow each step
carefully before building and publishing your release APK/AAB.

## Table of Contents

- [Pre-Release Checklist](#pre-release-checklist)
- [Security Checklist](#security-checklist)
- [Code Quality Checklist](#code-quality-checklist)
- [Testing Checklist](#testing-checklist)
- [Build Configuration Checklist](#build-configuration-checklist)
- [Performance Checklist](#performance-checklist)
- [Legal & Compliance Checklist](#legal--compliance-checklist)
- [Post-Build Checklist](#post-build-checklist)
- [Release Steps](#release-steps)

---

## Pre-Release Checklist

### Version Management

- [ ] **Update version code** in `app/build.gradle.kts`
    - Increment `versionCode` (must be higher than previous release)
    - Update `versionName` (e.g., "1.0", "1.1", "2.0")

- [ ] **Update CHANGELOG** or release notes
    - Document new features
    - Document bug fixes
    - Document breaking changes

### Code Cleanup

- [ ] **Remove all TODO comments** or ensure they're not critical
    - Check for `TODO`, `FIXME`, `HACK` comments
    - Address or document any remaining items

- [ ] **Remove all debug code**
    - Remove test code paths
    - Remove debug logging statements
    - Verify no hardcoded test credentials

- [ ] **Remove all unused resources**
    - Run "Remove Unused Resources" in Android Studio
    - Clean up unused imports
    - Remove commented-out code

---

## Security Checklist

### API Keys & Secrets

- [ ] **ArcGIS API Key** is NOT hardcoded
    - ✅ Loaded from `local.properties` (development)
    - ✅ Documented security best practices in `GdsGpsCollectionApp.kt`
    - [ ] TODO: Implement CI/CD environment variable approach (see TODO in code)
    - [ ] Consider backend proxy for production (most secure)

- [ ] **No hardcoded credentials** in code
    - Check for usernames, passwords, tokens
    - Verify no API keys in source files
    - Check for database credentials

- [ ] **Verify .gitignore** is properly configured
    - ✅ `local.properties` is ignored
    - ✅ `keystore.properties` is ignored
    - ✅ `*.jks` and `*.keystore` files are ignored
    - Check no sensitive files committed

### Code Obfuscation

- [ ] **ProGuard/R8 rules** are comprehensive
    - ✅ Updated `proguard-rules.pro` with all necessary rules
    - ✅ Rules for Kotlin, Compose, Hilt, Room, Ktor, ArcGIS
    - ✅ Domain models are preserved for serialization
    - Test that obfuscated APK works correctly

- [ ] **Minification enabled** in release build
    - ✅ `isMinifyEnabled = true` in `app/build.gradle.kts`
    - ✅ `isShrinkResources = true` enabled

### Signing Configuration

- [ ] **Keystore is generated** and stored securely
    - [ ] Generated with: `keytool -genkey -v -keystore release-keystore.jks ...`
    - [ ] Keystore backed up in secure location
    - [ ] Passwords stored in password manager

- [ ] **Signing configuration** is set up
    - [ ] Created `keystore.properties` from template
    - [ ] Configured signing in `app/build.gradle.kts`
    - [ ] Verified signing config works

- [ ] **Keystore security**
    - [ ] Keystore file NOT in version control
    - [ ] Passwords NOT in version control
    - [ ] CI/CD uses environment variables/secrets

### Network Security

- [ ] **All API endpoints use HTTPS**
    - ✅ Default URL in `KtorClient.kt` uses HTTPS
    - [ ] Verify actual production API uses HTTPS
    - [ ] Consider implementing certificate pinning

- [ ] **Network security config** (if applicable)
    - Add `res/xml/network_security_config.xml` if needed
    - Configure cleartext traffic settings
    - Set up certificate pinning

---

## Code Quality Checklist

### Code Review

- [ ] **All code reviewed** by team member
- [ ] **No compiler warnings** (or all acceptable)
- [ ] **Linter checks pass** with no critical issues
- [ ] **Code follows project conventions**
    - Kotlin style guide
  - Clean Architecture principles (see [ARCHITECTURE.md](ARCHITECTURE.md))
    - Naming conventions
  - Development standards documented in architecture guide

### String Resources

- [ ] **All UI strings externalized**
    - ✅ Login screen strings in `feature_auth/res/values/strings.xml`
    - ✅ Job card strings in `feature_jobs/res/values/strings.xml`
    - ✅ Map screen strings in `feature_map/res/values/strings.xml`
    - ✅ Common strings in `design-system/res/values/strings.xml`
    - [ ] Verify no hardcoded strings remain

- [ ] **Prepare for localization** (if applicable)
    - String resources are properly formatted
    - Placeholders use format strings correctly
    - Consider creating alternate language directories

### Dependencies

- [ ] **All dependencies up to date**
    - Check for security updates
    - Review dependency vulnerabilities
    - Update `gradle/libs.versions.toml`

- [ ] **Remove unused dependencies**
    - Check for unused libraries
    - Remove test-only dependencies from release

---

## Testing Checklist

### Unit Tests

- [ ] **All unit tests passing**
    - Run: `./gradlew test`
    - Minimum code coverage achieved
    - Critical business logic covered

### Instrumented Tests

- [ ] **All instrumented tests passing**
    - Run: `./gradlew connectedAndroidTest`
    - UI tests cover critical flows
    - Test on multiple API levels

### Manual Testing

- [ ] **Test all product flavors**
    - [ ] Electronic variant
    - [ ] Maintenance variant
    - [ ] Construction variant
    - [ ] Resurvey variant
    - [ ] Gas Storage variant

- [ ] **Test critical user flows**
    - [ ] Login flow
    - [ ] Map navigation
    - [ ] Job card entry
    - [ ] Data synchronization
    - [ ] Offline functionality

- [ ] **Test on multiple devices**
    - [ ] Different screen sizes
    - [ ] Different Android versions (min API 34+)
    - [ ] Different manufacturers

### Build Testing

- [ ] **Test release build** before publishing
    - Build release APK: `./gradlew assembleElectronicRelease`
    - Install and test thoroughly
    - Verify ProGuard didn't break functionality
    - Check app size is acceptable

---

## Build Configuration Checklist

### Gradle Configuration

- [ ] **Build types configured**
    - ✅ Debug build with debug suffix
    - ✅ Release build with minification
    - ✅ Signing configuration ready

- [ ] **Product flavors configured**
    - ✅ 5 variants properly configured
    - Each variant has unique app ID suffix
    - Each variant has appropriate resources

### Manifest

- [ ] **AndroidManifest.xml reviewed**
    - App name uses string resource
    - Permissions are necessary and documented
    - No debug permissions in release
    - Backup rules configured

- [ ] **Permissions audit**
    - All permissions are necessary
    - Runtime permissions properly requested
    - Permission rationale provided to users

### Resources

- [ ] **App icon** configured for all densities
    - Launcher icons for all variants
    - Adaptive icons provided
    - Notification icons (if applicable)

- [ ] **Splash screen** (if applicable)
    - Configured properly
    - Follows Material Design guidelines

---

## Performance Checklist

### App Size

- [ ] **APK/AAB size optimized**
    - R8 shrinking enabled
    - Resource shrinking enabled
    - App Bundle format used (AAB)
    - Consider splitting APKs by ABI if needed

### Performance

- [ ] **No memory leaks**
    - Test with LeakCanary in debug
    - Profile with Android Studio Profiler
    - Check for lifecycle awareness

- [ ] **Smooth animations**
    - 60 FPS maintained
    - Janky frames minimized
    - Compose recomposition optimized

- [ ] **Fast startup time**
    - Cold start under 3 seconds
    - Warm start under 1 second
    - Lazy initialization where appropriate

### Battery & Data

- [ ] **Battery usage optimized**
    - Background tasks minimized
    - Wake locks properly released
    - Doze mode compatibility

- [ ] **Data usage optimized**
    - Caching implemented
    - Offline mode functional
    - Large downloads on WiFi only

---

## Legal & Compliance Checklist

### Privacy

- [ ] **Privacy policy** created and accessible
    - Link in app (if collecting data)
    - Compliant with GDPR/CCPA
    - Clear data collection disclosure

- [ ] **Data handling**
    - User data encrypted
    - Secure storage practices
    - Data deletion capability

### Licensing

- [ ] **Third-party licenses** acknowledged
    - ArcGIS SDK license compliance
    - Open source licenses listed
    - Attribution provided where required

### Content

- [ ] **No placeholder content** in release
    - All images are final
    - All text is final
    - No "lorem ipsum" text

---

## Post-Build Checklist

### APK Analysis

- [ ] **Analyze APK** using Android Studio
    - Build > Analyze APK
    - Check APK size breakdown
    - Verify classes obfuscated
    - Check for sensitive strings

### Security Testing

- [ ] **Decompile APK** to verify obfuscation
    - Use jadx or similar tool
    - Verify no secrets exposed
    - Check code is obfuscated

### Final Verification

- [ ] **Install release APK** on fresh device
    - Uninstall any previous versions
    - Install release APK
    - Test all critical features
    - Verify no crashes

- [ ] **Check crash reporting** integration
    - Firebase Crashlytics (if used)
    - Test crash reporting works
    - Verify symbolication for stack traces

---

## Release Steps

### Building Release APK

#### For Single Variant:

```bash
# Build electronic variant release
./gradlew assembleElectronicRelease

# Output location:
# app/build/outputs/apk/electronic/release/app-electronic-release.apk
```

#### For All Variants:

```bash
# Build all release variants
./gradlew assembleRelease
```

#### For Android App Bundle (Google Play):

```bash
# Build AAB for upload to Play Store
./gradlew bundleElectronicRelease

# Output location:
# app/build/outputs/bundle/electronicRelease/app-electronic-release.aab
```

### Verification Commands

```bash
# List signing certificate
keytool -printcert -jarfile app-electronic-release.apk

# Verify ProGuard mapping file exists
ls app/build/outputs/mapping/electronicRelease/

# Check APK signatures
apksigner verify --verbose app-electronic-release.apk
```

### Pre-Upload to Play Store

- [ ] **Prepare store listing**
    - App description
    - Screenshots (all required sizes)
    - Feature graphic
    - App icon

- [ ] **Set up Google Play** (if first release)
    - Create app in Play Console
    - Complete store listing
    - Set up pricing & distribution
    - Configure content rating

- [ ] **Upload AAB** to Play Console
    - Internal testing track first
    - Then closed alpha/beta
    - Finally production release

### Post-Release

- [ ] **Monitor crash reports**
- [ ] **Monitor user reviews**
- [ ] **Monitor ANRs**
- [ ] **Check performance metrics**
- [ ] **Backup ProGuard mapping files** (for crash deobfuscation)

---

## Important Files Locations

| File             | Location                                      | Purpose             |
|------------------|-----------------------------------------------|---------------------|
| Release APK      | `app/build/outputs/apk/[variant]/release/`    | Installable APK     |
| App Bundle (AAB) | `app/build/outputs/bundle/[variant]Release/`  | Google Play upload  |
| ProGuard Mapping | `app/build/outputs/mapping/[variant]Release/` | Crash deobfuscation |
| Keystore         | Outside project (secure location)             | App signing         |

---

## Quick Commands Reference

```bash
# Clean build
./gradlew clean

# Build debug
./gradlew assembleElectronicDebug

# Build release (all variants)
./gradlew assembleRelease

# Build specific variant release
./gradlew assembleElectronicRelease

# Run tests
./gradlew test

# Run lint
./gradlew lint

# Check dependencies
./gradlew dependencies

# Generate ProGuard mapping
# (automatically generated during release build)
```

---

## Emergency Rollback Plan

If issues are discovered after release:

1. **Halt rollout** in Play Console
2. **Identify issue** using crash reports
3. **Fix issue** in hotfix branch
4. **Test thoroughly**
5. **Increment version code** and build
6. **Submit hotfix** for expedited review
7. **Document incident** for future prevention

---

## Notes

- Always keep a backup of your keystore file - you cannot update your app without it
- Store ProGuard mapping files for each release - needed for crash deobfuscation
- Test release builds on physical devices, not just emulators
- Consider phased rollouts (10% → 50% → 100%) for major releases
- Monitor metrics closely for first 24-48 hours after release

---

**Last Updated:** November 2025
**Version:** 1.0
**App Version:** 1.0 (versionCode 1)
