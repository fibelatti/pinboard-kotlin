@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.fibelatti.pinboard.features.main

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.LocalAppCompatActivity
import com.fibelatti.pinboard.core.android.composable.LongClickIconButton
import com.fibelatti.pinboard.core.android.composable.MainTitle
import com.fibelatti.pinboard.core.android.composable.RememberedEffect
import com.fibelatti.pinboard.core.android.getWindowSizeClass
import com.fibelatti.pinboard.core.extension.ScrollDirection
import com.fibelatti.pinboard.features.appstate.AccountSwitcherContent
import com.fibelatti.pinboard.features.appstate.AddPostContent
import com.fibelatti.pinboard.features.appstate.ConnectionAwareContent
import com.fibelatti.pinboard.features.appstate.Content
import com.fibelatti.pinboard.features.appstate.ContentWithHistory
import com.fibelatti.pinboard.features.appstate.EditPostContent
import com.fibelatti.pinboard.features.appstate.ExternalBrowserContent
import com.fibelatti.pinboard.features.appstate.ExternalContent
import com.fibelatti.pinboard.features.appstate.LoginContent
import com.fibelatti.pinboard.features.appstate.NoteDetailContent
import com.fibelatti.pinboard.features.appstate.NoteListContent
import com.fibelatti.pinboard.features.appstate.PopularPostDetailContent
import com.fibelatti.pinboard.features.appstate.PopularPostsContent
import com.fibelatti.pinboard.features.appstate.PostDetailContent
import com.fibelatti.pinboard.features.appstate.PostListContent
import com.fibelatti.pinboard.features.appstate.Refresh
import com.fibelatti.pinboard.features.appstate.RefreshPopular
import com.fibelatti.pinboard.features.appstate.SavedFiltersContent
import com.fibelatti.pinboard.features.appstate.SearchContent
import com.fibelatti.pinboard.features.appstate.TagListContent
import com.fibelatti.pinboard.features.appstate.UserPreferencesContent
import com.fibelatti.pinboard.features.appstate.find
import com.fibelatti.pinboard.features.filters.presentation.SavedFiltersScreen
import com.fibelatti.pinboard.features.navigation.NavigationMenuBottomSheet
import com.fibelatti.pinboard.features.notes.presentation.NoteDetailsScreen
import com.fibelatti.pinboard.features.notes.presentation.NoteListScreen
import com.fibelatti.pinboard.features.posts.presentation.BookmarkDetailsScreen
import com.fibelatti.pinboard.features.posts.presentation.BookmarkListScreen
import com.fibelatti.pinboard.features.posts.presentation.EditBookmarkScreen
import com.fibelatti.pinboard.features.posts.presentation.PopularBookmarksScreen
import com.fibelatti.pinboard.features.posts.presentation.SearchBookmarksScreen
import com.fibelatti.pinboard.features.tags.presentation.TagListScreen
import com.fibelatti.pinboard.features.user.presentation.AccountSwitcherScreen
import com.fibelatti.pinboard.features.user.presentation.AuthScreen
import com.fibelatti.pinboard.features.user.presentation.UserPreferencesScreen
import com.fibelatti.ui.components.rememberAppSheetState
import com.fibelatti.ui.components.showBottomSheet
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import kotlin.reflect.KClass
import kotlinx.coroutines.delay

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val appState by mainViewModel.appState.collectAsStateWithLifecycle()
    val state by mainViewModel.state.collectAsStateWithLifecycle()
    val backHandlerEnabled by remember {
        derivedStateOf { (appState.content as? ContentWithHistory)?.previousContent !is ExternalContent }
    }
    val isWidthAtLeastBreakpoint = getWindowSizeClass()
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    val localActivity = LocalAppCompatActivity.current
    val localOnBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    val localUriHandler = LocalUriHandler.current

    val navMenuSheetState = rememberAppSheetState()

    BackHandler(
        enabled = backHandlerEnabled,
        onBack = mainViewModel::navigateBack,
    )

    RememberedEffect(isWidthAtLeastBreakpoint) {
        mainViewModel.setMultiPanelAvailable(value = isWidthAtLeastBreakpoint)
    }

    RememberedEffect(appState.content) {
        mainViewModel.setCurrentScrollDirection(ScrollDirection.IDLE)

        when (val content = appState.content) {
            is ExternalBrowserContent -> {
                localUriHandler.openUri(content.post.url)
                mainViewModel.navigateBack()
            }

            is ExternalContent -> {
                localActivity.finish()
                mainViewModel.resetAppNavigation()
            }

            else -> Unit
        }
    }

    MainScreen(
        state = state,
        sidePanelVisible = appState.sidePanelVisible,
        content = appState.content,
        onNavigationClick = {
            localOnBackPressedDispatcher?.onBackPressed()
        },
        onActionButtonClick = { data ->
            mainViewModel.actionButtonClicked(state.actionButton.contentType, data)
        },
        onOfflineRetryClick = retryClick@{
            val action = when (appState.content) {
                is PostListContent -> Refresh()
                is PopularPostsContent -> RefreshPopular
                else -> return@retryClick
            }

            mainViewModel.runAction(action)
        },
        onBottomNavClick = navMenuSheetState::showBottomSheet,
        onMenuItemClick = { menuItem, data ->
            mainViewModel.menuItemClicked(
                contentType = state.bottomAppBar.contentType,
                menuItem = menuItem,
                data = data,
            )
        },
        onSideMenuItemClick = { menuItem, data ->
            mainViewModel.menuItemClicked(
                contentType = state.sidePanelAppBar.contentType,
                menuItem = menuItem,
                data = data,
            )
        },
        onFabClick = { data ->
            mainViewModel.fabClicked(contentType = state.floatingActionButton.contentType, data = data)
        },
        modifier = modifier,
    )

    NavigationMenuBottomSheet(
        sheetState = navMenuSheetState,
        appMode = appState.appMode,
        onNavOptionClicked = mainViewModel::runAction,
    )
}

