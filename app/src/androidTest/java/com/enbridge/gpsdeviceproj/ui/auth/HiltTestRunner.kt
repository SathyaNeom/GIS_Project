package com.enbridge.gpsdeviceproj.ui.auth

/**
 * @author Sathya Narayanan
 */

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Custom test runner for Hilt instrumented tests in feature_auth module.
 * This ensures that Hilt's test application is used for dependency injection during testing.
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
