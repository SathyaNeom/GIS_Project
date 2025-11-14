package com.enbridge.gdsgpscollection.ui.debug

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arcgismaps.geometry.Point
import com.enbridge.gdsgpscollection.R

/**
 * Debug settings screen for development builds.
 *
 * Provides:
 * - Location mode display (Simulated vs Real GPS)
 * - Current location coordinates
 * - Location availability status
 *
 * Only accessible in debug builds (BuildConfig.DEBUG = true).
 *
 * @param onNavigateBack Callback when back button is pressed
 * @param locationMode Current location mode ("Simulated" or "Real GPS")
 * @param currentLocation Current location point
 * @param isLocationAvailable Whether location is currently available
 *
 * @author Sathya Narayanan
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugSettingsScreen(
    onNavigateBack: () -> Unit,
    locationMode: String,
    currentLocation: Point?,
    isLocationAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.debug_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Location Mode Section
            DebugSettingCard(
                title = stringResource(R.string.debug_location_mode),
                value = locationMode
            )

            // Location Availability Section
            DebugSettingCard(
                title = "Location Status",
                value = if (isLocationAvailable) {
                    stringResource(R.string.debug_location_available)
                } else {
                    stringResource(R.string.debug_location_unavailable)
                },
                valueColor = if (isLocationAvailable) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            // Current Location Section
            DebugSettingCard(
                title = stringResource(R.string.debug_current_location),
                value = currentLocation?.let {
                    "X: ${String.format("%.6f", it.x)}\nY: ${String.format("%.6f", it.y)}"
                } ?: stringResource(R.string.debug_location_unavailable)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Info Card
            InfoCard(
                text = "This screen is only available in debug builds. " +
                        "Location mode is automatically determined by BuildConfig.DEBUG."
            )
        }
    }
}

/**
 * Card displaying a single debug setting with title and value.
 */
@Composable
private fun DebugSettingCard(
    title: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = valueColor
            )
        }
    }
}

/**
 * Informational card with icon.
 */
@Composable
private fun InfoCard(
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
