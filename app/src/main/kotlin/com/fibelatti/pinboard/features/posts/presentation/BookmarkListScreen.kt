@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.fibelatti.pinboard.features.posts.presentation

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.fibelatti.core.android.extension.shareText
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig.DEFAULT_PAGE_SIZE
import com.fibelatti.pinboard.core.AppConfig.PINBOARD_USER_URL
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.android.SelectionDialog
import com.fibelatti.pinboard.core.android.composable.EmptyListContent
import com.fibelatti.pinboard.core.android.composable.LaunchedErrorHandlerEffect
import com.fibelatti.pinboard.core.android.composable.PullRefreshLayout
import com.fibelatti.pinboard.core.android.composable.RememberedEffect
import com.fibelatti.pinboard.core.android.composable.TextWithBlockquote
import com.fibelatti.pinboard.core.extension.ScrollDirection
import com.fibelatti.pinboard.core.extension.applySecureFlag
import com.fibelatti.pinboard.core.extension.copyToClipboard
import com.fibelatti.pinboard.core.extension.rememberScrollDirection
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.features.appstate.AccountSwitcherContent
import com.fibelatti.pinboard.features.appstate.AddPost
import com.fibelatti.pinboard.features.appstate.ByDateAddedNewestFirst
import com.fibelatti.pinboard.features.appstate.ByDateAddedOldestFirst
import com.fibelatti.pinboard.features.appstate.ByDateModifiedNewestFirst
import com.fibelatti.pinboard.features.appstate.ByDateModifiedOldestFirst
import com.fibelatti.pinboard.features.appstate.ByTitleAlphabetical
import com.fibelatti.pinboard.features.appstate.ByTitleAlphabeticalReverse
import com.fibelatti.pinboard.features.appstate.ClearSearch
import com.fibelatti.pinboard.features.appstate.EditPost
import com.fibelatti.pinboard.features.appstate.GetNextPostPage
import com.fibelatti.pinboard.features.appstate.Loaded
import com.fibelatti.pinboard.features.appstate.PostList
import com.fibelatti.pinboard.features.appstate.PostListContent
import com.fibelatti.pinboard.features.appstate.PostsForTag
import com.fibelatti.pinboard.features.appstate.Private
import com.fibelatti.pinboard.features.appstate.Refresh
import com.fibelatti.pinboard.features.appstate.SearchParameters
import com.fibelatti.pinboard.features.appstate.SetSorting
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.appstate.Unread
import com.fibelatti.pinboard.features.appstate.UserLoggedIn
import com.fibelatti.pinboard.features.appstate.ViewPost
import com.fibelatti.pinboard.features.appstate.ViewRandomPost
import com.fibelatti.pinboard.features.appstate.ViewSearch
import com.fibelatti.pinboard.features.appstate.find
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import com.fibelatti.pinboard.features.main.MainState
import com.fibelatti.pinboard.features.main.MainViewModel
import com.fibelatti.pinboard.features.posts.domain.model.PendingSync
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.presentation.UserPreferencesViewModel
import com.fibelatti.ui.components.ChipGroup
import com.fibelatti.ui.components.MultilineChipGroup
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun BookmarkListScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel(),
    postListViewModel: PostListViewModel = hiltViewModel(),
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
    userPreferencesViewModel: UserPreferencesViewModel = hiltViewModel(),
    listState: LazyListState = rememberLazyListState(),
) {
    Surface(
        modifier = modifier,
        color = ExtendedTheme.colors.backgroundNoOverlay,
    ) {
        val appState by mainViewModel.appState.collectAsStateWithLifecycle()

        val postListContent by rememberUpdatedState(
            newValue = appState.content.find<PostListContent>() ?: return@Surface,
        )
        val postDetailScreenState by postDetailViewModel.screenState.collectAsStateWithLifecycle()

        val postListError by postListViewModel.error.collectAsStateWithLifecycle()
        val postDetailError by postDetailViewModel.error.collectAsStateWithLifecycle()
        val hasError by remember {
            derivedStateOf { postListError != null || postDetailError != null }
        }
        val isLoading by remember {
            derivedStateOf { (postListContent.shouldLoad != Loaded || postDetailScreenState.isLoading) && !hasError }
        }

        val userCredentials by userPreferencesViewModel.userCredentials.collectAsStateWithLifecycle()
        val userPreferences by userPreferencesViewModel.currentPreferences.collectAsStateWithLifecycle()
        val tagsClipboard = remember { mutableListOf<Tag>() }

        val localContext = LocalContext.current
        val localView = LocalView.current

        LaunchedMainViewModelEffect()
        LaunchedPostDetailViewModelEffect()
        LaunchedErrorHandlerEffect(error = postListError, handler = postListViewModel::errorHandled)
        LaunchedErrorHandlerEffect(error = postDetailError, handler = postDetailViewModel::errorHandled)

        BookmarkListScreen(
            appMode = appState.appMode,
            posts = postListContent.posts,
            isLoading = isLoading,
            onScrollDirectionChanged = mainViewModel::setCurrentScrollDirection,
            onNextPageRequested = { mainViewModel.runAction(GetNextPostPage) },
            sortType = postListContent.sortType,
            searchParameters = postListContent.searchParameters,
            onActiveSearchClicked = { mainViewModel.runAction(ViewSearch) },
            onClearClicked = { mainViewModel.runAction(ClearSearch) },
            onSaveClicked = {
                postListViewModel.saveFilter(
                    SavedFilter(
                        searchTerm = postListContent.searchParameters.term,
                        tags = postListContent.searchParameters.tags,
                    ),
                )
                localView.showBanner(R.string.saved_filters_saved_feedback)
            },
            onShareClicked = { searchParameters ->
                userCredentials.getPinboardUsername()?.let { username ->
                    shareFilteredResults(
                        context = localContext,
                        username = username,
                        searchParameters = searchParameters,
                    )
                }
            },
            onPullToRefresh = { mainViewModel.runAction(Refresh()) },
            onPostClicked = { post -> mainViewModel.runAction(ViewPost(post)) },
            onPostLongClicked = { post ->
                showQuickActionsDialog(
                    context = localContext,
                    post = post,
                    tagsClipboard = tagsClipboard,
                    hiddenPostQuickOptions = userPreferences.hiddenPostQuickOptions,
                    onToggleReadLater = {
                        postDetailViewModel.toggleReadLater(post = post)
                    },
                    onCopyTags = { tags ->
                        tagsClipboard.clear()
                        tagsClipboard.addAll(tags)
                    },
                    onPasteTags = { tags ->
                        postDetailViewModel.addTags(post = post, tags = tags)
                    },
                    onEdit = {
                        mainViewModel.runAction(action = EditPost(post))
                    },
                    onDelete = {
                        showDeleteConfirmationDialog(context = localContext) {
                            postDetailViewModel.deletePost(post)
                        }
                    },
                    onExpandDescription = {
                        PostDescriptionDialog.showPostDescriptionDialog(
                            context = localContext,
                            appMode = appState.appMode,
                            post = post,
                        )
                    },
                )
            },
            onTagClicked = { post -> mainViewModel.runAction(PostsForTag(post)) },
            onPrivateClicked = { mainViewModel.runAction(Private) },
            onReadLaterClicked = { mainViewModel.runAction(Unread) },
            showPostDescription = postListContent.showDescription,
            sidePanelVisible = appState.sidePanelVisible,
            listState = listState,
        )
    }
}

