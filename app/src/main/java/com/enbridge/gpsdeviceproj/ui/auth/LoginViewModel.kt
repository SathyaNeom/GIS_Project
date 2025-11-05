package com.enbridge.gpsdeviceproj.ui.auth

/**
 * @author Sathya Narayanan
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enbridge.gpsdeviceproj.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the login screen.
 *
 * @property isLoading Indicates whether a login operation is in progress
 * @property error Error message to display to the user, null if no error
 * @property loginSuccess Indicates whether the login was successful
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false
)

/**
 * ViewModel for managing login screen state and business logic.
 *
 * This ViewModel handles:
 * - User authentication via the LoginUseCase
 * - UI state management (loading, error, success states)
 * - Error handling and user feedback
 *
 * The ViewModel exposes a StateFlow of LoginUiState which the UI observes
 * to update its appearance and behavior accordingly.
 *
 * @property loginUseCase Use case for performing login operations
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Attempts to authenticate a user with the provided credentials.
     *
     * This method:
     * - Sets the loading state to true
     * - Invokes the login use case with the provided credentials
     * - Updates the UI state based on success or failure
     * - Handles any exceptions that occur during the process
     *
     * @param username The user's username
     * @param password The user's password
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            loginUseCase(username, password)
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loginSuccess = true,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loginSuccess = false,
                            error = exception.message ?: "Login failed"
                        )
                    }
                }
        }
    }

    /**
     * Clears any error message from the UI state.
     *
     * This should be called after the error has been displayed to the user
     * to prevent the error from being shown again on configuration changes.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
