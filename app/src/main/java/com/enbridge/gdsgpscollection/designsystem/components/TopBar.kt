package com.enbridge.gdsgpscollection.designsystem.components

/**
 * @author Sathya Narayanan
 */
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme

/**
 * Custom top app bar component with optional custom actions.
 *
 * @param title The title text displayed in the app bar
 * @param onActionClick Default action click handler (used when actions is null)
 * @param modifier Optional modifier for the top bar
 * @param navigationIcon Optional navigation icon composable (typically a menu or back button)
 * @param actions Optional custom actions composable - when provided, overrides the default search icon
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            navigationIcon?.invoke()
        },
        actions = actions ?: {
            // Default search icon action
            IconButton(onClick = onActionClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            }
        },
        modifier = modifier.windowInsetsPadding(
            WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
        ),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun AppTopBarPreview() {
    GdsGpsCollectionTheme {
        Column {
            AppTopBar(
                title = "Screen Title",
                onActionClick = { }
            )
            AppTopBar(
                title = "With Nav Icon",
                navigationIcon = {
                    AppIconButton(
                        icon = androidx.compose.material.icons.Icons.Default.Menu,
                        contentDescription = "Menu",
                        onClick = { }
                    )
                },
                onActionClick = { }
            )
        }
    }
}
