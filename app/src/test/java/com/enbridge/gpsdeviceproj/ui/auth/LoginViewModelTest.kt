package com.enbridge.gpsdeviceproj.ui.auth

/**
 * @author Sathya Narayanan
 */

import app.cash.turbine.test
import com.enbridge.gpsdeviceproj.domain.entity.User
import com.enbridge.gpsdeviceproj.domain.usecase.LoginUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for LoginViewModel
 *
 * Tests authentication view model operations ensuring proper handling of:
 * - Login success and failure scenarios
 * - Loading states
 * - Error handling and display
 * - State management throughout login flow
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var viewModel: LoginViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        loginUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): LoginViewModel {
        return LoginViewModel(loginUseCase)
    }

    @Test
    fun `initial state should have default values`() = runTest {
        // When
        viewModel = createViewModel()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertFalse(state.loginSuccess)
    }

    @Test
    fun `login should update state with loading and success when credentials are valid`() =
        runTest {
            // Given
            val username = "testuser"
            val password = "testpass"
            val user = User(
                id = "123",
                username = username,
                token = "auth_token"
            )
            coEvery { loginUseCase(username, password) } returns Result.success(user)
            viewModel = createViewModel()

            // When
            viewModel.login(username, password)
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertTrue(state.loginSuccess)
            assertNull(state.error)

            coVerify(exactly = 1) {
                loginUseCase(username, password)
            }
        }

    @Test
    fun `login should update state with loading and error when credentials are invalid`() =
        runTest {
            // Given
            val username = "invaliduser"
            val password = "wrongpass"
            val exception = Exception("Invalid credentials")
            coEvery { loginUseCase(username, password) } returns Result.failure(exception)
            viewModel = createViewModel()

            // When
            viewModel.login(username, password)
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertFalse(state.loginSuccess)
            assertEquals("Invalid credentials", state.error)

            coVerify(exactly = 1) {
                loginUseCase(username, password)
            }
        }

    @Test
    fun `login should emit loading state during authentication`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val user = User("123", "testuser", "token")
        coEvery { loginUseCase(username, password) } returns Result.success(user)
        viewModel = createViewModel()

        // When & Then
        viewModel.uiState.test {
            // Initial state
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)

            viewModel.login(username, password)
            testDispatcher.scheduler.advanceUntilIdle()

            // Loading state
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            assertNull(loadingState.error)

            // Success state
            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertTrue(successState.loginSuccess)
            assertNull(successState.error)
        }
    }

    @Test
    fun `login should handle network errors`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val exception = Exception("Network connection failed")
        coEvery { loginUseCase(username, password) } returns Result.failure(exception)
        viewModel = createViewModel()

        // When
        viewModel.login(username, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.loginSuccess)
        assertEquals("Network connection failed", state.error)
    }

    @Test
    fun `login should handle server errors`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val exception = Exception("Server error: 500")
        coEvery { loginUseCase(username, password) } returns Result.failure(exception)
        viewModel = createViewModel()

        // When
        viewModel.login(username, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("Server error: 500", state.error)
    }

    @Test
    fun `login should handle exception without message`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val exception = Exception()
        coEvery { loginUseCase(username, password) } returns Result.failure(exception)
        viewModel = createViewModel()

        // When
        viewModel.login(username, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("Login failed", state.error)
    }

    @Test
    fun `clearError should remove error from state`() = runTest {
        // Given
        val username = "invaliduser"
        val password = "wrongpass"
        val exception = Exception("Invalid credentials")
        coEvery { loginUseCase(username, password) } returns Result.failure(exception)
        viewModel = createViewModel()

        viewModel.login(username, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify error is present
        assertTrue(viewModel.uiState.value.error != null)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `login should clear previous error before new attempt`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val firstException = Exception("First error")
        val user = User("123", "testuser", "token")

        coEvery { loginUseCase(username, password) } returns Result.failure(firstException)
        viewModel = createViewModel()

        // First failed login
        viewModel.login(username, password)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("First error", viewModel.uiState.value.error)

        // Setup for successful login
        coEvery { loginUseCase(username, password) } returns Result.success(user)

        // When - Second login attempt
        viewModel.login(username, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Error should be cleared
        assertNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.loginSuccess)
    }

    @Test
    fun `multiple login attempts should be handled independently`() = runTest {
        // Given
        viewModel = createViewModel()
        val username = "testuser"
        val password = "testpass"

        // First attempt - failure
        val firstException = Exception("First attempt failed")
        coEvery { loginUseCase(username, password) } returns Result.failure(firstException)

        viewModel.login(username, password)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("First attempt failed", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.loginSuccess)

        // Second attempt - success
        val user = User("123", "testuser", "token")
        coEvery { loginUseCase(username, password) } returns Result.success(user)

        viewModel.login(username, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.loginSuccess)

        coVerify(exactly = 2) {
            loginUseCase(username, password)
        }
    }

    @Test
    fun `login should handle empty credentials with error`() = runTest {
        // Given
        val username = ""
        val password = ""
        val exception = Exception("Username and password cannot be empty")
        coEvery { loginUseCase(username, password) } returns Result.failure(exception)
        viewModel = createViewModel()

        // When
        viewModel.login(username, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.loginSuccess)
        assertEquals("Username and password cannot be empty", state.error)
    }

    @Test
    fun `login should handle special characters in credentials`() = runTest {
        // Given
        val username = "user@example.com"
        val password = "P@ssw0rd!#$"
        val user = User("123", username, "token")
        coEvery { loginUseCase(username, password) } returns Result.success(user)
        viewModel = createViewModel()

        // When
        viewModel.login(username, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.loginSuccess)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `state should remain immutable during operations`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val user = User("123", "testuser", "token")
        coEvery { loginUseCase(username, password) } returns Result.success(user)
        viewModel = createViewModel()

        // When
        val stateBeforeLogin = viewModel.uiState.value
        viewModel.login(username, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Original state should not be modified
        assertFalse(stateBeforeLogin.isLoading)
        assertFalse(stateBeforeLogin.loginSuccess)
        assertNull(stateBeforeLogin.error)
    }
}
