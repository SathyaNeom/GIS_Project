package com.enbridge.electronicservices.feature.auth

/**
 * @author Sathya Narayanan
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.enbridge.electronicservices.feature.auth.BuildConfig
import com.enbridge.electronicservices.designsystem.components.AppLogo
import com.enbridge.electronicservices.designsystem.components.AppSnackbarHost
import com.enbridge.electronicservices.designsystem.components.AppTextButton
import com.enbridge.electronicservices.designsystem.components.AppTextField
import com.enbridge.electronicservices.designsystem.components.LoadingView
import com.enbridge.electronicservices.designsystem.components.PrimaryButton
import com.enbridge.electronicservices.designsystem.components.SnackbarType
import com.enbridge.electronicservices.designsystem.components.getVariantIcon
import com.enbridge.electronicservices.designsystem.theme.ElectronicServicesTheme
import com.enbridge.electronicservices.designsystem.theme.Spacing

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    appName: String = "Electronic Services",
    appIcon: ImageVector = Icons.Default.Build,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    ElectronicServicesTheme {
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
                AppLogo(
                    appName = appName,
                    icon = appIcon
                )

                Spacer(modifier = Modifier.height(Spacing.huge))

                AppTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = stringResource(R.string.login_username_label),
                    placeholder = stringResource(R.string.login_username_placeholder),
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.normal))

                AppTextField(
                    value = password,
                    onValueChange = { password = it },
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
                    onClick = { viewModel.login(username, password) },
                    enabled = username.isNotBlank() && password.isNotBlank() && !uiState.isLoading,
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
                    icon = getVariantIcon(BuildConfig.APP_VARIANT),
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
    ElectronicServicesTheme {
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
                AppLogo(
                    appName = "Electronic Services",
                    icon = Icons.Default.Build
                )

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
