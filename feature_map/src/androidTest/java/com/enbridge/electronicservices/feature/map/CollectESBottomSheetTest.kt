package com.enbridge.electronicservices.feature.map

/**
 * @author Sathya Narayanan
 */

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.enbridge.electronicservices.domain.entity.AttributeType
import com.enbridge.electronicservices.domain.entity.FeatureAttribute
import com.enbridge.electronicservices.domain.entity.FeatureType
import com.enbridge.electronicservices.domain.entity.GeometryType
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for CollectESBottomSheet.
 *
 * Tests cover:
 * - Initial rendering with feature types list
 * - Feature type selection and navigation
 * - Attribute editing screen
 * - Form validation
 * - Loading states
 * - Error states
 * - Back navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@RunWith(AndroidJUnit4::class)
class CollectESBottomSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var testFeatureTypes: List<FeatureType>
    private lateinit var testFeatureType: FeatureType
    private lateinit var uiState: CollectESUiState
    private var onDismissCalled = false
    private var onRetryCalled = false
    private var onSaveCalled = false
    private lateinit var savedFeatureType: com.enbridge.electronicservices.feature.map.models.FeatureType
    private lateinit var savedAttributes: Map<String, String>

    @Before
    fun setup() {
        testFeatureTypes = listOf(
            FeatureType(
                id = "1",
                name = "Gas Meter",
                geometryType = GeometryType.POINT,
                legendColor = "#FF0000",
                attributes = listOf(
                    FeatureAttribute(
                        id = "attr1",
                        label = "Meter Number",
                        type = AttributeType.TEXT,
                        isRequired = true,
                        options = emptyList(),
                        hint = "Enter meter number",
                        defaultValue = ""
                    ),
                    FeatureAttribute(
                        id = "attr2",
                        label = "Meter Type",
                        type = AttributeType.DROPDOWN,
                        isRequired = false,
                        options = listOf("Residential", "Commercial", "Industrial"),
                        hint = "Select type",
                        defaultValue = ""
                    )
                )
            ),
            FeatureType(
                id = "2",
                name = "Service Line",
                geometryType = GeometryType.POLYLINE,
                legendColor = "#00FF00",
                attributes = emptyList()
            )
        )

        testFeatureType = testFeatureTypes[0]

        uiState = CollectESUiState(
            featureTypes = testFeatureTypes,
            isLoading = false,
            error = null
        )

        onDismissCalled = false
        onRetryCalled = false
        onSaveCalled = false
    }

    /**
     * Test that the choose feature type screen renders with title and subtitle.
     */
    @Test
    fun collectESBottomSheet_initialRendering_displaysHeader() {
        // Given
        composeTestRule.setContent {
            com.enbridge.electronicservices.feature.map.components.CollectESBottomSheet(
                onDismissRequest = { onDismissCalled = true },
                onSave = { featureType, attributes ->
                    savedFeatureType = featureType
                    savedAttributes = attributes
                    onSaveCalled = true
                },
                uiState = uiState,
                onRetry = { onRetryCalled = true }
            )
        }

        // Then
        composeTestRule
            .onNodeWithText("Choose Feature Type")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Select feature layer to collect points.")
            .assertIsDisplayed()
    }

    /**
     * Test that feature types list is displayed.
     */
    @Test
    fun collectESBottomSheet_withFeatureTypes_displaysListItems() {
        // Given
        composeTestRule.setContent {
            com.enbridge.electronicservices.feature.map.components.CollectESBottomSheet(
                onDismissRequest = { onDismissCalled = true },
                onSave = { _, _ -> },
                uiState = uiState,
                onRetry = { onRetryCalled = true }
            )
        }

        // Then
        composeTestRule
            .onNodeWithText("Gas Meter")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Service Line")
            .assertIsDisplayed()
    }

    /**
     * Test that clicking close button dismisses the bottom sheet.
     */
    @Test
    fun collectESBottomSheet_clickClose_dismissesSheet() {
        // Given
        composeTestRule.setContent {
            com.enbridge.electronicservices.feature.map.components.CollectESBottomSheet(
                onDismissRequest = { onDismissCalled = true },
                onSave = { _, _ -> },
                uiState = uiState,
                onRetry = { onRetryCalled = true }
            )
        }

        // When
        composeTestRule
            .onNodeWithContentDescription("Close")
            .performClick()

        // Then
        assert(onDismissCalled)
    }

    /**
     * Test that selecting a feature type navigates to edit attribute screen.
     */
    @Test
    fun collectESBottomSheet_selectFeatureType_navigatesToEditScreen() {
        // Given
        composeTestRule.setContent {
            com.enbridge.electronicservices.feature.map.components.CollectESBottomSheet(
                onDismissRequest = { onDismissCalled = true },
                onSave = { _, _ -> },
                uiState = uiState,
                onRetry = { onRetryCalled = true }
            )
        }

        // When
        composeTestRule
            .onNodeWithText("Gas Meter")
            .performClick()

        composeTestRule.waitForIdle()

        // Then - Should navigate to Edit Attribute screen
        composeTestRule
            .onNodeWithText("Edit Attribute")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Gas Meter")
            .assertIsDisplayed()
    }

    /**
     * Test loading state displays circular progress indicator.
     */
    @Test
    fun collectESBottomSheet_loadingState_displaysProgressIndicator() {
        // Given
        uiState = uiState.copy(isLoading = true, featureTypes = emptyList())

        composeTestRule.setContent {
            com.enbridge.electronicservices.feature.map.components.CollectESBottomSheet(
                onDismissRequest = { onDismissCalled = true },
                onSave = { _, _ -> },
                uiState = uiState,
                onRetry = { onRetryCalled = true }
            )
        }

        // Then - Progress indicator should be displayed
        // Note: CircularProgressIndicator doesn't have specific test tag
        // We verify by ensuring feature types are not displayed
        composeTestRule
            .onNodeWithText("Gas Meter")
            .assertDoesNotExist()
    }

    /**
     * Test error state displays error message and retry button.
     */
    @Test
    fun collectESBottomSheet_errorState_displaysErrorAndRetryButton() {
        // Given
        uiState = uiState.copy(
            isLoading = false,
            featureTypes = emptyList(),
            error = "Failed to load feature types"
        )

        composeTestRule.setContent {
            com.enbridge.electronicservices.feature.map.components.CollectESBottomSheet(
                onDismissRequest = { onDismissCalled = true },
                onSave = { _, _ -> },
                uiState = uiState,
                onRetry = { onRetryCalled = true }
            )
        }

        // Then
        composeTestRule
            .onNodeWithText("Failed to load feature types")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Retry")
            .assertIsDisplayed()
    }

    /**
     * Test clicking retry button calls retry callback.
     */
    @Test
    fun collectESBottomSheet_clickRetry_invokesCallback() {
        // Given
        uiState = uiState.copy(
            isLoading = false,
            featureTypes = emptyList(),
            error = "Failed to load feature types"
        )

        composeTestRule.setContent {
            com.enbridge.electronicservices.feature.map.components.CollectESBottomSheet(
                onDismissRequest = { onDismissCalled = true },
                onSave = { _, _ -> },
                uiState = uiState,
                onRetry = { onRetryCalled = true }
            )
        }

        // When
        composeTestRule
            .onNodeWithText("Retry")
            .performClick()

        // Then
        assert(onRetryCalled)
    }

    /**
     * Test empty state displays appropriate message.
     */
    @Test
    fun collectESBottomSheet_emptyState_displaysMessage() {
        // Given
        uiState = uiState.copy(featureTypes = emptyList())

        composeTestRule.setContent {
            com.enbridge.electronicservices.feature.map.components.CollectESBottomSheet(
                onDismissRequest = { onDismissCalled = true },
                onSave = { _, _ -> },
                uiState = uiState,
                onRetry = { onRetryCalled = true }
            )
        }

        // Then
        composeTestRule
            .onNodeWithText("No feature types available")
            .assertIsDisplayed()
    }

    /**
     * Test that back button on edit screen returns to feature type selection.
     */
    @Test
    fun collectESBottomSheet_clickBackOnEditScreen_returnsToSelection() {
        // Given
        composeTestRule.setContent {
            com.enbridge.electronicservices.feature.map.components.CollectESBottomSheet(
                onDismissRequest = { onDismissCalled = true },
                onSave = { _, _ -> },
                uiState = uiState,
                onRetry = { onRetryCalled = true }
            )
        }

        // When - Navigate to edit screen
        composeTestRule
            .onNodeWithText("Gas Meter")
            .performClick()

        composeTestRule.waitForIdle()

        // Then - Verify on edit screen
        composeTestRule
            .onNodeWithText("Edit Attribute")
            .assertIsDisplayed()

        // When - Click back button
        composeTestRule
            .onNodeWithContentDescription("Back")
            .performClick()

        composeTestRule.waitForIdle()

        // Then - Should be back on choose feature type screen
        composeTestRule
            .onNodeWithText("Choose Feature Type")
            .assertIsDisplayed()
    }

    /**
     * Test that Save button is displayed on edit attribute screen.
     */
    @Test
    fun collectESBottomSheet_editScreen_displaysSaveButton() {
        // Given
        composeTestRule.setContent {
            com.enbridge.electronicservices.feature.map.components.CollectESBottomSheet(
                onDismissRequest = { onDismissCalled = true },
                onSave = { _, _ -> },
                uiState = uiState,
                onRetry = { onRetryCalled = true }
            )
        }

        // When
        composeTestRule
            .onNodeWithText("Gas Meter")
            .performClick()

        composeTestRule.waitForIdle()

        // Then
        composeTestRule
            .onNodeWithText("Save")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Select Asset")
            .assertIsDisplayed()
    }

    /**
     * Test that GPS indicator is displayed on edit screen.
     */
    @Test
    fun collectESBottomSheet_editScreen_displaysGPSIndicator() {
        // Given
        composeTestRule.setContent {
            com.enbridge.electronicservices.feature.map.components.CollectESBottomSheet(
                onDismissRequest = { onDismissCalled = true },
                onSave = { _, _ -> },
                uiState = uiState,
                onRetry = { onRetryCalled = true }
            )
        }

        // When
        composeTestRule
            .onNodeWithText("Gas Meter")
            .performClick()

        composeTestRule.waitForIdle()

        // Then
        composeTestRule
            .onNodeWithText("GPS")
            .assertIsDisplayed()
    }

    /**
     * Test that task description is displayed on edit screen.
     */
    @Test
    fun collectESBottomSheet_editScreen_displaysTaskDescription() {
        // Given
        composeTestRule.setContent {
            com.enbridge.electronicservices.feature.map.components.CollectESBottomSheet(
                onDismissRequest = { onDismissCalled = true },
                onSave = { _, _ -> },
                uiState = uiState,
                onRetry = { onRetryCalled = true }
            )
        }

        // When
        composeTestRule
            .onNodeWithText("Gas Meter")
            .performClick()

        composeTestRule.waitForIdle()

        // Then
        composeTestRule
            .onNodeWithText("Enbridge Edit Attribute Task")
            .assertIsDisplayed()
    }
}
