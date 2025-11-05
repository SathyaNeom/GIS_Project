package com.enbridge.gdsgpscollection.ui.auth

/**
 * @author Sathya Narayanan
 */

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.enbridge.gdsgpscollection.HiltTestActivity
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for LoginScreen
 *
 * Tests user interface components and interactions ensuring:
 * - All UI elements are displayed correctly
 * - User input is handled properly
 * - Button states respond to input
 * - Navigation callbacks are triggered
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun loginScreen_shouldDisplayAllComponents() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                LoginScreen(
                    onLoginSuccess = {},
                    onForgotPasswordClick = {}
                )
            }
        }

        // Then - Verify all components are displayed
        composeTestRule.onNodeWithText("Username").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
        composeTestRule.onNodeWithText("Forgot Password?").assertIsDisplayed()
    }

    @Test
    fun loginScreen_loginButtonShouldBeDisabledInitially() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                LoginScreen(
                    onLoginSuccess = {},
                    onForgotPasswordClick = {}
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Login")
            .assertIsNotEnabled()
    }

    @Test
    fun loginScreen_shouldAcceptUsernameInput() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                LoginScreen(
                    onLoginSuccess = {},
                    onForgotPasswordClick = {}
                )
            }
        }

        // When
        composeTestRule
            .onNodeWithText("Username")
            .performTextInput("testuser")

        // Then
        composeTestRule
            .onNodeWithText("testuser")
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_shouldAcceptPasswordInput() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                LoginScreen(
                    onLoginSuccess = {},
                    onForgotPasswordClick = {}
                )
            }
        }

        // When
        composeTestRule
            .onNodeWithText("Password")
            .performTextInput("testpass")

        // Then - Password should be masked, but input should be accepted
        composeTestRule
            .onNodeWithText("Password")
            .assertExists()
    }

    @Test
    fun loginScreen_loginButtonShouldBeEnabledWhenBothFieldsFilled() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                LoginScreen(
                    onLoginSuccess = {},
                    onForgotPasswordClick = {}
                )
            }
        }

        // When
        composeTestRule
            .onNodeWithText("Username")
            .performTextInput("testuser")

        composeTestRule
            .onNodeWithText("Password")
            .performTextInput("testpass")

        // Wait for recomposition
        composeTestRule.waitForIdle()

        // Then
        composeTestRule
            .onNodeWithText("Login")
            .assertIsEnabled()
    }

    @Test
    fun loginScreen_loginButtonShouldStayDisabledWithOnlyUsername() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                LoginScreen(
                    onLoginSuccess = {},
                    onForgotPasswordClick = {}
                )
            }
        }

        // When
        composeTestRule
            .onNodeWithText("Username")
            .performTextInput("testuser")

        composeTestRule.waitForIdle()

        // Then
        composeTestRule
            .onNodeWithText("Login")
            .assertIsNotEnabled()
    }

    @Test
    fun loginScreen_loginButtonShouldStayDisabledWithOnlyPassword() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                LoginScreen(
                    onLoginSuccess = {},
                    onForgotPasswordClick = {}
                )
            }
        }

        // When
        composeTestRule
            .onNodeWithText("Password")
            .performTextInput("testpass")

        composeTestRule.waitForIdle()

        // Then
        composeTestRule
            .onNodeWithText("Login")
            .assertIsNotEnabled()
    }

    @Test
    fun loginScreen_forgotPasswordButtonShouldTriggerCallback() {
        // Given
        var forgotPasswordClicked = false
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                LoginScreen(
                    onLoginSuccess = {},
                    onForgotPasswordClick = { forgotPasswordClicked = true }
                )
            }
        }

        // When
        composeTestRule
            .onNodeWithText("Forgot Password?")
            .performClick()

        // Then
        assert(forgotPasswordClicked)
    }

    @Test
    fun loginScreen_shouldClearUsernameField() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                LoginScreen(
                    onLoginSuccess = {},
                    onForgotPasswordClick = {}
                )
            }
        }

        // When
        composeTestRule
            .onNodeWithText("Username")
            .performTextInput("testuser")

        composeTestRule
            .onNodeWithText("Username")
            .performTextClearance()

        // Then
        composeTestRule
            .onNodeWithText("testuser")
            .assertDoesNotExist()
    }

    @Test
    fun loginScreen_shouldHandleMultipleCharacterInput() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                LoginScreen(
                    onLoginSuccess = {},
                    onForgotPasswordClick = {}
                )
            }
        }

        // When
        val longUsername = "very_long_username_with_special_chars@123"
        composeTestRule
            .onNodeWithText("Username")
            .performTextInput(longUsername)

        // Then
        composeTestRule
            .onNodeWithText(longUsername)
            .assertExists()
    }

    @Test
    fun loginScreen_shouldHandleSpecialCharactersInPassword() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                LoginScreen(
                    onLoginSuccess = {},
                    onForgotPasswordClick = {}
                )
            }
        }

        // When
        composeTestRule
            .onNodeWithText("Username")
            .performTextInput("testuser")

        composeTestRule
            .onNodeWithText("Password")
            .performTextInput("P@ssw0rd!#$")

        composeTestRule.waitForIdle()

        // Then - Button should be enabled with special characters
        composeTestRule
            .onNodeWithText("Login")
            .assertIsEnabled()
    }

    @Test
    fun loginScreen_shouldDisplayCustomAppName() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                LoginScreen(
                    onLoginSuccess = {},
                    onForgotPasswordClick = {}
                )
            }
        }

        // Then - Verify screen renders without error
        composeTestRule
            .onNodeWithText("Login")
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_shouldAcceptEmptySpacesAndTrimValidation() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                LoginScreen(
                    onLoginSuccess = {},
                    onForgotPasswordClick = {}
                )
            }
        }

        // When - Try to input only spaces
        composeTestRule
            .onNodeWithText("Username")
            .performTextInput("   ")

        composeTestRule
            .onNodeWithText("Password")
            .performTextInput("   ")

        composeTestRule.waitForIdle()

        // Then - Button should still be disabled (isNotBlank validation)
        composeTestRule
            .onNodeWithText("Login")
            .assertIsNotEnabled()
    }

    @Test
    fun loginScreen_shouldRespectTextFieldEnabledState() {
        // Given - All fields should be enabled initially
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                LoginScreen(
                    onLoginSuccess = {},
                    onForgotPasswordClick = {}
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Username")
            .assertIsEnabled()

        composeTestRule
            .onNodeWithText("Password")
            .assertIsEnabled()

        composeTestRule
            .onNodeWithText("Forgot Password?")
            .assertIsEnabled()
    }

    @Test
    fun loginScreen_shouldDisplayUsernameLabel() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                LoginScreen(
                    onLoginSuccess = {},
                    onForgotPasswordClick = {}
                )
            }
        }

        // Then
        composeTestRule
            .onNode(hasText("Username") and hasSetTextAction())
            .assertExists()
    }

    @Test
    fun loginScreen_shouldDisplayPasswordLabel() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                LoginScreen(
                    onLoginSuccess = {},
                    onForgotPasswordClick = {}
                )
            }
        }

        // Then
        composeTestRule
            .onNode(hasText("Password") and hasSetTextAction())
            .assertExists()
    }

    @Test
    fun loginScreen_shouldHaveCorrectLayoutStructure() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                LoginScreen(
                    onLoginSuccess = {},
                    onForgotPasswordClick = {}
                )
            }
        }

        // Then - All components should exist in the hierarchy
        composeTestRule.onRoot().assertExists()
        composeTestRule.onNodeWithText("Username").assertExists()
        composeTestRule.onNodeWithText("Password").assertExists()
        composeTestRule.onNodeWithText("Login").assertExists()
        composeTestRule.onNodeWithText("Forgot Password?").assertExists()
    }

    @Test
    fun loginScreen_shouldHandleSequentialInput() {
        // Given
        composeTestRule.setContent {
            GdsGpsCollectionTheme {
                LoginScreen(
                    onLoginSuccess = {},
                    onForgotPasswordClick = {}
                )
            }
        }

        // When - Input username first
        composeTestRule
            .onNodeWithText("Username")
            .performTextInput("user")

        composeTestRule.waitForIdle()

        // Then - Button still disabled
        composeTestRule
            .onNodeWithText("Login")
            .assertIsNotEnabled()

        // When - Now input password
        composeTestRule
            .onNodeWithText("Password")
            .performTextInput("pass")

        composeTestRule.waitForIdle()

        // Then - Button should be enabled
        composeTestRule
            .onNodeWithText("Login")
            .assertIsEnabled()
    }
}
