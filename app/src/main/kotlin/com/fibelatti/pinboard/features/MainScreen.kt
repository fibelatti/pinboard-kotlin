package com.fibelatti.pinboard.features

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.LocalAppCompatActivity
import com.fibelatti.pinboard.core.android.composable.LongClickIconButton
import com.fibelatti.pinboard.core.android.composable.MainTitle
import com.fibelatti.pinboard.core.android.isMultiPanelAvailable
import com.fibelatti.pinboard.core.extension.ScrollDirection
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.features.appstate.AddPostContent
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.ConnectionAwareContent
import com.fibelatti.pinboard.features.appstate.Content
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
import com.fibelatti.pinboard.features.appstate.SidePanelContent
import com.fibelatti.pinboard.features.appstate.TagListContent
import com.fibelatti.pinboard.features.appstate.UserPreferencesContent
import com.fibelatti.pinboard.features.filters.presentation.SavedFiltersScreen
import com.fibelatti.pinboard.features.navigation.NavigationMenu
import com.fibelatti.pinboard.features.notes.presentation.NoteDetailsScreen
import com.fibelatti.pinboard.features.notes.presentation.NoteListScreen
import com.fibelatti.pinboard.features.posts.presentation.BookmarkDetailsScreen
import com.fibelatti.pinboard.features.posts.presentation.BookmarkListScreen
import com.fibelatti.pinboard.features.posts.presentation.EditBookmarkScreen
import com.fibelatti.pinboard.features.posts.presentation.PopularBookmarksScreen
import com.fibelatti.pinboard.features.posts.presentation.SearchBookmarksScreen
import com.fibelatti.pinboard.features.tags.presentation.TagListScreen
import com.fibelatti.pinboard.features.user.presentation.AuthScreen
import com.fibelatti.pinboard.features.user.presentation.UserPreferencesScreen
import com.fibelatti.ui.foundation.pxToDp
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import kotlin.reflect.KClass

