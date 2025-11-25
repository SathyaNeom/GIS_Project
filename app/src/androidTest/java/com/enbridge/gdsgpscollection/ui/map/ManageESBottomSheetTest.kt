package com.enbridge.gdsgpscollection.ui.map

/**
 * @author Sathya Narayanan
 */

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
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
                    onPostDataSnackbar = {},
                    initialViewpoint = null,
                    onRestoreViewpoint = {}
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
                    onPostDataSnackbar = {},
                    initialViewpoint = null,
                    onRestoreViewpoint = {}
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
                    onPostDataSnackbar = {},
                    initialViewpoint = null,
                    onRestoreViewpoint = {}
                )
            }
        }

        // Then - Distance dropdown should be present
        composeTestRule
            .onNodeWithText("Select distance")
            .assertIsDisplayed()
    }

    @Test
    fun bottomSheet_shouldDisplayGetDataButton() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                ManageESBottomSheet(
                    onDismissRequest = {},
                    onPostDataSnackbar = {},
                    initialViewpoint = null,
                    onRestoreViewpoint = {}
                )
            }
        }

        // Then - Get Data button should be displayed but disabled (no distance selected)
        composeTestRule
            .onNodeWithText("Get Data")
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun bottomSheet_shouldDisplayDataChangedSection() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                ManageESBottomSheet(
                    onDismissRequest = {},
                    onPostDataSnackbar = {},
                    initialViewpoint = null,
                    onRestoreViewpoint = {}
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
                    onPostDataSnackbar = {},
                    initialViewpoint = null,
                    onRestoreViewpoint = {}
                )
            }
        }

        // Then - Delete JC button should be disabled (no job card selected)
        composeTestRule
            .onNodeWithText("Delete JC")
            .assertIsDisplayed()
            .assertIsNotEnabled()

        // And - Post Data button should be disabled (no job card selected)
        composeTestRule
            .onNodeWithText("Post Data")
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun bottomSheet_shouldDisplayCloseButton() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                ManageESBottomSheet(
                    onDismissRequest = {},
                    onPostDataSnackbar = {},
                    initialViewpoint = null,
                    onRestoreViewpoint = {}
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
                    onPostDataSnackbar = {},
                    initialViewpoint = null,
                    onRestoreViewpoint = {}
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
    fun postDataButton_shouldBeDisabledWhenNoJobCardSelected() {
        // Given - No job card selected
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                ManageESBottomSheet(
                    onDismissRequest = {},
                    onPostDataSnackbar = {},
                    initialViewpoint = null,
                    onRestoreViewpoint = {}
                )
            }
        }

        // Then - Post Data button should be disabled
        composeTestRule
            .onNodeWithText("Post Data")
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun deleteJCButton_shouldShowDialog() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                ManageESBottomSheet(
                    onDismissRequest = {},
                    onPostDataSnackbar = {},
                    initialViewpoint = null,
                    onRestoreViewpoint = {}
                )
            }
        }

        // When - Try to click Delete JC (will be disabled, but we test dialog logic)
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
    fun getDataButton_shouldBeDisabledWhenNoDistanceSelected() {
        // Given - No distance selected initially
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                ManageESBottomSheet(
                    onDismissRequest = {},
                    onPostDataSnackbar = {},
                    initialViewpoint = null,
                    onRestoreViewpoint = {}
                )
            }
        }

        // Then - Get Data button should be disabled
        composeTestRule
            .onNodeWithText("Get Data")
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun distanceDropdown_shouldShowPlaceholder() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                ManageESBottomSheet(
                    onDismissRequest = {},
                    onPostDataSnackbar = {},
                    initialViewpoint = null,
                    onRestoreViewpoint = {}
                )
            }
        }

        // Then - Distance dropdown should show placeholder text
        composeTestRule
            .onNodeWithText("Select distance")
            .assertIsDisplayed()
    }
}
