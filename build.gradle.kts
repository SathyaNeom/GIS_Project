/**
 * @author Sathya Narayanan
 */
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    id("jacoco")
}

// Jacoco configuration for unified test coverage reporting
jacoco {
    toolVersion = "0.8.11"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "Reporting"
    description = "Generate unified Jacoco coverage report for all modules"

    dependsOn(
        ":domain:testDebugUnitTest",
        ":data:testDebugUnitTest",
        ":feature_auth:testElectronicDebugUnitTest",
        ":feature_jobs:testDebugUnitTest",
        ":feature_map:testDebugUnitTest",
        ":core:testDebugUnitTest"
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)

        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/jacocoTestReport.xml"))
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/html"))
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/di/**",
        "**/hilt/**",
        "**/*_HiltModules*",
        "**/*_Factory*",
        "**/*_MembersInjector*",
        "**/*Module*",
        "**/*Dagger*",
        "**/*_Provide*Factory*",
        "**/*_Hilt*"
    )

    val javaTree = fileTree("${project.rootDir}") {
        include(
            "**/domain/build/intermediates/javac/debug/classes/**",
            "**/data/build/intermediates/javac/debug/classes/**",
            "**/feature_auth/build/intermediates/javac/electronicDebug/classes/**",
            "**/feature_jobs/build/intermediates/javac/debug/classes/**",
            "**/feature_map/build/intermediates/javac/debug/classes/**",
            "**/core/build/intermediates/javac/debug/classes/**"
        )
        exclude(fileFilter)
    }

    val kotlinTree = fileTree("${project.rootDir}") {
        include(
            "**/domain/build/tmp/kotlin-classes/debug/**",
            "**/data/build/tmp/kotlin-classes/debug/**",
            "**/feature_auth/build/tmp/kotlin-classes/electronicDebug/**",
            "**/feature_jobs/build/tmp/kotlin-classes/debug/**",
            "**/feature_map/build/tmp/kotlin-classes/debug/**",
            "**/core/build/tmp/kotlin-classes/debug/**"
        )
        exclude(fileFilter)
    }

    classDirectories.setFrom(files(javaTree, kotlinTree))

    val sourceDirs = files(
        "${project.rootDir}/domain/src/main/java",
        "${project.rootDir}/data/src/main/java",
        "${project.rootDir}/feature_auth/src/main/java",
        "${project.rootDir}/feature_jobs/src/main/java",
        "${project.rootDir}/feature_map/src/main/java",
        "${project.rootDir}/core/src/main/java"
    )

    sourceDirectories.setFrom(sourceDirs)

    val executionDataFiles = fileTree("${project.rootDir}") {
        include(
            "**/domain/build/jacoco/testDebugUnitTest.exec",
            "**/data/build/jacoco/testDebugUnitTest.exec",
            "**/feature_auth/build/jacoco/testElectronicDebugUnitTest.exec",
            "**/feature_jobs/build/jacoco/testDebugUnitTest.exec",
            "**/feature_map/build/jacoco/testDebugUnitTest.exec",
            "**/core/build/jacoco/testDebugUnitTest.exec"
        )
    }

    executionData.setFrom(executionDataFiles)
}

// Coverage verification task
tasks.register<JacocoCoverageVerification>("jacocoCoverageVerification") {
    group = "Verification"
    description = "Verify code coverage meets minimum thresholds"

    dependsOn("jacocoTestReport")

    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal() // 80% minimum coverage
            }
        }

        rule {
            element = "CLASS"
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.70".toBigDecimal() // 70% branch coverage
            }
        }
    }
}