@Composable
fun MainScreen(
    state: MainState,
    sidePanelVisible: Boolean,
    content: Content,
    onNavigationClick: () -> Unit,
    onActionButtonClick: (data: Any?) -> Unit,
    onOfflineRetryClick: () -> Unit,
    onBottomNavClick: () -> Unit,
    onMenuItemClick: (MainState.MenuItemComponent, data: Any?) -> Unit,
    onSideMenuItemClick: (MainState.MenuItemComponent, data: Any?) -> Unit,
    onFabClick: (data: Any?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (content !is LoginContent || content.previousContent !is ExternalContent) {
                MainTopAppBar(
                    state = state,
                    onNavigationClick = onNavigationClick,
                    onActionButtonClick = onActionButtonClick,
                    isOffline = content.let { it is ConnectionAwareContent && !it.isConnected },
                    showRetryButton = content is PostListContent || content is PopularPostsContent,
                    onOfflineRetryClick = onOfflineRetryClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                MainPanelContent(
                    content = content,
                    sidePanelVisible = sidePanelVisible,
                    modifier = Modifier.fillMaxWidth(fraction = if (sidePanelVisible) .45f else 1f),
                )

                if (sidePanelVisible) {
                    Spacer(modifier = Modifier.width(8.dp))

                    SidePanelContent(
                        contentClass = content::class,
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(
                                insets = WindowInsets.navigationBars
                                    .union(WindowInsets.displayCutout)
                                    .only(WindowInsetsSides.End),
                            ),
                    )
                }
            }
        }

        val bottomBarVisible = content !is LoginContent &&
            state.floatingActionButton is MainState.FabComponent.Visible &&
            state.scrollDirection != ScrollDirection.DOWN

        AnimatedVisibility(
            visible = bottomBarVisible,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
        ) {
            MainBottomAppBar(
                state = state,
                sidePanelVisible = sidePanelVisible,
                onBottomNavClick = onBottomNavClick,
                onMenuItemClick = onMenuItemClick,
                onSideMenuItemClick = onSideMenuItemClick,
                onFabClick = onFabClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.navigationBars
                            .add(WindowInsets.displayCutout)
                            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                    )
                    .padding(bottom = 16.dp),
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun MainPanelContent(
    content: Content,
    sidePanelVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    val mainPanelContent: KClass<out Content> = remember(content::class, sidePanelVisible) {
        when {
            sidePanelVisible && content::class == PostDetailContent::class -> PostListContent::class
            sidePanelVisible && content::class == NoteDetailContent::class -> NoteListContent::class
            sidePanelVisible && content::class == PopularPostDetailContent::class -> PopularPostsContent::class
            else -> content::class
        }
    }
    val postListContent: PostListContent? = remember(content) { content.find() }

    val dpCacheWindow = LazyLayoutCacheWindow(aheadFraction = .5f, behindFraction = .5f)
    val bookmarkListState: LazyListState = rememberLazyListState(cacheWindow = dpCacheWindow)

    LaunchedEffect(
        postListContent?.category,
        postListContent?.posts?.list?.firstOrNull(),
        postListContent?.searchParameters,
    ) {
        if (postListContent != null) {
            delay(200L)
            bookmarkListState.scrollToItem(index = 0)
        }
    }

    AnimatedContent(
        targetState = mainPanelContent,
        modifier = modifier.fillMaxSize(),
        transitionSpec = { fadeIn(tween()) togetherWith fadeOut(tween()) },
    ) { targetState ->
        when (targetState) {
            LoginContent::class -> AuthScreen()
            PostListContent::class -> BookmarkListScreen(listState = bookmarkListState)
            PostDetailContent::class -> BookmarkDetailsScreen()
            SearchContent::class -> SearchBookmarksScreen()
            AddPostContent::class -> EditBookmarkScreen()
            EditPostContent::class -> EditBookmarkScreen()
            TagListContent::class -> TagListScreen()
            SavedFiltersContent::class -> SavedFiltersScreen()
            NoteListContent::class -> NoteListScreen()
            NoteDetailContent::class -> NoteDetailsScreen()
            PopularPostsContent::class -> PopularBookmarksScreen()
            PopularPostDetailContent::class -> BookmarkDetailsScreen()
            AccountSwitcherContent::class -> AccountSwitcherScreen()
            UserPreferencesContent::class -> UserPreferencesScreen()
        }
    }
}

@Composable
private fun SidePanelContent(
    contentClass: KClass<out Content>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        when (contentClass) {
            PostDetailContent::class, PopularPostDetailContent::class -> BookmarkDetailsScreen()
            NoteDetailContent::class -> NoteDetailsScreen()
        }
    }
}

@Composable
private fun MainTopAppBar(
    state: MainState,
    onNavigationClick: () -> Unit,
    onActionButtonClick: (data: Any?) -> Unit,
    isOffline: Boolean,
    showRetryButton: Boolean,
    onOfflineRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        MainTitle(
            title = state.title,
            subtitle = state.subtitle,
            navigation = state.navigation,
            onNavigationClicked = onNavigationClick,
            actionButton = state.actionButton,
            onActionButtonClicked = onActionButtonClick,
        )

        AnimatedVisibility(visible = isOffline) {
            OfflineAlert(
                showRetryButton = showRetryButton,
                onOfflineRetryClick = onOfflineRetryClick,
            )
        }
    }
}

@Composable
private fun OfflineAlert(
    showRetryButton: Boolean,
    onOfflineRetryClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = R.string.offline_alert),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )

        if (showRetryButton) {
            Button(
                onClick = onOfflineRetryClick,
                shapes = ExtendedTheme.defaultButtonShapes,
            ) {
                Text(text = stringResource(id = R.string.offline_retry))
            }
        }
    }
}

