/**
 * @author Sathya Narayanan
 */
import org.gradle.kotlin.dsl.android
import org.gradle.kotlin.dsl.kapt
import org.gradle.kotlin.dsl.kotlinOptions
import org.gradle.kotlin.dsl.libs

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.enbridge.electronicservices.feature.auth"
    compileSdk = 36

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    // Product flavors matching the app module
    flavorDimensions += "variant"
    productFlavors {
        create("electronic") {
            dimension = "variant"
            buildConfigField("String", "APP_VARIANT", "\"electronic\"")
            buildConfigField("String", "APP_NAME", "\"Electronic Services\"")
        }
        create("maintenance") {
            dimension = "variant"
            buildConfigField("String", "APP_VARIANT", "\"maintenance\"")
            buildConfigField("String", "APP_NAME", "\"Maintenance\"")
        }
        create("construction") {
            dimension = "variant"
            buildConfigField("String", "APP_VARIANT", "\"construction\"")
            buildConfigField("String", "APP_NAME", "\"Construction\"")
        }
        create("resurvey") {
            dimension = "variant"
            buildConfigField("String", "APP_VARIANT", "\"resurvey\"")
            buildConfigField("String", "APP_NAME", "\"Resurvey\"")
        }
        create("gasStorage") {
            dimension = "variant"
            buildConfigField("String", "APP_VARIANT", "\"gas-storage\"")
            buildConfigField("String", "APP_NAME", "\"Gas Storage\"")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true  // Enable BuildConfig generation
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(project(":design-system"))
    implementation(project(":core"))
    implementation(project(":domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.compiler)

    // Add data module for Android instrumented tests
    androidTestImplementation(project(":data"))
}
