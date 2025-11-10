package com.enbridge.gdsgpscollection.ui.map.components

/**
 * @author Sathya Narayanan
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enbridge.gdsgpscollection.R
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing

/**
 * Displays a persistent banner when the device is offline.
 *
 * Shows a red background with white text and an icon to inform users they are working
 * in offline mode with cached data. The banner is always visible when offline and cannot
 * be dismissed by the user.
 *
 * Visibility is controlled by the parent composable. This component always renders when called.
 *
 * @param isOffline Whether the device is currently offline (parameter kept for API consistency)
 * @param modifier Optional modifier for this composable
 */
@Composable
fun OfflineBanner(
    isOffline: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(OfflineBannerRed)
            .padding(horizontal = Spacing.normal, vertical = Spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = stringResource(R.string.cd_offline_banner),
            tint = Color.White,
            modifier = Modifier
                .size(20.dp)
                .padding(end = Spacing.small)
        )

        Text(
            text = stringResource(R.string.banner_offline_message),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}

/**
 * Semi-transparent red color for the offline banner background.
 * Provides clear visibility without being overly alarming.
 */
private val OfflineBannerRed = Color(0xFFD32F2F).copy(alpha = 0.9f)

@Preview(showBackground = true)
@Composable
private fun OfflineBannerPreview() {
    GdsGpsCollectionTheme {
        OfflineBanner(isOffline = true)
    }
}

@Preview(showBackground = true)
@Composable
private fun OfflineBannerHiddenPreview() {
    GdsGpsCollectionTheme {
        OfflineBanner(isOffline = false)
    }
}
