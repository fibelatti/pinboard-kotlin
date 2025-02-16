package com.fibelatti.pinboard.features.posts.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.fibelatti.core.android.extension.shareText
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.AppConfig.DEFAULT_PAGE_SIZE
import com.fibelatti.pinboard.core.AppConfig.PINBOARD_USER_URL
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.android.DialogEntryPoint
import com.fibelatti.pinboard.core.android.SelectionDialog
import com.fibelatti.pinboard.core.android.composable.EmptyListContent
import com.fibelatti.pinboard.core.android.composable.LaunchedErrorHandlerEffect
import com.fibelatti.pinboard.core.android.composable.PullRefreshLayout
import com.fibelatti.pinboard.core.android.composable.TextWithBlockquote
import com.fibelatti.pinboard.core.android.isMultiPanelAvailable
import com.fibelatti.pinboard.core.extension.ScrollDirection
import com.fibelatti.pinboard.core.extension.applySecureFlag
import com.fibelatti.pinboard.core.extension.copyToClipboard
import com.fibelatti.pinboard.core.extension.rememberScrollDirection
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AddPost
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
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
import com.fibelatti.pinboard.features.appstate.PostDetailContent
import com.fibelatti.pinboard.features.appstate.PostList
import com.fibelatti.pinboard.features.appstate.PostListContent
import com.fibelatti.pinboard.features.appstate.PostsForTag
import com.fibelatti.pinboard.features.appstate.Private
import com.fibelatti.pinboard.features.appstate.Public
import com.fibelatti.pinboard.features.appstate.Recent
import com.fibelatti.pinboard.features.appstate.Refresh
import com.fibelatti.pinboard.features.appstate.SearchParameters
import com.fibelatti.pinboard.features.appstate.SetSorting
import com.fibelatti.pinboard.features.appstate.ShouldForceLoad
import com.fibelatti.pinboard.features.appstate.ShouldLoadFirstPage
import com.fibelatti.pinboard.features.appstate.ShouldLoadNextPage
import com.fibelatti.pinboard.features.appstate.SidePanelContent
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.appstate.Unread
import com.fibelatti.pinboard.features.appstate.Untagged
import com.fibelatti.pinboard.features.appstate.ViewCategory
import com.fibelatti.pinboard.features.appstate.ViewPost
import com.fibelatti.pinboard.features.appstate.ViewSearch
import com.fibelatti.pinboard.features.appstate.find
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import com.fibelatti.pinboard.features.posts.domain.model.PendingSync
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.presentation.UserPreferencesViewModel
import com.fibelatti.ui.components.ChipGroup
import com.fibelatti.ui.components.MultilineChipGroup
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.EntryPointAccessors
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun BookmarkListScreen(
    appStateViewModel: AppStateViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
    postListViewModel: PostListViewModel = hiltViewModel(),
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
    userPreferencesViewModel: UserPreferencesViewModel = hiltViewModel(),
) {
    Surface(
        color = ExtendedTheme.colors.backgroundNoOverlay,
    ) {
        val appMode by appStateViewModel.appMode.collectAsStateWithLifecycle()
        val content by appStateViewModel.content.collectAsStateWithLifecycle()

        val currentState by rememberUpdatedState(
            newValue = content.find<PostListContent>() ?: return@Surface,
        )

        val postDetailScreenState by postDetailViewModel.screenState.collectAsStateWithLifecycle()

        val postListError by postListViewModel.error.collectAsStateWithLifecycle()
        val postDetailError by postDetailViewModel.error.collectAsStateWithLifecycle()
        val hasError = postListError != null || postDetailError != null

        val shouldLoadContent = currentState.shouldLoad is ShouldLoadFirstPage ||
            currentState.shouldLoad is ShouldForceLoad ||
            currentState.shouldLoad is ShouldLoadNextPage

        val userPreferences by userPreferencesViewModel.currentPreferences.collectAsStateWithLifecycle()
        val tagsClipboard = remember { mutableListOf<Tag>() }

        val localContext = LocalContext.current
        val localView = LocalView.current

        LaunchedEffect(shouldLoadContent, currentState) {
            if (shouldLoadContent) postListViewModel.loadContent(currentState)
        }

        LaunchedViewModelEffects()

        BookmarkListScreen(
            appMode = appMode,
            category = currentState.category,
            posts = currentState.posts,
            isLoading = (currentState.shouldLoad != Loaded || postDetailScreenState.isLoading) && !hasError,
            onScrollDirectionChanged = mainViewModel::setCurrentScrollDirection,
            onNextPageRequested = { appStateViewModel.runAction(GetNextPostPage) },
            sortType = currentState.sortType,
            searchParameters = currentState.searchParameters,
            onActiveSearchClicked = { appStateViewModel.runAction(ViewSearch) },
            onClearClicked = { appStateViewModel.runAction(ClearSearch) },
            onSaveClicked = {
                postListViewModel.saveFilter(
                    SavedFilter(
                        searchTerm = currentState.searchParameters.term,
                        tags = currentState.searchParameters.tags,
                    ),
                )
                localView.showBanner(R.string.saved_filters_saved_feedback)
            },
            onShareClicked = { searchParameters ->
                val entryPoint = EntryPointAccessors.fromApplication<DialogEntryPoint>(localContext.applicationContext)
                shareFilteredResults(
                    context = localContext,
                    username = entryPoint.userRepository().getUsername(),
                    searchParameters = searchParameters,
                )
            },
            onPullToRefresh = { appStateViewModel.runAction(Refresh()) },
            onPostClicked = { post -> appStateViewModel.runAction(ViewPost(post)) },
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
                        appStateViewModel.runAction(action = EditPost(post))
                    },
                    onDelete = {
                        showDeleteConfirmationDialog(context = localContext) {
                            postDetailViewModel.deletePost(post)
                        }
                    },
                    onExpandDescription = {
                        PostDescriptionDialog.showPostDescriptionDialog(
                            context = localContext,
                            appMode = appMode,
                            post = post,
                        )
                    },
                )
            },
            onTagClicked = { post -> appStateViewModel.runAction(PostsForTag(post)) },
            showPostDescription = currentState.showDescription,
            sidePanelVisible = content is SidePanelContent && isMultiPanelAvailable(),
        )
    }
}

