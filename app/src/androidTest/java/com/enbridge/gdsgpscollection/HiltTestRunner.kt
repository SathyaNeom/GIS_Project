package com.enbridge.gdsgpscollection

/**
 * @author Sathya Narayanan
 */

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Custom test runner for Hilt instrumented tests.
 * This ensures that Hilt's test application is used instead of the production application
 * during instrumented tests, allowing for proper dependency injection in tests.
 *
 * Configured in build.gradle.kts:
 * testInstrumentationRunner = "com.enbridge.gdsgpscollection.HiltTestRunner"
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