// region ViewModel setup
@Composable
private fun LaunchedMainViewModelEffect(
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val appState by mainViewModel.appState.collectAsStateWithLifecycle()
    val localContext = LocalContext.current
    val localLifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(Unit) {
        mainViewModel.menuItemClicks(contentType = PostListContent::class)
            .onEach { (menuItem, _) ->
                when (menuItem) {
                    is MainState.MenuItemComponent.SearchBookmarks -> {
                        mainViewModel.runAction(ViewSearch)
                    }

                    is MainState.MenuItemComponent.SortBookmarks -> {
                        showSortingSelector(
                            context = localContext,
                            appMode = appState.appMode,
                        ) { option ->
                            mainViewModel.runAction(SetSorting(option))
                        }
                    }

                    is MainState.MenuItemComponent.RandomBookmark -> {
                        mainViewModel.runAction(ViewRandomPost)
                    }

                    is MainState.MenuItemComponent.SyncBookmarks -> {
                        mainViewModel.runAction(Refresh(force = true))
                    }

                    else -> Unit
                }
            }
            .flowWithLifecycle(localLifecycle)
            .launchIn(this)
        mainViewModel.fabClicks(contentType = PostListContent::class)
            .onEach { mainViewModel.runAction(AddPost) }
            .flowWithLifecycle(localLifecycle)
            .launchIn(this)
        mainViewModel.actionButtonClicks(contentType = AccountSwitcherContent::class)
            .onEach { data ->
                if (data is AppMode) {
                    mainViewModel.runAction(UserLoggedIn(appMode = data))
                }
            }
            .flowWithLifecycle(localLifecycle)
            .launchIn(this)
    }
}

