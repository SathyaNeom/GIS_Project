package com.enbridge.gpsdeviceproj.designsystem.components

/**
 * @author Sathya Narayanan
 */
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enbridge.gpsdeviceproj.BuildConfig
import com.enbridge.gpsdeviceproj.R
import com.enbridge.gpsdeviceproj.designsystem.theme.ElectronicServicesTheme

/**
 * App logo with brand icon and text
 * Automatically uses variant-specific icon from resources
 *
 * The icon is loaded from variant-specific drawable resources (ic_app_logo).
 * This ensures consistency with the app's branding while being optimized for in-app display.
 *
 * Each variant (electronic, maintenance, construction, resurvey, gasStorage) has its own
 * custom icon defined in app/src/{variant}/res/drawable/ic_app_logo.xml
 *
 * @param modifier Optional modifier for the logo container
 * @param showAppName Whether to display the app name below the icon (defaults to true)
 */
@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    showAppName: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Use the variant-specific app logo icon
        // This automatically picks the correct icon based on the build variant
        Icon(
            painter = painterResource(id = R.drawable.ic_app_logo),
            contentDescription = stringResource(R.string.app_logo_description),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )

        if (showAppName) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = BuildConfig.APP_NAME,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

/**
 * Variant of AppLogo for preview purposes or when you need to override defaults
 *
 * @param appName Custom app name to display
 * @param modifier Optional modifier for the logo container
 */
@Composable
fun AppLogoWithCustomName(
    appName: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_app_logo),
            contentDescription = stringResource(R.string.app_logo_description),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = appName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * Legacy AppLogo with Material Icon support (for backward compatibility)
 *
 * @deprecated Use AppLogo() instead, which automatically uses variant-specific resources
 */
@Deprecated(
    message = "Use AppLogo() instead, which automatically uses variant-specific resources",
    replaceWith = ReplaceWith("AppLogo(modifier)")
)
@Composable
fun AppLogoLegacy(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Build,
            contentDescription = stringResource(R.string.app_logo_description),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = BuildConfig.APP_NAME,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppLogoPreview() {
    ElectronicServicesTheme {
        Column {
            AppLogo()
            Spacer(modifier = Modifier.height(32.dp))
            AppLogo(showAppName = false)
            Spacer(modifier = Modifier.height(32.dp))
            AppLogoWithCustomName(appName = "Custom Name")
        }
    }
}
