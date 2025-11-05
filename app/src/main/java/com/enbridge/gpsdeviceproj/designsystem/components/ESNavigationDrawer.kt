package com.enbridge.gpsdeviceproj.designsystem.components

/**
 * @author Sathya Narayanan
 */
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.enbridge.gpsdeviceproj.R
import com.enbridge.gpsdeviceproj.designsystem.theme.Spacing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.LocationOn

/**
 * GPS Device Project Navigation Drawer Content
 * Displays up to 4 menu items:
 * 1. Collect Electronic Services
 * 2. ES Job Card Entry (only shown when showJobCardEntry is true)
 * 3. Project Settings
 * 4. Manage ES Edits
 *
 * @param onCollectESClick Callback when Collect ES is clicked
 * @param onESJobCardEntryClick Callback when ES Job Card Entry is clicked
 * @param onProjectSettingsClick Callback when Project Settings is clicked
 * @param onManageESEditsClick Callback when Manage ES Edits is clicked
 * @param drawerState State for controlling the drawer
 * @param scope Coroutine scope for drawer operations
 * @param modifier Optional modifier for the drawer
 * @param selectedItem Currently selected drawer item
 * @param showJobCardEntry Whether to show the ES Job Card Entry menu item (default: true)
 */
@Composable
fun ESNavigationDrawerContent(
    onCollectESClick: () -> Unit,
    onESJobCardEntryClick: () -> Unit,
    onProjectSettingsClick: () -> Unit,
    onManageESEditsClick: () -> Unit,
    drawerState: DrawerState,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
    selectedItem: ESDrawerItem? = null,
    showJobCardEntry: Boolean = true
) {
    Surface(
        modifier = modifier
            .wrapContentWidth(align = Alignment.Start)
            .wrapContentHeight(align = Alignment.Top)
            .widthIn(min = 240.dp, max = 320.dp)
            .windowInsetsPadding(
                WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Start)
            ),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 16.dp,
            bottomEnd = 16.dp,
            bottomStart = 0.dp
        )
    ) {
        Column(
            modifier = Modifier.wrapContentHeight()
        ) {
            Spacer(modifier = Modifier.height(Spacing.normal))
            Text(
                stringResource(R.string.task_list),
                modifier = Modifier.padding(Spacing.normal),
                style = MaterialTheme.typography.titleLarge
            )
            HorizontalDivider()

            // Collect Electronic Services
            ESNavigationDrawerItem(
                icon = Icons.Default.LocationOn,
                label = stringResource(R.string.collect_electronic_services),
                selected = selectedItem == ESDrawerItem.COLLECT_ES,
                onClick = {
                    scope.launch { drawerState.close() }
                    onCollectESClick()
                }
            )

            // ES Job Card Entry - conditionally shown based on showJobCardEntry parameter
            if (showJobCardEntry) {
                ESNavigationDrawerItem(
                    icon = Icons.Default.List,
                    label = stringResource(R.string.es_job_card_entry),
                    selected = selectedItem == ESDrawerItem.JOB_CARD_ENTRY,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onESJobCardEntryClick()
                    }
                )
            }

            // Project Settings
            ESNavigationDrawerItem(
                icon = Icons.Default.Settings,
                label = stringResource(R.string.project_settings),
                selected = selectedItem == ESDrawerItem.PROJECT_SETTINGS,
                onClick = {
                    scope.launch { drawerState.close() }
                    onProjectSettingsClick()
                }
            )

            // Manage ES Edits
            ESNavigationDrawerItem(
                icon = Icons.Default.Edit,
                label = stringResource(R.string.manage_es_edits),
                selected = selectedItem == ESDrawerItem.MANAGE_ES_EDITS,
                onClick = {
                    scope.launch { drawerState.close() }
                    onManageESEditsClick()
                }
            )

            Spacer(modifier = Modifier.height(Spacing.normal))
        }
    }
}

/**
 * Individual drawer item component
 */
@Composable
private fun ESNavigationDrawerItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label) },
        selected = selected,
        onClick = onClick,
        modifier = modifier.padding(horizontal = Spacing.medium)
    )
}

/**
 * Enum to represent drawer items for selection state
 */
enum class ESDrawerItem {
    COLLECT_ES,
    JOB_CARD_ENTRY,
    PROJECT_SETTINGS,
    MANAGE_ES_EDITS
}
