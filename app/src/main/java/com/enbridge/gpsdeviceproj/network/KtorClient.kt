package com.enbridge.gpsdeviceproj.network

/**
 * @author Sathya Narayanan
 */

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * KtorClient provides a configured HTTP client for network operations.
 *
 * This client is configured with:
 * - Content negotiation for JSON serialization/deserialization
 * - Request logging for debugging purposes
 * - Timeout configurations for network requests
 * - Android-specific optimizations
 *
 * The client uses kotlinx.serialization for JSON handling and is configured
 * to be lenient with unknown JSON properties to ensure forward compatibility.
 */
object KtorClient {

    // TODO: BEST PRACTICE - API URL Configuration for Different Environments
    //
    // CURRENT APPROACH:
    // - Hardcoded placeholder URL (https://api.example.com/)
    // - This should be replaced with actual API endpoints
    //
    // RECOMMENDED APPROACHES:
    //
    // 1. BUILD VARIANTS WITH DIFFERENT BASE URLs (Recommended):
    //    In app/build.gradle.kts or core/build.gradle.kts:
    //    
    //    buildTypes {
    //        debug {
    //            buildConfigField("String", "API_BASE_URL", "\"https://api-dev.example.com/\"")
    //        }
    //        release {
    //            buildConfigField("String", "API_BASE_URL", "\"https://api.example.com/\"")
    //        }
    //    }
    //
    //    Then use: url(BuildConfig.API_BASE_URL)
    //
    // 2. PRODUCT FLAVORS FOR MULTIPLE ENVIRONMENTS:
    //    
    //    flavorDimensions += "environment"
    //    productFlavors {
    //        create("dev") {
    //            dimension = "environment"
    //            buildConfigField("String", "API_BASE_URL", "\"https://api-dev.example.com/\"")
    //        }
    //        create("staging") {
    //            dimension = "environment"
    //            buildConfigField("String", "API_BASE_URL", "\"https://api-staging.example.com/\"")
    //        }
    //        create("production") {
    //            dimension = "environment"
    //            buildConfigField("String", "API_BASE_URL", "\"https://api.example.com/\"")
    //        }
    //    }
    //
    // 3. ENVIRONMENT VARIABLES (CI/CD):
    //    Load from environment variable during build:
    //    buildConfigField("String", "API_BASE_URL",
    //                     "\"${System.getenv("API_BASE_URL") ?: "https://api.example.com/"}\"")
    //
    // 4. LOCAL.PROPERTIES (Development Flexibility):
    //    Store in local.properties (already used for ARCGIS_API_KEY):
    //    API_BASE_URL_DEV=https://api-dev.example.com/
    //    API_BASE_URL_PROD=https://api.example.com/
    //
    // 5. REMOTE CONFIG (Runtime Flexibility):
    //    - Use Firebase Remote Config or similar
    //    - Can update API endpoints without app update
    //    - Good for gradual rollouts or emergency endpoint changes
    //    - Requires fallback URL in case of network issues
    //
    // IMPLEMENTATION STEPS:
    // 1. Decide which approach fits your deployment strategy
    // 2. Add buildConfigField in appropriate gradle file
    // 3. Replace hardcoded URL below with BuildConfig.API_BASE_URL
    // 4. Test with different build variants/flavors
    // 5. Document endpoint configuration in README
    //
    // SECURITY CONSIDERATIONS:
    // - Use HTTPS for all API endpoints
    // - Implement certificate pinning for production
    // - Validate server certificates
    // - Consider using different API keys per environment
    //
    // EXAMPLE USAGE AFTER SETUP:
    // url(BuildConfig.API_BASE_URL)

    /**
     * Creates and configures an HTTP client instance.
     *
     * @return A fully configured HttpClient ready for network requests
     */
    fun create(): HttpClient {
        return HttpClient(Android) {
            // Configure JSON content negotiation with lenient parsing
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            // Enable request/response logging for debugging
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }

            // Configure timeout settings for all requests
            install(HttpTimeout) {
                requestTimeoutMillis = 30000  // 30 seconds
                connectTimeoutMillis = 30000  // 30 seconds
                socketTimeoutMillis = 30000   // 30 seconds
            }

            // Set the base URL for API requests
            defaultRequest {
                url("https://api.example.com/")
            }
        }
    }
}
