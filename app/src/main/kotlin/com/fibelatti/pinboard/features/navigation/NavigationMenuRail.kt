package com.fibelatti.pinboard.features.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailState
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.android.composable.LongClickIconButton
import com.fibelatti.pinboard.core.android.icons.AppIcons
import com.fibelatti.pinboard.core.android.icons.Menu
import com.fibelatti.pinboard.features.appstate.Action
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.AppState
import com.fibelatti.pinboard.features.export.ExportBookmarksEffects
import com.fibelatti.pinboard.features.export.ExportBookmarksViewModel
import com.fibelatti.pinboard.features.main.MainViewModel
import com.fibelatti.ui.preview.PreviewAll
import com.fibelatti.ui.theme.ExtendedTheme
import kotlinx.coroutines.launch

/**
 * A navigation rail that presents [navigationMenuGroups].
 *
 * While collapsed, it shows only the primary destinations; expanding it reveals every entry with
 * its label.
 */
@Composable
fun NavigationMenuRail(
    railState: WideNavigationRailState,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = WideNavigationRailDefaults.windowInsets,
    exportBookmarksViewModel: ExportBookmarksViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val appState: AppState by mainViewModel.appState.collectAsStateWithLifecycle()

    ExportBookmarksEffects(
        state = exportBookmarksViewModel.state,
        onDestinationSelect = exportBookmarksViewModel::exportFile,
        onMessageDismiss = exportBookmarksViewModel::messageHandled,
    )

    NavigationMenuRail(
        railState = railState,
        appMode = appState.appMode,
        selectedDestination = appState.content.navigationMenuDestination(),
        onActionClick = rememberNavigationMenuActionHandler(
            onNavigate = mainViewModel::runAction,
            onExportBookmarks = { exportBookmarksViewModel.createBackup(appState.appMode) },
        ),
        modifier = modifier,
        windowInsets = windowInsets,
    )
}

@Composable
private fun NavigationMenuRail(
    railState: WideNavigationRailState,
    appMode: AppMode,
    selectedDestination: Action?,
    onActionClick: (NavigationMenuAction) -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = WideNavigationRailDefaults.windowInsets,
) {
    val coroutineScope = rememberCoroutineScope()
    val expanded: Boolean = railState.targetValue == WideNavigationRailValue.Expanded
    val groups: List<List<NavigationMenuItem>> = remember(appMode) { navigationMenuGroups(appMode) }

    WideNavigationRail(
        modifier = modifier,
        state = railState,
        windowInsets = windowInsets,
    ) {
        // The menu button is placed here rather than in the rail's header: the rail measures its content against the
        // height below the header but places it further down, so a full height scrollable column would overflow.
        Column {
            LongClickIconButton(
                painter = rememberVectorPainter(AppIcons.Menu),
                description = stringResource(R.string.cd_menu),
                onClick = { coroutineScope.launch { railState.toggle() } },
                modifier = Modifier.padding(start = 24.dp),
            )

            Spacer(modifier = Modifier.height(RailHeaderSpacing))

            NavigationMenuRailItems(
                groups = groups,
                selectedDestination = selectedDestination,
                expanded = expanded,
                onActionClick = { action: NavigationMenuAction ->
                    onActionClick(action)

                    // Leaves the content as much room as possible once a destination is reached
                    if (action is NavigationMenuAction.Navigate) {
                        coroutineScope.launch { railState.collapse() }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            )
        }
    }
}

@Composable
private fun NavigationMenuRailItems(
    groups: List<List<NavigationMenuItem>>,
    selectedDestination: Action?,
    expanded: Boolean,
    onActionClick: (NavigationMenuAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        groups.forEachIndexed { groupIndex: Int, group: List<NavigationMenuItem> ->
            if (groupIndex > 0) {
                AnimatedVisibility(visible = expanded) {
                    HorizontalDivider(
                        modifier = Modifier
                            .widthIn(max = RailFooterMaxWidth)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            for ((textRes: Int, icon: ImageVector, action: NavigationMenuAction, isPrimary: Boolean) in group) {
                AnimatedVisibility(visible = expanded || isPrimary) {
                    WideNavigationRailItem(
                        selected = (action as? NavigationMenuAction.Navigate)?.action == selectedDestination,
                        onClick = { onActionClick(action) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(id = textRes),
                                textAlign = TextAlign.Center,
                            )
                        },
                        railExpanded = expanded,
                    )
                }
            }
        }

        AnimatedVisibility(visible = expanded) {
            AppVersionDetails(
                onClick = { onActionClick(NavigationMenuAction.ViewLicenses) },
                modifier = Modifier
                    .widthIn(max = RailFooterMaxWidth)
                    .padding(horizontal = 16.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.Start,
            )
        }
    }
}

/**
 * Matches the spacing below the header of the [WideNavigationRail] from the Material 3 specs.
 */
private val RailHeaderSpacing: Dp = 40.dp

/**
 * Matches the minimum width of the expanded [WideNavigationRail] from the Material 3 specs, which
 * is not exposed by the library.
 */
private val RailFooterMaxWidth: Dp = 220.dp

// region Previews
@Composable
@PreviewAll
private fun NavigationMenuRailCollapsedPreview() {
    ExtendedTheme {
        NavigationMenuRail(
            appMode = AppMode.PINBOARD,
            selectedDestination = All,
            railState = rememberWideNavigationRailState(initialValue = WideNavigationRailValue.Collapsed),
            onActionClick = {},
        )
    }
}

@Composable
@PreviewAll
private fun NavigationMenuRailExpandedPreview() {
    ExtendedTheme {
        NavigationMenuRail(
            appMode = AppMode.PINBOARD,
            selectedDestination = All,
            railState = rememberWideNavigationRailState(initialValue = WideNavigationRailValue.Expanded),
            onActionClick = {},
        )
    }
}
// endregion Previews
