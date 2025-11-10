/**
 * @author Sathya Narayanan
 */
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.serialization)
    id("jacoco")
}

android {
    namespace = "com.enbridge.gdsgpscollection"
    compileSdk = 36

    testOptions {
        animationsDisabled = true
        unitTests.isReturnDefaultValues = true

        // Test orchestrator disabled - add dependency if needed
        // execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }

    defaultConfig {
        applicationId = "com.enbridge.gdsgpscollection"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.enbridge.gdsgpscollection.HiltTestRunner"
        testInstrumentationRunnerArguments["functionalTest"] = "false"

        vectorDrawables {
            useSupportLibrary = true
        }

        // Load ArcGIS API Key from local.properties
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { properties.load(it) }
        }
        val arcgisApiKey = properties.getProperty("ARCGIS_API_KEY", "")
        buildConfigField("String", "ARCGIS_API_KEY", "\"$arcgisApiKey\"")

    }

    signingConfigs {
        getByName("debug") {
            // Uses default debug keystore
        }

        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(FileInputStream(keystorePropertiesFile))

                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    flavorDimensions += "variant"
    productFlavors {
        create("electronic") {
            dimension = "variant"
            applicationIdSuffix = ".electronic"
            versionNameSuffix = "-electronic"
            buildConfigField("String", "APP_VARIANT", "\"electronic\"")
            buildConfigField("String", "APP_NAME", "\"Electronic Services\"")
            resValue("string", "app_name", "Electronic Services")
        }
        create("maintenance") {
            dimension = "variant"
            applicationIdSuffix = ".maintenance"
            versionNameSuffix = "-maintenance"
            buildConfigField("String", "APP_VARIANT", "\"maintenance\"")
            buildConfigField("String", "APP_NAME", "\"Maintenance\"")
            resValue("string", "app_name", "Maintenance")
        }
        create("construction") {
            dimension = "variant"
            applicationIdSuffix = ".construction"
            versionNameSuffix = "-construction"
            buildConfigField("String", "APP_VARIANT", "\"construction\"")
            buildConfigField("String", "APP_NAME", "\"Construction\"")
            resValue("string", "app_name", "Construction")
        }
        create("resurvey") {
            dimension = "variant"
            applicationIdSuffix = ".resurvey"
            versionNameSuffix = "-resurvey"
            buildConfigField("String", "APP_VARIANT", "\"resurvey\"")
            buildConfigField("String", "APP_NAME", "\"Resurvey\"")
            resValue("string", "app_name", "Resurvey")
        }
        create("gasStorage") {
            dimension = "variant"
            applicationIdSuffix = ".gasstorage"
            versionNameSuffix = "-gas-storage"
            buildConfigField("String", "APP_VARIANT", "\"gas-storage\"")
            buildConfigField("String", "APP_NAME", "\"Gas Storage\"")
            resValue("string", "app_name", "Gas Storage")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("release")
        }
    }

    // Configure APK output names
    applicationVariants.all {
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val variantName = when (flavorName) {
                "construction" -> "Main_Construction"
                "electronic" -> "Electronic Services"
                "gasStorage" -> "Gas Storage"
                "maintenance" -> "Maintenance"
                "resurvey" -> "Resurvey"
                else -> flavorName.replaceFirstChar { it.uppercase() }
            }

            // Only rename release builds - use base version name without suffixes
            if (buildType.name == "release") {
                val baseVersionName = defaultConfig.versionName
                output.outputFileName = "${variantName}_v${baseVersionName}.apk"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"

        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/license.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/notice.txt"
            excludes += "/META-INF/*.kotlin_module"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        }
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.material.icons.extended)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Ktor Client
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // kotlinx-serialization
    implementation(libs.kotlinx.serialization.json)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // ArcGIS Maps SDK
    implementation(libs.arcgis.maps.kotlin)

    // ArcGIS Toolkit for Compose
    implementation(platform(libs.arcgis.toolkit.bom))
    implementation(libs.arcgis.toolkit.geoview)

    // Coil - Image Loading
    implementation(libs.coil.compose)

    // Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Testing - Unit Tests
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    // Testing - Instrumented Tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}