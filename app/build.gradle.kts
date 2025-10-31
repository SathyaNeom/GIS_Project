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
}

android {
    namespace = "com.enbridge.electronicservices"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.enbridge.electronicservices"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
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
        }
    }
}

dependencies {
    // Domain and Data modules
    implementation(project(":domain"))
    implementation(project(":data"))

    // Feature modules
    implementation(project(":core"))
    implementation(project(":design-system"))
    implementation(project(":feature_auth"))
    implementation(project(":feature_map"))
    implementation(project(":feature_jobs"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // ArcGIS Maps SDK (for ArcGISEnvironment)
    implementation(libs.arcgis.maps.kotlin)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}