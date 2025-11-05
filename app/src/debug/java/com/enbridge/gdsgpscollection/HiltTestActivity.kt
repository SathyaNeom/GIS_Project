package com.enbridge.gdsgpscollection

/**
 * @author Sathya Narayanan
 */
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Test activity for Hilt-enabled Compose UI tests.
 * This activity is annotated with @AndroidEntryPoint to enable Hilt dependency injection in tests.
 *
 * **Important:** This activity is in the debug source set so it's part of the app process,
 * not the test process. This allows tests to launch it correctly.
 *
 * Use this activity with createAndroidComposeRule in your Compose UI tests:
 * ```
 * val composeTestRule = createAndroidComposeRule<HiltTestActivity>()
 * ```
 */
@AndroidEntryPoint
class HiltTestActivity : ComponentActivity()
