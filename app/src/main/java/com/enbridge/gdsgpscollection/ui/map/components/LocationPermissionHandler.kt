package com.enbridge.gdsgpscollection.ui.map.components

import android.Manifest
import android.os.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Composable function that handles location permission requests.
 *
 * This component manages the permission request flow for location services,
 * including showing rationale dialogs when necessary and handling different
 * Android versions.
 *
 * ## Permissions Requested:
 * - ACCESS_FINE_LOCATION: For precise GPS location
 * - ACCESS_COARSE_LOCATION: For approximate location (fallback)
 *
 * ## Future Bluetooth GPS Integration:
 * When implementing Bluetooth GPS, you'll need additional permissions.
 * Add these to the permissions list:
 * ```kotlin
 * val bluetoothPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
 *     listOf(
 *         Manifest.permission.BLUETOOTH_CONNECT,
 *         Manifest.permission.BLUETOOTH_SCAN
 *     )
 * } else {
 *     listOf(
 *         Manifest.permission.BLUETOOTH,
 *         Manifest.permission.BLUETOOTH_ADMIN
 *     )
 * }
 * ```
 *
 * @param onPermissionResult Callback invoked with permission status result
 * @param shouldRequest Whether to automatically request permissions on composition
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionHandler(
    onPermissionResult: (Boolean) -> Unit,
    shouldRequest: Boolean = true
) {
    val context = LocalContext.current
    var showRationaleDialog by remember { mutableStateOf(false) }

    // Define the required permissions
    val locationPermissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // Create permission state
    val permissionsState = rememberMultiplePermissionsState(
        permissions = locationPermissions
    ) { permissions ->
        // Check if at least one location permission is granted
        val hasLocationPermission = permissions.values.any { it }
        onPermissionResult(hasLocationPermission)
    }

    // Check if we should show rationale
    val shouldShowRationale = permissionsState.permissions.any { it.status.shouldShowRationale }

    // Check if permissions are already granted
    val hasLocationPermission = permissionsState.permissions.any { it.status.isGranted }

    // Automatically request permissions if needed
    LaunchedEffect(shouldRequest) {
        if (shouldRequest && !hasLocationPermission) {
            if (shouldShowRationale) {
                showRationaleDialog = true
            } else {
                permissionsState.launchMultiplePermissionRequest()
            }
        } else if (hasLocationPermission) {
            onPermissionResult(true)
        }
    }

    // Show rationale dialog if needed
    if (showRationaleDialog) {
        LocationPermissionRationaleDialog(
            onConfirm = {
                showRationaleDialog = false
                permissionsState.launchMultiplePermissionRequest()
            },
            onDismiss = {
                showRationaleDialog = false
                onPermissionResult(false)
            }
        )
    }
}

/**
 * Dialog explaining why location permissions are needed.
 *
 * This dialog is shown when the user has previously denied permissions
 * and Android requires us to show a rationale before requesting again.
 *
 * ## Future Bluetooth GPS Integration:
 * Update the dialog text to explain both device location and Bluetooth GPS:
 * - "...to display your current location on the map"
 * - "...to connect to external Bluetooth GPS devices for enhanced accuracy"
 * - "...to access elevation data and accuracy metrics from GPS hardware"
 */
@Composable
private fun LocationPermissionRationaleDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Location Permission Required")
        },
        text = {
            Text(
                text = "This app needs access to your device's location to display your " +
                        "current position on the map. This helps you navigate and understand " +
                        "your position relative to map features.\n\n" +
                        "Note: In the future, this will also enable connection to external " +
                        "Bluetooth GPS devices for enhanced location accuracy, elevation data, " +
                        "and precision metrics."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Grant Permission")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
