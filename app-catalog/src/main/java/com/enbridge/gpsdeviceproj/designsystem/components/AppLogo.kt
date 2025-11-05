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
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Propane
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enbridge.gpsdeviceproj.designsystem.R
import com.enbridge.gpsdeviceproj.designsystem.theme.ElectronicServicesTheme

/**
 * Get the icon for a specific variant
 */
fun getVariantIcon(variant: String): ImageVector {
    return when (variant) {
        "electronic" -> Icons.Default.Build
        "maintenance" -> Icons.Default.Handyman
        "construction" -> Icons.Default.Construction
        "resurvey" -> Icons.Default.LocationOn
        "gas-storage" -> Icons.Default.Propane
        else -> Icons.Default.Build
    }
}

/**
 * App logo with brand icon and text
 * Uses primary theme color (yellow) for brand consistency
 * Icon and text can be customized based on the app variant
 *
 * @param appName The name of the application to display
 * @param icon The icon to display (defaults to Build icon for GPS Device Project)
 * @param modifier Optional modifier for the logo container
 */
@Composable
fun AppLogo(
    appName: String = stringResource(R.string.app_logo_text),
    icon: ImageVector = Icons.Default.Build,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
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

@Preview(showBackground = true)
@Composable
private fun AppLogoPreview() {
    ElectronicServicesTheme {
        Column {
            AppLogo()
            Spacer(modifier = Modifier.height(32.dp))
            AppLogo(
                appName = "Maintenance",
                icon = Icons.Default.Handyman
            )
            Spacer(modifier = Modifier.height(32.dp))
            AppLogo(
                appName = "Construction",
                icon = Icons.Default.Construction
            )
        }
    }
}