@Composable
private fun LaunchedPostDetailViewModelEffect(
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
) {
    val screenState by postDetailViewModel.screenState.collectAsStateWithLifecycle()

    val localContext = LocalContext.current
    val localView = LocalView.current

    RememberedEffect(screenState) {
        val current = screenState
        when {
            current.deleted is Success<Boolean> && current.deleted.value -> {
                localView.showBanner(R.string.posts_deleted_feedback)
                postDetailViewModel.userNotified()
            }

            current.deleted is Failure -> {
                MaterialAlertDialogBuilder(localContext).apply {
                    setMessage(R.string.posts_deleted_error)
                    setPositiveButton(R.string.hint_ok) { dialog, _ -> dialog?.dismiss() }
                }.applySecureFlag().show()
            }

            current.updated is Success<Boolean> && current.updated.value -> {
                localView.showBanner(R.string.posts_marked_as_read_feedback)
                postDetailViewModel.userNotified()
            }

            current.updated is Failure -> {
                localView.showBanner(R.string.posts_marked_as_read_error)
                postDetailViewModel.userNotified()
            }
        }
    }
}
// endregion ViewModel setup

// region Service functions
private fun showQuickActionsDialog(
    context: Context,
    post: Post,
    tagsClipboard: List<Tag>,
    hiddenPostQuickOptions: Set<String>,
    onToggleReadLater: () -> Unit,
    onCopyTags: (List<Tag>) -> Unit,
    onPasteTags: (List<Tag>) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExpandDescription: () -> Unit,
) = with(context) {
    val allOptions = PostQuickActions.allOptions(post = post, tagsClipboard = tagsClipboard)
        .associateWith { option -> option.serializedName in hiddenPostQuickOptions }

    SelectionDialog.show(
        context = context,
        title = getString(R.string.quick_actions_title),
        options = allOptions,
        optionName = { option -> getString(option.title) },
        optionIcon = PostQuickActions::icon,
        onOptionSelected = { option ->
            when (option) {
                is PostQuickActions.ToggleReadLater -> onToggleReadLater()

                is PostQuickActions.CopyTags -> onCopyTags(option.tags)

                is PostQuickActions.PasteTags -> onPasteTags(option.tags)

                is PostQuickActions.Edit -> onEdit()

                is PostQuickActions.Delete -> onDelete()

                is PostQuickActions.CopyUrl -> copyToClipboard(
                    label = post.displayTitle,
                    text = post.url,
                )

                is PostQuickActions.Share -> shareText(
                    title = R.string.posts_share_title,
                    text = post.url,
                )

                is PostQuickActions.ExpandDescription -> onExpandDescription()

                is PostQuickActions.OpenBrowser -> startActivity(
                    Intent(Intent.ACTION_VIEW, post.url.toUri()),
                )

                is PostQuickActions.SearchWayback -> startActivity(
                    Intent(Intent.ACTION_VIEW, "https://web.archive.org/web/*/${post.url}".toUri()),
                )

                is PostQuickActions.SendToWayback -> startActivity(
                    Intent(Intent.ACTION_VIEW, "https://web.archive.org/save/${post.url}".toUri()),
                )

                is PostQuickActions.SendToArchiveToday -> startActivity(
                    Intent(Intent.ACTION_VIEW, "https://archive.today/submit/?url=${post.url}".toUri()),
                )

                is PostQuickActions.SendToGhostArchive -> startActivity(
                    Intent(Intent.ACTION_VIEW, "https://ghostarchive.org/save/${post.url}".toUri()),
                )
            }
        },
    )
}