object BookmarkListScreen {

    val ACTION_ID = UUID.randomUUID().toString()
}

// region ViewModel setup
@Composable
private fun LaunchedViewModelEffects(
    postListViewModel: PostListViewModel = hiltViewModel(),
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
) {
    LaunchedAppStateViewModelEffect(actionId = BookmarkListScreen.ACTION_ID)
    LaunchedMainViewModelEffect(actionId = BookmarkListScreen.ACTION_ID)
    LaunchedPostDetailViewModelEffect()

    LaunchedErrorHandlerEffect(viewModel = postListViewModel)
    LaunchedErrorHandlerEffect(viewModel = postDetailViewModel)
}

@Composable
private fun LaunchedAppStateViewModelEffect(
    appStateViewModel: AppStateViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
    actionId: String,
) {
    val content by appStateViewModel.content.collectAsStateWithLifecycle()
    val isMultiPanelAvailable = isMultiPanelAvailable()
    val postListContent by remember {
        derivedStateOf {
            when (val current = content) {
                is PostListContent -> current
                is PostDetailContent -> current.previousContent.takeIf { isMultiPanelAvailable }
                else -> null
            }
        }
    }

    val localContext = LocalContext.current

    LaunchedEffect(postListContent) {
        val current = postListContent ?: return@LaunchedEffect

        mainViewModel.updateState { currentState ->
            currentState.copy(
                title = MainState.TitleComponent.Visible(getCategoryTitle(localContext, current.category)),
                subtitle = if (current.posts == null && current.shouldLoad is Loaded) {
                    MainState.TitleComponent.Gone
                } else {
                    MainState.TitleComponent.Visible(
                        label = buildPostCountSubTitle(localContext, current.totalCount, current.sortType),
                    )
                },
                navigation = MainState.NavigationComponent.Gone,
                bottomAppBar = MainState.BottomAppBarComponent.Visible(
                    id = actionId,
                    menuItems = buildList {
                        add(MainState.MenuItemComponent.SearchBookmarks)
                        add(MainState.MenuItemComponent.SortBookmarks)

                        if (current.category == All && current.canForceSync) {
                            add(MainState.MenuItemComponent.SyncBookmarks)
                        }
                    },
                    navigationIcon = R.drawable.ic_menu,
                ),
                floatingActionButton = MainState.FabComponent.Visible(actionId, R.drawable.ic_pin),
            )
        }
    }
}

