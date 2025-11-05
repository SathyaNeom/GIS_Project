package com.enbridge.gpsdeviceproj.ui.jobs

/**
 * @author Sathya Narayanan
 */

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication
import kotlin.jvm.java

/**
 * Custom test runner that uses Hilt's test application.
 * This allows us to use Hilt dependency injection in our tests.
 *
 * To use this runner, add it to your module's build.gradle:
 * android {
 *     defaultConfig {
 *         testInstrumentationRunner = "com.enbridge.gpsdeviceproj.ui.jobs.HiltTestRunner"
 *     }
 * }
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