private fun shareFilteredResults(
    context: Context,
    username: String,
    searchParameters: SearchParameters,
) = with(context) {
    val queryUrl = "$PINBOARD_USER_URL$username?query=${searchParameters.term}"
    val tagsUrl = "$PINBOARD_USER_URL$username/${searchParameters.tags.joinToString { "t:${it.name}/" }}"

    when {
        searchParameters.term.isNotBlank() && searchParameters.tags.isEmpty() -> {
            shareText(R.string.search_share_title, queryUrl)
        }

        searchParameters.term.isBlank() && searchParameters.tags.isNotEmpty() -> {
            shareText(R.string.search_share_title, tagsUrl)
        }

        else -> {
            SelectionDialog.show(
                context = context,
                title = getString(R.string.search_share_title),
                options = ShareSearchOption.entries,
                optionName = { option ->
                    when (option) {
                        ShareSearchOption.QUERY -> getString(R.string.search_share_query)
                        ShareSearchOption.TAGS -> getString(R.string.search_share_tags)
                    }
                },
                onOptionSelected = { option ->
                    val url = when (option) {
                        ShareSearchOption.QUERY -> queryUrl
                        ShareSearchOption.TAGS -> tagsUrl
                    }
                    shareText(R.string.search_share_title, url)
                },
            )
        }
    }
}

private fun showSortingSelector(
    context: Context,
    appMode: AppMode,
    onOptionSelected: (SortType) -> Unit,
) = with(context) {
    SelectionDialog.show(
        context = context,
        title = getString(R.string.menu_main_sorting),
        options = buildList {
            add(ByDateAddedNewestFirst)
            add(ByDateAddedOldestFirst)

            if (AppMode.LINKDING == appMode) {
                add(ByDateModifiedNewestFirst)
                add(ByDateModifiedOldestFirst)
            }

            add(ByTitleAlphabetical)
            add(ByTitleAlphabeticalReverse)
        },
        optionName = { option ->
            when (option) {
                is ByDateAddedNewestFirst -> getString(R.string.sorting_by_date_added_newest_first)
                is ByDateAddedOldestFirst -> getString(R.string.sorting_by_date_added_oldest_first)
                is ByDateModifiedNewestFirst -> getString(R.string.sorting_by_date_modified_newest_first)
                is ByDateModifiedOldestFirst -> getString(R.string.sorting_by_date_modified_oldest_first)
                is ByTitleAlphabetical -> getString(R.string.sorting_by_title_alphabetical)
                is ByTitleAlphabeticalReverse -> getString(R.string.sorting_by_title_alphabetical_reverse)
            }
        },
        onOptionSelected = onOptionSelected,
    )
}
// endregion Service functions