@Composable
private fun MainBottomAppBar(
    state: MainState,
    sidePanelVisible: Boolean,
    onBottomNavClick: () -> Unit,
    onMenuItemClick: (MainState.MenuItemComponent, data: Any?) -> Unit,
    onSideMenuItemClick: (MainState.MenuItemComponent, data: Any?) -> Unit,
    onFabClick: (data: Any?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
    ) {
        MainPanelBottomAppBar(
            bottomAppBar = state.bottomAppBar,
            floatingActionButton = state.floatingActionButton,
            onBottomNavClick = onBottomNavClick,
            onMenuItemClick = onMenuItemClick,
            onFabClick = onFabClick,
        )

        AnimatedVisibility(
            visible = sidePanelVisible && state.sidePanelAppBar is MainState.SidePanelAppBarComponent.Visible,
        ) {
            SidePanelBottomAppBar(
                sidePanelAppBar = state.sidePanelAppBar,
                onMenuItemClick = onSideMenuItemClick,
            )
        }
    }
}

@Composable
private fun MainPanelBottomAppBar(
    bottomAppBar: MainState.BottomAppBarComponent,
    floatingActionButton: MainState.FabComponent,
    onBottomNavClick: () -> Unit,
    onMenuItemClick: (MainState.MenuItemComponent, data: Any?) -> Unit,
    onFabClick: (data: Any?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (bottomAppBar !is MainState.BottomAppBarComponent.Visible) return

    val expanded: Boolean = bottomAppBar.navigationIcon != null || bottomAppBar.menuItems.isNotEmpty()
    val fab by rememberUpdatedState(floatingActionButton as? MainState.FabComponent.Visible)

    HorizontalFloatingToolbar(
        expanded = expanded,
        floatingActionButton = {
            FloatingToolbarDefaults.VibrantFloatingActionButton(
                onClick = { onFabClick(fab?.data) },
                modifier = Modifier.testTag("fab-${floatingActionButton.contentType.simpleName}"),
            ) {
                AnimatedContent(
                    targetState = fab?.icon,
                    transitionSpec = { fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut() },
                    label = "Fab_Icon",
                ) { icon ->
                    Icon(
                        painter = painterResource(icon ?: R.drawable.ic_hourglass),
                        contentDescription = null,
                    )
                }
            }
        },
        modifier = modifier.defaultMinSize(minHeight = if (expanded) 64.dp else 80.dp),
        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
        content = {
            if (bottomAppBar.navigationIcon != null) {
                LongClickIconButton(
                    painter = painterResource(id = bottomAppBar.navigationIcon),
                    description = stringResource(R.string.cd_menu),
                    onClick = onBottomNavClick,
                )
            }

            MenuItemsContent(
                menuItems = bottomAppBar.menuItems,
                data = bottomAppBar.data,
                onMenuItemClick = onMenuItemClick,
            )
        },
    )
}

@Composable
private fun SidePanelBottomAppBar(
    sidePanelAppBar: MainState.SidePanelAppBarComponent,
    onMenuItemClick: (MainState.MenuItemComponent, data: Any?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (sidePanelAppBar !is MainState.SidePanelAppBarComponent.Visible) return

    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier,
        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(),
    ) {
        MenuItemsContent(
            menuItems = sidePanelAppBar.menuItems,
            onMenuItemClick = onMenuItemClick,
            data = sidePanelAppBar.data,
        )
    }
}

@Composable
@Suppress("UnusedReceiverParameter")
private fun RowScope.MenuItemsContent(
    menuItems: List<MainState.MenuItemComponent>,
    onMenuItemClick: (MainState.MenuItemComponent, data: Any?) -> Unit,
    data: Any? = null,
    contentColor: Color = LocalContentColor.current,
) {
    for (menuItem in menuItems) {
        if (menuItem.icon == null) {
            TextButton(
                onClick = { onMenuItemClick(menuItem, data) },
                shapes = ExtendedTheme.defaultButtonShapes,
            ) {
                Text(
                    text = stringResource(id = menuItem.name),
                    color = contentColor,
                )
            }
        } else {
            LongClickIconButton(
                painter = painterResource(id = menuItem.icon),
                description = stringResource(id = menuItem.name),
                onClick = { onMenuItemClick(menuItem, data) },
                iconTint = contentColor,
            )
        }
    }
}

// region Previews
@Composable
@ThemePreviews
private fun MainTopAppBarPreview() {
    ExtendedTheme {
        MainTopAppBar(
            state = remember {
                MainState(
                    title = MainState.TitleComponent.Visible(label = "Sample title"),
                    subtitle = MainState.TitleComponent.Visible(label = "Sample subtitle"),
                    navigation = MainState.NavigationComponent.Visible(),
                    actionButton = MainState.ActionButtonComponent.Visible(
                        contentType = Content::class,
                        label = "Action",
                    ),
                    bottomAppBar = MainState.BottomAppBarComponent.Visible(
                        contentType = Content::class,
                        menuItems = listOf(MainState.MenuItemComponent.SearchBookmarks),
                        navigationIcon = R.drawable.ic_menu,
                    ),
                    floatingActionButton = MainState.FabComponent.Visible(
                        contentType = Content::class,
                        icon = R.drawable.ic_pin,
                    ),
                )
            },
            onNavigationClick = {},
            onActionButtonClick = {},
            isOffline = true,
            showRetryButton = true,
            onOfflineRetryClick = {},
        )
    }
}

@Composable
@ThemePreviews
private fun BottomAppBarPreview() {
    ExtendedTheme {
        Box(
            contentAlignment = Alignment.BottomCenter,
        ) {
            MainPanelBottomAppBar(
                bottomAppBar = MainState.BottomAppBarComponent.Visible(
                    contentType = Content::class,
                    menuItems = listOf(MainState.MenuItemComponent.SearchBookmarks),
                    navigationIcon = R.drawable.ic_menu,
                ),
                floatingActionButton = MainState.FabComponent.Visible(
                    contentType = Content::class,
                    icon = R.drawable.ic_pin,
                ),
                onBottomNavClick = {},
                onMenuItemClick = { _, _ -> },
                onFabClick = {},
            )
        }
    }
}
// endregion Previews
