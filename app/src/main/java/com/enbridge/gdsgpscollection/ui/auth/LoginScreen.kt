package com.enbridge.gdsgpscollection.ui.auth

/**
 * @author Sathya Narayanan
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.enbridge.gdsgpscollection.BuildConfig
import com.enbridge.gdsgpscollection.R
import com.enbridge.gdsgpscollection.designsystem.components.AppLogo
import com.enbridge.gdsgpscollection.designsystem.components.AppSnackbarHost
import com.enbridge.gdsgpscollection.designsystem.components.AppTextButton
import com.enbridge.gdsgpscollection.designsystem.components.AppTextField
import com.enbridge.gdsgpscollection.designsystem.components.LoadingView
import com.enbridge.gdsgpscollection.designsystem.components.PrimaryButton
import com.enbridge.gdsgpscollection.designsystem.components.SnackbarType
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val screenState = rememberLoginScreenState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            screenState.resetForm()
            onLoginSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    GdsGpsCollectionTheme {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.systemBars),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = Spacing.massive * 8)
                    .padding(Spacing.extraLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AppLogo()

                Spacer(modifier = Modifier.height(Spacing.huge))

                AppTextField(
                    value = screenState.formState.username,
                    onValueChange = { screenState.updateUsername(it) },
                    label = stringResource(R.string.login_username_label),
                    placeholder = stringResource(R.string.login_username_placeholder),
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.normal))

                AppTextField(
                    value = screenState.formState.password,
                    onValueChange = { screenState.updatePassword(it) },
                    label = stringResource(R.string.login_password_label),
                    placeholder = stringResource(R.string.login_password_placeholder),
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.large))

                PrimaryButton(
                    text = if (uiState.isLoading) stringResource(R.string.login_button_loading) else stringResource(
                        R.string.login_button
                    ),
                    onClick = {
                        viewModel.login(
                            screenState.formState.username,
                            screenState.formState.password
                        )
                    },
                    enabled = screenState.isFormValid() && !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.normal))

                AppTextButton(
                    text = stringResource(R.string.login_forgot_password),
                    onClick = onForgotPasswordClick,
                    enabled = !uiState.isLoading
                )
            }

            // Loading overlay - covers entire screen including system bars
            if (uiState.isLoading) {
                LoadingView(
                    message = stringResource(R.string.login_loading_message)
                )
            }

            // Snackbar host
            AppSnackbarHost(
                hostState = snackbarHostState,
                snackbarType = SnackbarType.ERROR,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(Spacing.normal)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    GdsGpsCollectionTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(Spacing.extraLarge),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = Spacing.massive * 8),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AppLogo()

                Spacer(modifier = Modifier.height(Spacing.huge))

                AppTextField(
                    value = "",
                    onValueChange = { },
                    label = "Username",
                    placeholder = "Enter your username",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.normal))

                AppTextField(
                    value = "",
                    onValueChange = { },
                    label = "Password",
                    placeholder = "Enter your password",
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.large))

                PrimaryButton(
                    text = "Login",
                    onClick = { },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.normal))

                AppTextButton(
                    text = "Forgot Password?",
                    onClick = { }
                )
            }
        }
    }
}
