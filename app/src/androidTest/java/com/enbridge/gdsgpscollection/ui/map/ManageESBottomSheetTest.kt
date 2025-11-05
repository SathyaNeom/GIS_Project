package com.enbridge.gdsgpscollection.ui.map

/**
 * @author Sathya Narayanan
 */

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.enbridge.gdsgpscollection.HiltTestActivity
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme
import com.enbridge.gdsgpscollection.ui.map.components.ManageESBottomSheet
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for ManageESBottomSheet
 * Tests user interface components and interactions using Compose Testing and Espresso
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ManageESBottomSheetTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun bottomSheet_shouldDisplayTitle() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                ManageESBottomSheet(
                    onDismissRequest = {},
                    onPostDataSnackbar = {}
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Get data or Post Data")
            .assertIsDisplayed()
    }

    @Test
    fun bottomSheet_shouldDisplaySubtitle() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                ManageESBottomSheet(
                    onDismissRequest = {},
                    onPostDataSnackbar = {}
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Get Data - Select a Distance around where you are standing")
            .assertIsDisplayed()
    }

    @Test
    fun bottomSheet_shouldDisplayDistanceDropdown() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                ManageESBottomSheet(
                    onDismissRequest = {},
                    onPostDataSnackbar = {}
                )
            }
        }

        // Then - Distance dropdown should be present
        composeTestRule
            .onNodeWithText("Distance")
            .assertIsDisplayed()
    }

    @Test
    fun bottomSheet_shouldDisplayGetDataButton() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                ManageESBottomSheet(
                    onDismissRequest = {},
                    onPostDataSnackbar = {}
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Get Data")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun bottomSheet_shouldDisplayDataChangedSection() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                ManageESBottomSheet(
                    onDismissRequest = {},
                    onPostDataSnackbar = {}
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Data Changed:")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("No data changes")
            .assertIsDisplayed()
    }

    @Test
    fun bottomSheet_shouldDisplayActionButtons() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                ManageESBottomSheet(
                    onDismissRequest = {},
                    onPostDataSnackbar = {}
                )
            }
        }

        // Then - Delete JC button
        composeTestRule
            .onNodeWithText("Delete JC")
            .assertIsDisplayed()
            .assertIsEnabled()

        // And - Post Data button
        composeTestRule
            .onNodeWithText("Post Data")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun bottomSheet_shouldDisplayCloseButton() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                ManageESBottomSheet(
                    onDismissRequest = {},
                    onPostDataSnackbar = {}
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithContentDescription("Close")
            .assertIsDisplayed()
    }

    @Test
    fun closeButton_shouldTriggerDismissCallback() {
        // Given
        var dismissCalled = false
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                ManageESBottomSheet(
                    onDismissRequest = { dismissCalled = true },
                    onPostDataSnackbar = {}
                )
            }
        }

        // When
        composeTestRule
            .onNodeWithContentDescription("Close")
            .performClick()

        // Then
        assert(dismissCalled)
    }

    @Test
    fun postDataButton_shouldTriggerSnackbarCallback() {
        // Given
        var snackbarCalled = false
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                ManageESBottomSheet(
                    onDismissRequest = {},
                    onPostDataSnackbar = { snackbarCalled = true }
                )
            }
        }

        // When
        composeTestRule
            .onNodeWithText("Post Data")
            .performClick()

        // Then
        assert(snackbarCalled)
    }

    @Test
    fun deleteJCButton_shouldShowDialog() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                ManageESBottomSheet(
                    onDismissRequest = {},
                    onPostDataSnackbar = {}
                )
            }
        }

        // When
        composeTestRule
            .onNodeWithText("Delete JC")
            .performClick()

        // Wait for dialog to appear
        composeTestRule.waitForIdle()

        // Then - Dialog should be displayed
        composeTestRule
            .onNodeWithText("Delete Job Card Info")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("No Job Card has been saved")
            .assertIsDisplayed()
    }

    @Test
    fun getDataButton_shouldShowProgressDialog() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                ManageESBottomSheet(
                    onDismissRequest = {},
                    onPostDataSnackbar = {}
                )
            }
        }

        // When
        composeTestRule
            .onNodeWithText("Get Data")
            .performClick()

        // Wait for download to start
        composeTestRule.waitForIdle()

        // Then - Progress dialog should appear
        composeTestRule
            .onNodeWithText("Downloading Data")
            .assertIsDisplayed()
    }

    @Test
    fun distanceDropdown_shouldShowAllOptions() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                ManageESBottomSheet(
                    onDismissRequest = {},
                    onPostDataSnackbar = {}
                )
            }
        }

        // When - Click on distance dropdown
        composeTestRule
            .onNodeWithText("100 Meters") // Default selected value
            .performClick()

        composeTestRule.waitForIdle()

        // Then - All distance options should be visible
        composeTestRule
            .onNodeWithText("50 Meters")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("100 Meters")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("500 Meters")
            .assertIsDisplayed()
    }
}
