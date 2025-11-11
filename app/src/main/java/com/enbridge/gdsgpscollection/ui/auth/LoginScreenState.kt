package com.enbridge.gdsgpscollection.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * State holder for form field data.
 *
 * Manages the input values for the login form fields.
 *
 * @property username Username input value
 * @property password Password input value
 */
data class LoginFormState(
    val username: String = "", val password: String = ""
)

/**
 * State holder for form validation.
 *
 * Manages validation state and error messages for form fields.
 *
 * @property isUsernameValid True if username is valid
 * @property isPasswordValid True if password is valid
 * @property usernameError Error message for username field (null if valid)
 * @property passwordError Error message for password field (null if valid)
 */
data class LoginValidationState(
    val isUsernameValid: Boolean = true,
    val isPasswordValid: Boolean = true,
    val usernameError: String? = null,
    val passwordError: String? = null
)

/**
 * Centralized state holder for LoginScreen.
 *
 * This class consolidates all screen-level state into a single, stable holder
 * following Compose best practices and the pattern established in MainMapScreenState.
 *
 * ## Usage Example:
 * ```kotlin
 * @Composable
 * fun LoginScreen() {
 *     val screenState = rememberLoginScreenState()
 *
 *     // Access state
 *     val username = screenState.formState.username
 *     val isValid = screenState.isFormValid()
 *
 *     // Update state
 *     screenState.updateUsername("user@example.com")
 *     screenState.updatePassword("password123")
 * }
 * ```
 *
 * ## Design Rationale:
 * - **@Stable annotation**: Ensures Compose can optimize recomposition
 * - **Grouped state**: Related state variables are grouped into logical categories
 * - **Validation logic**: Encapsulates form validation rules
 * - **Convenience methods**: Simplify common state update patterns
 *
 * @property formState State for form field data
 * @property validationState State for form validation
 *
 * @author Sathya Narayanan
 */
@Stable
class LoginScreenState(
    formState: LoginFormState = LoginFormState(),
    validationState: LoginValidationState = LoginValidationState()
) {
    var formState by mutableStateOf(formState)
        private set

    var validationState by mutableStateOf(validationState)
        private set

    /**
     * Updates the username field.
     *
     * Automatically validates the username after updating.
     *
     * @param username New username value
     */
    fun updateUsername(username: String) {
        formState = formState.copy(username = username)
        validateUsername(username)
    }

    /**
     * Updates the password field.
     *
     * Automatically validates the password after updating.
     *
     * @param password New password value
     */
    fun updatePassword(password: String) {
        formState = formState.copy(password = password)
        validatePassword(password)
    }

    /**
     * Validates the username field.
     *
     * Validation rules:
     * - Must not be blank
     *
     * @param username Username to validate
     */
    private fun validateUsername(username: String) {
        // Only mark as invalid if user has typed something that doesn't meet criteria
        // Empty field should not be marked invalid initially
        val isValid = when {
            username.isEmpty() -> true // Empty is valid (user hasn't typed yet)
//            username.length < 3 -> false // Too short
            else -> true
        }

        val error = when {
            username.isNotEmpty() /*&& username.length < 3*/ -> "Username cannot be empty"/*"Username must be at least 3 characters"*/
            else -> null
        }

        validationState = validationState.copy(
            isUsernameValid = isValid, usernameError = error
        )
    }

    /**
     * Validates the password field.
     *
     * Validation rules:
     * - Must not be blank
     *
     * @param password Password to validate
     */
    private fun validatePassword(password: String) {
        // Only mark as invalid if user has typed something that doesn't meet criteria
        // Empty field should not be marked invalid initially
        val isValid = when {
            password.isEmpty() -> true // Empty is valid (user hasn't typed yet)
            /*password.length < 6 -> false // Too short*/
            else -> true
        }

        val error = when {
            password.isNotEmpty() /*&& password.length < 6*/ -> "Password cannot be empty"/*"Password must be at least 6 characters"*/
            else -> null
        }

        validationState = validationState.copy(
            isPasswordValid = isValid, passwordError = error
        )
    }

    /**
     * Checks if the entire form is valid and ready for submission.
     *
     * @return True if all fields are valid and not empty
     */
    fun isFormValid(): Boolean {
        return formState.username.isNotBlank() && formState.password.isNotBlank() && validationState.isUsernameValid && validationState.isPasswordValid
    }

    /**
     * Resets the form to initial empty state.
     *
     * Useful after successful login or when screen is dismissed.
     */
    fun resetForm() {
        formState = LoginFormState()
        validationState = LoginValidationState()
    }

    /**
     * Clears all validation errors.
     * It's available for future enhancements where you want to clear validation errors without resetting the entire form.
     * Useful when transitioning from error state or before revalidation.
     */
    fun clearValidationErrors() {
        validationState = LoginValidationState()
    }
}

/**
 * Creates and remembers a LoginScreenState instance.
 *
 * This function should be called at the top of the LoginScreen composable
 * to create a stable state holder that survives recomposition.
 *
 * ## Usage:
 * ```kotlin
 * @Composable
 * fun LoginScreen() {
 *     val screenState = rememberLoginScreenState()
 *     // Use screenState throughout the composable
 * }
 * ```
 *
 * @return A remembered LoginScreenState instance
 */
@Composable
fun rememberLoginScreenState(): LoginScreenState {
    return remember { LoginScreenState() }
}