@Composable
private fun LaunchedMainViewModelEffect(
    mainViewModel: MainViewModel = hiltViewModel(),
    appStateViewModel: AppStateViewModel = hiltViewModel(),
    actionId: String,
) {
    val appMode by appStateViewModel.appMode.collectAsStateWithLifecycle()

    val localContext = LocalContext.current
    val localLifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(Unit) {
        mainViewModel.menuItemClicks(actionId)
            .onEach { (menuItem, _) ->
                when (menuItem) {
                    is MainState.MenuItemComponent.SearchBookmarks -> {
                        appStateViewModel.runAction(ViewSearch)
                    }

                    is MainState.MenuItemComponent.SortBookmarks -> {
                        showSortingSelector(
                            context = localContext,
                            appMode = appMode,
                        ) { option ->
                            appStateViewModel.runAction(SetSorting(option))
                        }
                    }

                    is MainState.MenuItemComponent.SyncBookmarks -> {
                        appStateViewModel.runAction(Refresh(force = true))
                    }

                    else -> Unit
                }
            }
            .flowWithLifecycle(localLifecycle)
            .launchIn(this)
        mainViewModel.fabClicks(actionId)
            .onEach { appStateViewModel.runAction(AddPost) }
            .flowWithLifecycle(localLifecycle)
            .launchIn(this)
    }
}

@Composable
private fun LaunchedPostDetailViewModelEffect(
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val screenState by postDetailViewModel.screenState.collectAsStateWithLifecycle()

    val localContext = LocalContext.current
    val localView = LocalView.current

    LaunchedEffect(screenState) {
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
                mainViewModel.updateState { currentState ->
                    currentState.copy(actionButton = MainState.ActionButtonComponent.Gone)
                }
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
                    Intent(Intent.ACTION_VIEW, Uri.parse(post.url)),
                )

                is PostQuickActions.SubmitToWayback -> startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://web.archive.org/save/${post.url}")),
                )

                is PostQuickActions.SearchWayback -> startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://web.archive.org/web/*/${post.url}")),
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

private fun getCategoryTitle(
    context: Context,
    category: ViewCategory,
): String = with(context) {
    return when (category) {
        All -> getString(R.string.posts_title_all)
        Recent -> getString(R.string.posts_title_recent)
        Public -> getString(R.string.posts_title_public)
        Private -> getString(R.string.posts_title_private)
        Unread -> getString(R.string.posts_title_unread)
        Untagged -> getString(R.string.posts_title_untagged)
    }
}