@Composable
fun MainScreen(
    content: Content,
    onExternalBrowserContent: (ExternalBrowserContent) -> Unit,
    onExternalContent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            MainTopAppBar(modifier = Modifier.fillMaxWidth())

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                val multiPanelAvailable = content is SidePanelContent && isMultiPanelAvailable()

                MainScreenContent(
                    content = content,
                    multiPanelAvailable = multiPanelAvailable,
                    onExternalBrowserContent = onExternalBrowserContent,
                    onExternalContent = onExternalContent,
                    modifier = Modifier.fillMaxWidth(fraction = if (multiPanelAvailable) .45f else 1f),
                )

                if (multiPanelAvailable) {
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

        MainBottomAppBar(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun MainScreenContent(
    content: Content,
    multiPanelAvailable: Boolean,
    onExternalBrowserContent: (ExternalBrowserContent) -> Unit,
    onExternalContent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val localView = LocalView.current

    LaunchedEffect(content::class) {
        if (content is LoginContent && content.isUnauthorized) {
            localView.showBanner(messageRes = R.string.auth_logged_out_feedback)
        }
    }

    AnimatedContent(
        targetState = content::class,
        modifier = modifier.fillMaxSize(),
        transitionSpec = { fadeIn(tween()) togetherWith fadeOut(tween()) },
    ) { targetState ->
        when (targetState) {
            LoginContent::class -> AuthScreen()
            PostListContent::class -> BookmarkListScreen()
            PostDetailContent::class -> if (multiPanelAvailable) BookmarkListScreen() else BookmarkDetailsScreen()
            SearchContent::class -> SearchBookmarksScreen()
            AddPostContent::class -> EditBookmarkScreen()
            EditPostContent::class -> EditBookmarkScreen()
            TagListContent::class -> TagListScreen()
            SavedFiltersContent::class -> SavedFiltersScreen()
            NoteListContent::class -> NoteListScreen()
            NoteDetailContent::class -> if (multiPanelAvailable) NoteListScreen() else NoteDetailsScreen()
            PopularPostsContent::class -> PopularBookmarksScreen()
            PopularPostDetailContent::class -> if (multiPanelAvailable) PopularBookmarksScreen() else BookmarkDetailsScreen()
            UserPreferencesContent::class -> UserPreferencesScreen()
            ExternalBrowserContent::class -> (content as ExternalBrowserContent).let(onExternalBrowserContent)
            ExternalContent::class -> onExternalContent()
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
fun MainTopAppBar(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel(),
    appStateViewModel: AppStateViewModel = hiltViewModel(),
) {
    val state by mainViewModel.state.collectAsStateWithLifecycle()
    val content by appStateViewModel.content.collectAsStateWithLifecycle()
    val localOnBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    MainTopAppBar(
        state = state,
        onNavigationClick = { localOnBackPressedDispatcher?.onBackPressed() },
        onActionButtonClick = { data -> mainViewModel.actionButtonClicked(state.actionButton.id, data) },
        isOffline = content.let { it is ConnectionAwareContent && !it.isConnected },
        showRetryButton = content is PostListContent || content is PopularPostsContent,
        onOfflineRetryClick = retryClick@{
            val action = when (content) {
                is PostListContent -> Refresh()
                is PopularPostsContent -> RefreshPopular
                else -> return@retryClick
            }

            appStateViewModel.runAction(action)
        },
        hideAllControls = content is LoginContent,
        modifier = modifier,
    )
}

@Composable
fun MainBottomAppBar(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel(),
    appStateViewModel: AppStateViewModel = hiltViewModel(),
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomEnd,
    ) {
        val state by mainViewModel.state.collectAsStateWithLifecycle()
        val currentScrollDirection by mainViewModel.currentScrollDirection.collectAsStateWithLifecycle()

        val content by appStateViewModel.content.collectAsStateWithLifecycle()
        val hideAllControls by remember { derivedStateOf { content is LoginContent } }

        var bottomAppBarHeight by remember { mutableIntStateOf(0) }

        LaunchedEffect(content) {
            mainViewModel.setCurrentScrollDirection(ScrollDirection.IDLE)
        }

        AnimatedVisibility(
            visible = !hideAllControls &&
                content is SidePanelContent &&
                isMultiPanelAvailable() &&
                state.sidePanelAppBar is MainState.SidePanelAppBarComponent.Visible,
            modifier = Modifier
                .padding(
                    bottom = if (state.bottomAppBar is MainState.BottomAppBarComponent.Visible) {
                        bottomAppBarHeight.pxToDp()
                    } else {
                        0.dp
                    },
                )
                .windowInsetsPadding(
                    WindowInsets.navigationBars
                        .add(WindowInsets.displayCutout)
                        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                ),
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
        ) {
            SidePanelBottomAppBar(
                state = state,
                scrollDirection = currentScrollDirection,
                onMenuItemClick = { menuItem, data ->
                    mainViewModel.menuItemClicked(id = state.sidePanelAppBar.id, menuItem = menuItem, data = data)
                },
            )
        }

        AnimatedVisibility(
            visible = !hideAllControls && currentScrollDirection != ScrollDirection.DOWN,
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { bottomAppBarHeight = it.size.height },
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
        ) {
            val localActivity = LocalAppCompatActivity.current

            MainBottomAppBar(
                state = state,
                onBottomNavClick = {
                    NavigationMenu.show(activity = localActivity)
                },
                onMenuItemClick = { menuItem, data ->
                    mainViewModel.menuItemClicked(id = state.bottomAppBar.id, menuItem = menuItem, data = data)
                },
                onFabClick = { data ->
                    mainViewModel.fabClicked(id = state.floatingActionButton.id, data = data)
                },
            )
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
    hideAllControls: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        if (!hideAllControls) {
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
            TextButton(onClick = onOfflineRetryClick) {
                Text(text = stringResource(id = R.string.offline_retry))
            }
        }
    }
}

@Composable
private fun MainBottomAppBar(
    state: MainState,
    onBottomNavClick: () -> Unit,
    onMenuItemClick: (MainState.MenuItemComponent, data: Any?) -> Unit,
    onFabClick: (data: Any?) -> Unit,
) {
    if (state.bottomAppBar is MainState.BottomAppBarComponent.Visible) {
        Surface(
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp)
                    .windowInsetsPadding(
                        WindowInsets.navigationBars
                            .add(WindowInsets.displayCutout)
                            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                    ),
            ) {
                AnimatedContent(
                    targetState = state.bottomAppBar.navigationIcon,
                    label = "MainBottomAppBar_NavIcon",
                ) { icon ->
                    if (icon != null) {
                        LongClickIconButton(
                            painter = painterResource(id = icon),
                            description = stringResource(R.string.cd_menu),
                            onClick = onBottomNavClick,
                        )
                    }
                }

                MenuItemsContent(
                    menuItems = state.bottomAppBar.menuItems,
                    data = state.bottomAppBar.data,
                    onMenuItemClick = onMenuItemClick,
                )

                Spacer(modifier = Modifier.weight(1f))

                if (state.floatingActionButton is MainState.FabComponent.Visible) {
                    FloatingActionButton(
                        onClick = { onFabClick(state.floatingActionButton.data) },
                        modifier = Modifier.testTag("fab-${state.floatingActionButton.id}"),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ) {
                        AnimatedContent(
                            targetState = state.floatingActionButton.icon,
                            transitionSpec = { fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut() },
                            label = "Fab_Icon",
                        ) { icon ->
                            Icon(
                                painter = painterResource(icon),
                                contentDescription = null,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuItemsContent(
    menuItems: List<MainState.MenuItemComponent>,
    data: Any?,
    onMenuItemClick: (MainState.MenuItemComponent, data: Any?) -> Unit,
) {
    AnimatedContent(
        targetState = menuItems,
        label = "MenuItemsContent",
    ) { items ->
        Row {
            for (menuItem in items) {
                if (menuItem.icon == null) {
                    TextButton(
                        onClick = { onMenuItemClick(menuItem, data) },
                        modifier = Modifier.animateEnterExit(enter = fadeIn(), exit = fadeOut()),
                    ) {
                        Text(
                            text = stringResource(id = menuItem.name),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                } else {
                    LongClickIconButton(
                        painter = painterResource(id = menuItem.icon),
                        description = stringResource(id = menuItem.name),
                        onClick = { onMenuItemClick(menuItem, data) },
                        modifier = Modifier.animateEnterExit(enter = fadeIn(), exit = fadeOut()),
                    )
                }
            }
        }
    }
}

@Composable
private fun SidePanelBottomAppBar(
    state: MainState,
    scrollDirection: ScrollDirection,
    onMenuItemClick: (MainState.MenuItemComponent, data: Any?) -> Unit,
) {
    if (state.sidePanelAppBar is MainState.SidePanelAppBarComponent.Visible) {
        var collapsed by remember(scrollDirection) {
            mutableStateOf(scrollDirection == ScrollDirection.DOWN)
        }

        Box(
            modifier = Modifier
                .padding(all = 16.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.75f),
                    shape = MaterialTheme.shapes.large,
                )
                .padding(all = 8.dp)
                .animateContentSize(),
        ) {
            if (collapsed) {
                LongClickIconButton(
                    painter = painterResource(id = R.drawable.ic_chevron_left),
                    description = stringResource(id = R.string.cd_expand_menu),
                    onClick = { collapsed = false },
                )
            } else {
                MenuItemsContent(
                    menuItems = state.sidePanelAppBar.menuItems,
                    data = state.sidePanelAppBar.data,
                    onMenuItemClick = onMenuItemClick,
                )
            }
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
                    actionButton = MainState.ActionButtonComponent.Visible(id = "", label = "Action"),
                    bottomAppBar = MainState.BottomAppBarComponent.Visible(
                        id = "",
                        menuItems = listOf(MainState.MenuItemComponent.SearchBookmarks),
                        navigationIcon = R.drawable.ic_menu,
                    ),
                    floatingActionButton = MainState.FabComponent.Visible(id = "", icon = R.drawable.ic_pin),
                )
            },
            onNavigationClick = {},
            onActionButtonClick = {},
            isOffline = true,
            showRetryButton = true,
            onOfflineRetryClick = {},
            hideAllControls = false,
        )
    }
}

@Composable
@ThemePreviews
private fun MainBottomAppBarPreview() {
    ExtendedTheme {
        Box(
            contentAlignment = Alignment.BottomCenter,
        ) {
            MainBottomAppBar(
                state = remember {
                    MainState(
                        title = MainState.TitleComponent.Visible(label = "Sample title"),
                        subtitle = MainState.TitleComponent.Visible(label = "Sample subtitle"),
                        navigation = MainState.NavigationComponent.Visible(),
                        actionButton = MainState.ActionButtonComponent.Visible(id = "", label = "Action"),
                        bottomAppBar = MainState.BottomAppBarComponent.Visible(
                            id = "",
                            menuItems = listOf(MainState.MenuItemComponent.SearchBookmarks),
                            navigationIcon = R.drawable.ic_menu,
                        ),
                        floatingActionButton = MainState.FabComponent.Visible(id = "", icon = R.drawable.ic_pin),
                    )
                },
                onBottomNavClick = {},
                onMenuItemClick = { _, _ -> },
                onFabClick = {},
            )
        }
    }
}
// endregion Previews
