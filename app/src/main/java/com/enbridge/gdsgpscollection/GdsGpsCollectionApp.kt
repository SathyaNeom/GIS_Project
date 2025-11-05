package com.enbridge.gdsgpscollection

/**
 * @author Sathya Narayanan
 */
import android.app.Application
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.enbridge.gdsgpscollection.util.Logger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GdsGpsCollectionApp : Application() {

    companion object {
        private const val TAG = "GdsGpsCollectionApp"
    }

    override fun onCreate() {
        super.onCreate()

        Logger.i(TAG, "========================================")
        Logger.i(TAG, "Application Starting")
        Logger.i(TAG, "App Variant: ${BuildConfig.APP_VARIANT}")
        Logger.i(TAG, "App Name: ${BuildConfig.APP_NAME}")
        Logger.i(TAG, "Build Type: ${if (BuildConfig.DEBUG) "DEBUG" else "RELEASE"}")
        Logger.i(TAG, "Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        Logger.i(TAG, "========================================")

        // Set the ArcGIS API key
        // TODO: SECURITY - Secure ArcGIS API Key for Production Release
        //
        // CURRENT APPROACH (Development Only):
        // - API key is loaded from local.properties via BuildConfig
        // - This is acceptable for development but NOT secure for production
        //
        // RECOMMENDED APPROACHES FOR PRODUCTION:
        //
        // 1. ENVIRONMENT VARIABLES IN CI/CD (Most Secure - Recommended):
        //    - Store the API key as a secret environment variable in your CI/CD pipeline
        //      (GitHub Actions, GitLab CI, Jenkins, etc.)
        //    - Inject the key during build time:
        //      * GitHub Actions: Use repository secrets
        //        Example: ${{ secrets.ARCGIS_API_KEY }}
        //      * GitLab CI: Use protected variables
        //      * Jenkins: Use credentials binding
        //    - In build.gradle.kts:
        //      buildConfigField("String", "ARCGIS_API_KEY", "\"${System.getenv("ARCGIS_API_KEY") ?: ""}\"")
        //
        // 2. BACKEND API PROXY (Most Secure for Client Apps):
        //    - Do NOT store the API key in the app at all
        //    - Create a backend service that acts as a proxy to ArcGIS services
        //    - Your app authenticates with your backend (OAuth, JWT, etc.)
        //    - Backend adds the ArcGIS API key to requests server-side
        //    - This prevents key extraction from the APK
        //
        // 3. ANDROID KEYSTORE SYSTEM (Moderate Security):
        //    - Use Android Keystore to encrypt the API key
        //    - Store encrypted key in SharedPreferences or a file
        //    - Decrypt at runtime using Keystore
        //    - Note: This is obfuscation, not foolproof security
        //
        // 4. NATIVE LIBRARY (JNI) WITH OBFUSCATION (Moderate Security):
        //    - Store the API key in a native C/C++ library
        //    - Access via JNI from Kotlin
        //    - Combine with ProGuard/R8 obfuscation
        //    - Makes reverse engineering harder but not impossible
        //
        // 5. SECRETS GRADLE PLUGIN (Development/Small Teams):
        //    - Use a plugin like secrets-gradle-plugin
        //    - Keeps keys out of version control
        //    - Still embedded in APK (can be extracted)
        //
        // STEPS TO IMPLEMENT FOR CI/CD:
        //
        // Step 1: Add environment variable to your CI/CD platform
        //   - Name: ARCGIS_API_KEY
        //   - Value: Your actual API key
        //   - Mark as "secret" or "protected"
        //
        // Step 2: Update core/build.gradle.kts:
        //   buildConfigField("String", "ARCGIS_API_KEY",
        //                    "\"${System.getenv("ARCGIS_API_KEY") ?: "PLACEHOLDER_KEY"}\"")
        //
        // Step 3: For local development, keep using local.properties
        //   (as fallback when environment variable is not set)
        //
        // Step 4: Add local.properties to .gitignore (already done)
        //
        // Step 5: Document the setup process for new developers in README
        //
        // SECURITY BEST PRACTICES:
        // - Never commit API keys to version control
        // - Use different keys for development, staging, and production
        // - Implement key rotation policies
        // - Monitor API key usage for suspicious activity
        // - Set up ArcGIS referrer restrictions if available
        // - Consider implementing certificate pinning for network security
        //
        // LINKS:
        // - ArcGIS Security Best Practices: https://developers.arcgis.com/documentation/mapping-apis-and-services/security/
        // - Android Security: https://developer.android.com/privacy-and-security/security-tips
        // - Secrets Gradle Plugin: https://github.com/google/secrets-gradle-plugin

        try {
            Logger.d(TAG, "Initializing ArcGIS Environment")
            val apiKey = BuildConfig.ARCGIS_API_KEY

            if (apiKey.isBlank()) {
                Logger.e(TAG, "ArcGIS API Key is blank! Map functionality will not work.")
                Logger.e(TAG, "Please configure ARCGIS_API_KEY in local.properties file")
            } else {
                Logger.d(TAG, "ArcGIS API Key configured (${apiKey.take(10)}...)")
            }

            ArcGISEnvironment.apiKey = ApiKey.create(apiKey)
            Logger.i(TAG, "ArcGIS Environment initialized successfully")
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to initialize ArcGIS Environment", e)
        }

        Logger.i(TAG, "Application initialization completed")
    }
}
