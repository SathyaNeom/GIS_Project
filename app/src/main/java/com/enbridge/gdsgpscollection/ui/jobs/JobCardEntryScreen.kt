package com.enbridge.gdsgpscollection.ui.jobs

/**
 * @author Sathya Narayanan
 */

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.enbridge.gdsgpscollection.R
import com.enbridge.gdsgpscollection.designsystem.components.AppSnackbarHost
import com.enbridge.gdsgpscollection.designsystem.components.AppTabRow
import com.enbridge.gdsgpscollection.designsystem.components.PrimaryButton
import com.enbridge.gdsgpscollection.designsystem.components.SnackbarType
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobCardEntryScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: JobCardEntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val savedSuccessMessage = stringResource(R.string.job_card_entry_saved_success)

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar(savedSuccessMessage)
            viewModel.clearSaveSuccess()
        }
    }

    GdsGpsCollectionTheme {
        Scaffold(
            snackbarHost = {
                AppSnackbarHost(
                    hostState = snackbarHostState,
                    snackbarType = SnackbarType.INFO
                )
            }
        ) { paddingValues ->
            Card(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .windowInsetsPadding(WindowInsets.systemBars),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header with white background and elevation
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.normal, vertical = Spacing.medium),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.job_card_entry_title),
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.Black
                            )
                            IconButton(onClick = onClose) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.job_card_entry_close),
                                    tint = Color.Black
                                )
                            }
                        }
                    }

                    // Tabs with white background and elevation
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shadowElevation = 4.dp
                    ) {
                        AppTabRow(
                            selectedTabIndex = uiState.selectedTab,
                            onTabSelected = { viewModel.selectTab(it) },
                            tabs = listOf(
                                stringResource(R.string.tab_job_card),
                                stringResource(R.string.tab_measurements),
                                stringResource(R.string.tab_meter_info)
                            )
                        )
                    }

                    // Content with Crossfade animation for tab switching
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        BoxWithConstraints(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Use Material Design breakpoints:
                            // Compact: < 600dp (phone portrait)
                            // Medium: 600dp - 840dp (phone landscape, small tablet)
                            // Expanded: >= 840dp (large tablet, desktop)
                            val isWideScreen = maxWidth >= 840.dp

                            Crossfade(
                                targetState = uiState.selectedTab,
                                animationSpec = tween(durationMillis = 300),
                                label = "Tab Content Crossfade"
                            ) { selectedTab ->
                                when (selectedTab) {
                                    0 -> JobCardTab(
                                        entry = uiState.entry,
                                        onUpdateField = { update -> viewModel.updateField(update) },
                                        isWideScreen = isWideScreen
                                    )

                                    1 -> MeasurementsTab(
                                        entry = uiState.entry,
                                        onUpdateField = { update -> viewModel.updateField(update) },
                                        isWideScreen = isWideScreen
                                    )

                                    2 -> MeterInfoTab(
                                        entry = uiState.entry,
                                        onUpdateField = { update -> viewModel.updateField(update) },
                                        isWideScreen = isWideScreen
                                    )
                                }
                            }
                        }
                    }

                    // Save Button
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        shadowElevation = 4.dp
                    ) {
                        PrimaryButton(
                            text = if (uiState.isSaving) stringResource(R.string.job_card_entry_saving) else stringResource(
                                R.string.job_card_entry_save
                            ),
                            onClick = { viewModel.saveJobCardEntry() },
                            enabled = !uiState.isSaving,
                            modifier = Modifier.padding(Spacing.normal)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun JobCardEntryScreenPreview() {
    GdsGpsCollectionTheme {
        JobCardEntryScreen(
            onClose = { }
        )
    }
}