// region Content
@Composable
fun BookmarkListScreen(
    appMode: AppMode,
    posts: PostList?,
    isLoading: Boolean,
    onScrollDirectionChanged: (ScrollDirection) -> Unit,
    onNextPageRequested: () -> Unit,
    sortType: SortType,
    searchParameters: SearchParameters,
    onActiveSearchClicked: () -> Unit,
    onClearClicked: () -> Unit,
    onSaveClicked: () -> Unit,
    onShareClicked: (SearchParameters) -> Unit,
    onPullToRefresh: () -> Unit,
    onPostClicked: (Post) -> Unit,
    onPostLongClicked: (Post) -> Unit,
    onTagClicked: (Tag) -> Unit,
    onPrivateClicked: () -> Unit,
    onReadLaterClicked: () -> Unit,
    showPostDescription: Boolean,
    sidePanelVisible: Boolean,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        val windowInsets = WindowInsets.safeDrawing
            .only(if (sidePanelVisible) WindowInsetsSides.Start else WindowInsetsSides.Horizontal)
            .add(WindowInsets(left = 16.dp, right = 16.dp, bottom = 8.dp))

        AnimatedVisibility(visible = searchParameters.isActive()) {
            ActiveSearch(
                appMode = appMode,
                onViewClicked = onActiveSearchClicked,
                onClearClicked = onClearClicked,
                onSaveClicked = onSaveClicked,
                onShareClicked = { onShareClicked(searchParameters) },
                modifier = Modifier.windowInsetsPadding(windowInsets),
            )
        }

        AnimatedVisibility(visible = isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(windowInsets),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        if (posts == null && !isLoading) {
            EmptyListContent(
                icon = painterResource(id = R.drawable.ic_pin),
                title = stringResource(id = R.string.posts_empty_title),
                description = stringResource(id = R.string.posts_empty_description),
            )
        } else if (posts != null) {
            val scrollDirection by listState.rememberScrollDirection()
            val currentOnScrollDirectionChanged by rememberUpdatedState(onScrollDirectionChanged)

            val shouldRequestNewPage by remember {
                derivedStateOf {
                    listState.layoutInfo.run {
                        visibleItemsInfo.isNotEmpty() &&
                            visibleItemsInfo.last().index >= totalItemsCount - (DEFAULT_PAGE_SIZE / 2)
                    }
                }
            }
            val currentOnNextPageRequested by rememberUpdatedState(onNextPageRequested)

            RememberedEffect(posts.canPaginate, shouldRequestNewPage) {
                if (posts.canPaginate && shouldRequestNewPage) currentOnNextPageRequested()
            }

            RememberedEffect(scrollDirection) {
                currentOnScrollDirectionChanged(scrollDirection)
            }

            val listWindowInsets = WindowInsets.safeDrawing
                .only(if (sidePanelVisible) WindowInsetsSides.Start else WindowInsetsSides.Horizontal)
                .add(WindowInsets(top = 12.dp, bottom = 100.dp))

            PullRefreshLayout(
                onPullToRefresh = onPullToRefresh,
                listState = listState,
                contentPadding = listWindowInsets.asPaddingValues(),
            ) {
                items(posts.list, key = { it.id }) { post ->
                    BookmarkItem(
                        appMode = appMode,
                        post = post,
                        sortType = sortType,
                        onPostClicked = onPostClicked,
                        onPostLongClicked = onPostLongClicked,
                        showDescription = showPostDescription,
                        onTagClicked = onTagClicked,
                        onPrivateClicked = onPrivateClicked,
                        onReadLaterClicked = onReadLaterClicked,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveSearch(
    appMode: AppMode,
    onViewClicked: () -> Unit,
    onClearClicked: () -> Unit,
    onSaveClicked: () -> Unit,
    onShareClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val minHeight = 36.dp
        val padding = PaddingValues(horizontal = 8.dp)

        FilledTonalButton(
            onClick = onViewClicked,
            shapes = ExtendedTheme.defaultButtonShapes,
            modifier = Modifier.heightIn(min = minHeight),
            contentPadding = padding,
        ) {
            Text(
                text = stringResource(id = R.string.search_active),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        FilledTonalButton(
            onClick = onClearClicked,
            shapes = ExtendedTheme.defaultButtonShapes,
            modifier = Modifier.heightIn(min = minHeight),
            contentPadding = padding,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_clear_filter),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        }

        FilledTonalButton(
            onClick = onSaveClicked,
            shapes = ExtendedTheme.defaultButtonShapes,
            modifier = Modifier.heightIn(min = minHeight),
            contentPadding = padding,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_save),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        }

        if (AppMode.PINBOARD == appMode) {
            FilledTonalButton(
                onClick = onShareClicked,
                shapes = ExtendedTheme.defaultButtonShapes,
                modifier = Modifier.heightIn(min = minHeight),
                contentPadding = padding,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_share),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun BookmarkItem(
    appMode: AppMode,
    post: Post,
    sortType: SortType,
    onPostClicked: (Post) -> Unit,
    onPostLongClicked: (Post) -> Unit,
    showDescription: Boolean,
    onTagClicked: (Tag) -> Unit,
    onPrivateClicked: () -> Unit,
    onReadLaterClicked: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 4.dp),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onPostClicked(post) },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPostLongClicked(post)
                    },
                ),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(
                modifier = Modifier.padding(start = 8.dp, top = 28.dp, end = 8.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (post.pendingSync != null) {
                    PendingSyncIndicator(
                        text = when (post.pendingSync) {
                            PendingSync.ADD -> stringResource(id = R.string.posts_pending_add)
                            PendingSync.UPDATE -> stringResource(id = R.string.posts_pending_update)
                            PendingSync.DELETE -> stringResource(id = R.string.posts_pending_delete)
                        },
                    )
                }

                Text(
                    text = post.displayTitle,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    style = MaterialTheme.typography.titleMediumEmphasized.copy(fontSize = 18.sp),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    post.resolvedFaviconUrl?.let { faviconUrl ->
                        val painter: AsyncImagePainter = rememberAsyncImagePainter(model = faviconUrl)
                        val faviconState: AsyncImagePainter.State by painter.state.collectAsStateWithLifecycle()

                        AnimatedVisibility(
                            visible = faviconState is AsyncImagePainter.State.Success,
                            modifier = Modifier.size(16.dp),
                        ) {
                            Image(
                                painter = painter,
                                contentDescription = null,
                            )
                        }
                    }

                    Text(
                        text = post.url,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                if (showDescription && post.displayDescription.isNotBlank()) {
                    TextWithBlockquote(
                        text = post.displayDescription,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 5,
                        clickableLinks = false,
                    )
                }

                if (AppMode.LINKDING == appMode && !post.notes.isNullOrBlank()) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp),
                    )

                    TextWithBlockquote(
                        text = post.notes,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 5,
                        clickableLinks = false,
                    )
                }

                if (!post.tags.isNullOrEmpty()) {
                    val tags = remember(post.tags) {
                        post.tags.map { tag -> ChipGroup.Item(text = tag.name) }
                    }

                    MultilineChipGroup(
                        items = tags,
                        onItemClick = { item -> onTagClicked(post.tags.first { tag -> tag.name == item.text }) },
                        modifier = Modifier.padding(top = 8.dp),
                        itemTextStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    )
                }
            }
        }

        BookmarkFlags(
            time = if (sortType == ByDateModifiedNewestFirst || sortType == ByDateModifiedOldestFirst) {
                post.displayDateModified
            } else {
                post.displayDateAdded
            },
            private = post.private,
            readLater = post.readLater,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = (-12).dp, x = (-12).dp),
            onPrivateClicked = onPrivateClicked,
            onReadLaterClicked = onReadLaterClicked,
        )
    }
}

@Composable
fun PendingSyncIndicator(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_sync),
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
        )
    }
}