private fun buildPostCountSubTitle(
    context: Context,
    count: Int,
    sortType: SortType,
): String = with(context) {
    val countFormatArg = if (count % AppConfig.API_PAGE_SIZE == 0) "$count+" else "$count"
    val countString = resources.getQuantityString(R.plurals.posts_quantity, count, countFormatArg)
    return resources.getString(
        when (sortType) {
            is ByDateAddedNewestFirst -> R.string.posts_sorting_newest_first
            is ByDateAddedOldestFirst -> R.string.posts_sorting_oldest_first
            is ByDateModifiedNewestFirst -> R.string.posts_sorting_newest_first
            is ByDateModifiedOldestFirst -> R.string.posts_sorting_oldest_first
            is ByTitleAlphabetical -> R.string.posts_sorting_alphabetical
            is ByTitleAlphabeticalReverse -> R.string.posts_sorting_alphabetical_reverse
        },
        countString,
    )
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
    category: ViewCategory,
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
    showPostDescription: Boolean,
    sidePanelVisible: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
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
            val listState = rememberLazyListState()

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

            LaunchedEffect(posts.canPaginate, shouldRequestNewPage) {
                if (posts.canPaginate && shouldRequestNewPage) currentOnNextPageRequested()
            }

            LaunchedEffect(scrollDirection) {
                currentOnScrollDirectionChanged(scrollDirection)
            }

            LaunchedEffect(category, posts.list.first(), searchParameters) {
                delay(200L)
                listState.scrollToItem(index = 0)
            }

            val listWindowInsets = WindowInsets.safeDrawing
                .only(if (sidePanelVisible) WindowInsetsSides.Start else WindowInsetsSides.Horizontal)
                .add(WindowInsets(top = 4.dp, bottom = 100.dp))

            PullRefreshLayout(
                onPullToRefresh = onPullToRefresh,
                listState = listState,
                contentPadding = listWindowInsets.asPaddingValues(),
            ) {
                items(posts.list) { post ->
                    BookmarkItem(
                        appMode = appMode,
                        post = post,
                        sortType = sortType,
                        onPostClicked = onPostClicked,
                        onPostLongClicked = onPostLongClicked,
                        showDescription = showPostDescription,
                        onTagClicked = onTagClicked,
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
        val shape = MaterialTheme.shapes.small
        val padding = PaddingValues(horizontal = 8.dp)

        FilledTonalButton(
            onClick = onViewClicked,
            modifier = Modifier.heightIn(min = minHeight),
            shape = shape,
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
            modifier = Modifier.heightIn(min = minHeight),
            shape = shape,
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
            modifier = Modifier.heightIn(min = minHeight),
            shape = shape,
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
                modifier = Modifier.heightIn(min = minHeight),
                shape = shape,
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
@OptIn(ExperimentalFoundationApi::class)
private fun BookmarkItem(
    appMode: AppMode,
    post: Post,
    sortType: SortType,
    onPostClicked: (Post) -> Unit,
    onPostLongClicked: (Post) -> Unit,
    showDescription: Boolean,
    onTagClicked: (Tag) -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
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
                style = MaterialTheme.typography.titleMedium,
            )

            if (post.url != post.displayTitle) {
                Text(
                    text = post.url,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            BookmarkFlags(
                time = if (sortType == ByDateModifiedNewestFirst || sortType == ByDateModifiedOldestFirst) {
                    post.displayDateModified
                } else {
                    post.displayDateAdded
                },
                private = post.private,
                readLater = post.readLater,
            )

            if (showDescription && post.displayDescription.isNotBlank()) {
                TextWithBlockquote(
                    text = post.displayDescription,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
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
                    itemTonalElevation = 16.dp,
                )
            }
        }
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
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = time,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
        )

        Spacer(modifier = Modifier.size(8.dp))

        if (private == true) {
            Icon(
                painter = painterResource(id = R.drawable.ic_private),
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = stringResource(id = R.string.posts_item_private),
                modifier = Modifier.padding(end = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            )
        }

        if (readLater == true) {
            Icon(
                painter = painterResource(id = R.drawable.ic_read_later),
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = stringResource(id = R.string.posts_item_read_later),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            )
        }
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
            category = All,
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
            showPostDescription = true,
            onTagClicked = {},
            sidePanelVisible = false,
            appMode = AppMode.LINKDING,
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
            modifier = Modifier.background(ExtendedTheme.colors.backgroundNoOverlay),
        ) {
            BookmarkItem(
                appMode = AppMode.LINKDING,
                post = post,
                sortType = ByDateModifiedNewestFirst,
                onPostClicked = {},
                onPostLongClicked = {},
                showDescription = true,
                onTagClicked = {},
            )
        }
    }
}
// endregion Previews