@Composable
private fun BookmarkFlags(
    time: String,
    private: Boolean?,
    readLater: Boolean?,
    modifier: Modifier = Modifier,
    onPrivateClicked: () -> Unit = {},
    onReadLaterClicked: () -> Unit = {},
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (private == true) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .shadow(elevation = 2.dp, shape = MaterialTheme.shapes.medium)
                    .background(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    .clickable(onClick = onPrivateClicked)
                    .padding(all = 8.dp)
                    .testTag("private-flag"),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_private),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        if (readLater == true) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .shadow(elevation = 2.dp, shape = MaterialTheme.shapes.medium)
                    .background(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    .clickable(onClick = onReadLaterClicked)
                    .padding(all = 8.dp)
                    .testTag("read-later-flag"),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_read_later),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Text(
            text = time,
            modifier = Modifier
                .fillMaxHeight()
                .shadow(elevation = 2.dp, shape = MaterialTheme.shapes.medium)
                .background(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                )
                .padding(all = 8.dp),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}
// endregion Content

// region Previews
@Composable
@ThemePreviews
private fun BookmarkListScreenPreview(
    @PreviewParameter(provider = PostListProvider::class) posts: List<Post>,
) {
    ExtendedTheme {
        BookmarkListScreen(
            appMode = AppMode.LINKDING,
            posts = PostList(
                list = posts,
                totalCount = posts.size,
                canPaginate = false,
            ),
            isLoading = true,
            onScrollDirectionChanged = {},
            onNextPageRequested = {},
            sortType = ByDateModifiedNewestFirst,
            searchParameters = SearchParameters(term = "bookmark"),
            onActiveSearchClicked = {},
            onClearClicked = {},
            onSaveClicked = {},
            onShareClicked = {},
            onPullToRefresh = {},
            onPostClicked = {},
            onPostLongClicked = {},
            onTagClicked = {},
            onPrivateClicked = {},
            onReadLaterClicked = {},
            showPostDescription = true,
            sidePanelVisible = false,
        )
    }
}

@Composable
@ThemePreviews
private fun ActiveSearchPreview() {
    ExtendedTheme {
        Box {
            ActiveSearch(
                onViewClicked = {},
                onClearClicked = {},
                onSaveClicked = {},
                onShareClicked = {},
                appMode = AppMode.LINKDING,
            )
        }
    }
}

@Composable
@ThemePreviews
private fun BookmarkItemPreview(
    @PreviewParameter(provider = PostProvider::class) post: Post,
) {
    ExtendedTheme {
        Box(
            modifier = Modifier
                .background(ExtendedTheme.colors.backgroundNoOverlay)
                .safeDrawingPadding(),
        ) {
            BookmarkItem(
                appMode = AppMode.LINKDING,
                post = post,
                sortType = ByDateModifiedNewestFirst,
                onPostClicked = {},
                onPostLongClicked = {},
                showDescription = true,
                onTagClicked = {},
                onPrivateClicked = {},
                onReadLaterClicked = {},
            )
        }
    }
}
// endregion Previews
