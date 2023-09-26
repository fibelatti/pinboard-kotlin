package com.fibelatti.pinboard.features.posts.presentation

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig.DEFAULT_PAGE_SIZE
import com.fibelatti.pinboard.core.android.composable.EmptyListContent
import com.fibelatti.pinboard.core.android.composable.PullRefreshLayout
import com.fibelatti.pinboard.core.android.composable.TextWithBlockquote
import com.fibelatti.pinboard.core.extension.ScrollDirection
import com.fibelatti.pinboard.core.extension.rememberScrollDirection
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.ClearSearch
import com.fibelatti.pinboard.features.appstate.GetNextPostPage
import com.fibelatti.pinboard.features.appstate.Loaded
import com.fibelatti.pinboard.features.appstate.PostDetailContent
import com.fibelatti.pinboard.features.appstate.PostList
import com.fibelatti.pinboard.features.appstate.PostListContent
import com.fibelatti.pinboard.features.appstate.PostsForTag
import com.fibelatti.pinboard.features.appstate.Refresh
import com.fibelatti.pinboard.features.appstate.SearchParameters
import com.fibelatti.pinboard.features.appstate.ShouldForceLoad
import com.fibelatti.pinboard.features.appstate.ShouldLoadFirstPage
import com.fibelatti.pinboard.features.appstate.ShouldLoadNextPage
import com.fibelatti.pinboard.features.appstate.SidePanelContent
import com.fibelatti.pinboard.features.appstate.ViewPost
import com.fibelatti.pinboard.features.appstate.ViewSearch
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import com.fibelatti.pinboard.features.posts.domain.model.PendingSync
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.ui.components.ChipGroup
import com.fibelatti.ui.components.MultilineChipGroup
import com.fibelatti.ui.foundation.asHorizontalPaddingDp
import com.fibelatti.ui.foundation.navigationBarsCompat
import com.fibelatti.ui.foundation.toStableList
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import kotlinx.coroutines.delay

@Composable
fun BookmarkListScreen(
    appStateViewModel: AppStateViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
    postListViewModel: PostListViewModel = hiltViewModel(),
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
    onPostLongClicked: (Post) -> Unit,
    onShareClicked: (SearchParameters) -> Unit,
) {
    Surface(
        color = ExtendedTheme.colors.backgroundNoOverlay,
    ) {
        val content by appStateViewModel.content.collectAsStateWithLifecycle()

        val currentState by rememberUpdatedState(
            newValue = when (val current = content) {
                is PostListContent -> current
                is PostDetailContent -> current.previousContent
                else -> return@Surface
            },
        )

        val postListLoading = currentState.shouldLoad != Loaded
        val postDetailScreenState by postDetailViewModel.screenState.collectAsStateWithLifecycle()

        val postListError by postListViewModel.error.collectAsStateWithLifecycle()
        val postDetailError by postDetailViewModel.error.collectAsStateWithLifecycle()
        val hasError = postListError != null || postDetailError != null

        val shouldLoadContent = currentState.shouldLoad is ShouldLoadFirstPage ||
            currentState.shouldLoad is ShouldForceLoad ||
            currentState.shouldLoad is ShouldLoadNextPage

        val multiPanelEnabled by mainViewModel.state.collectAsStateWithLifecycle()
        val sidePanelVisible by remember {
            derivedStateOf { content is SidePanelContent && multiPanelEnabled.multiPanelEnabled }
        }

        val localView = LocalView.current
        val savedFeedback = stringResource(id = R.string.saved_filters_saved_feedback)

        LaunchedEffect(shouldLoadContent, currentState) {
            if (shouldLoadContent) postListViewModel.loadContent(currentState)
        }

        BookmarkListScreen(
            posts = currentState.posts,
            isLoading = (postListLoading || postDetailScreenState.isLoading) && !hasError,
            onScrollDirectionChanged = mainViewModel::setCurrentScrollDirection,
            onNextPageRequested = { appStateViewModel.runAction(GetNextPostPage) },
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
                localView.showBanner(savedFeedback)
            },
            onShareClicked = onShareClicked,
            onPullToRefresh = { appStateViewModel.runAction(Refresh()) },
            onPostClicked = { post -> appStateViewModel.runAction(ViewPost(post)) },
            onPostLongClicked = onPostLongClicked,
            onTagClicked = { post -> appStateViewModel.runAction(PostsForTag(post)) },
            showPostDescription = currentState.showDescription,
            sidePanelVisible = sidePanelVisible,
        )
    }
}

@Composable
fun BookmarkListScreen(
    posts: PostList?,
    isLoading: Boolean,
    onScrollDirectionChanged: (ScrollDirection) -> Unit,
    onNextPageRequested: () -> Unit,
    searchParameters: SearchParameters,
    onActiveSearchClicked: () -> Unit,
    onClearClicked: () -> Unit,
    onSaveClicked: () -> Unit,
    onShareClicked: (SearchParameters) -> Unit,
    onPullToRefresh: () -> Unit = {},
    onPostClicked: (Post) -> Unit,
    onPostLongClicked: (Post) -> Unit,
    onTagClicked: (Tag) -> Unit,
    showPostDescription: Boolean,
    sidePanelVisible: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        val (leftPadding, rightPadding) = WindowInsets.navigationBarsCompat
            .asHorizontalPaddingDp(addStart = 16.dp, addEnd = 16.dp)

        AnimatedVisibility(visible = searchParameters.isActive()) {
            ActiveSearch(
                onViewClicked = onActiveSearchClicked,
                onClearClicked = onClearClicked,
                onSaveClicked = onSaveClicked,
                onShareClicked = { onShareClicked(searchParameters) },
                modifier = Modifier.padding(
                    start = leftPadding,
                    end = if (sidePanelVisible) 16.dp else rightPadding,
                    bottom = 8.dp,
                ),
            )
        }

        AnimatedVisibility(visible = isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = leftPadding,
                        end = if (sidePanelVisible) 16.dp else rightPadding,
                        bottom = 8.dp,
                    ),
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

            var shouldScrollToTop by remember(posts) { mutableStateOf(posts.shouldScrollToTop) }

            LaunchedEffect(posts.canPaginate, shouldRequestNewPage) {
                if (posts.canPaginate && shouldRequestNewPage) currentOnNextPageRequested()
            }

            LaunchedEffect(scrollDirection) {
                currentOnScrollDirectionChanged(scrollDirection)
            }

            LaunchedEffect(shouldScrollToTop) {
                if (shouldScrollToTop) {
                    delay(200L)
                    listState.scrollToItem(index = 0)
                    shouldScrollToTop = false
                }
            }

            val (listLeftPadding, listRightPadding) = WindowInsets.navigationBarsCompat.asHorizontalPaddingDp()

            PullRefreshLayout(
                onPullToRefresh = onPullToRefresh,
                listState = listState,
                contentPadding = PaddingValues(
                    start = listLeftPadding,
                    top = 4.dp,
                    end = if (sidePanelVisible) 0.dp else listRightPadding,
                    bottom = 100.dp,
                ),
            ) {
                items(posts.list) { post ->
                    BookmarkItem(
                        post = post,
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
        val shape = RoundedCornerShape(size = 8.dp)
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

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun BookmarkItem(
    post: Post,
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
        shape = RoundedCornerShape(6.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(all = 8.dp),
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
                text = post.title,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )

            Text(
                text = stringResource(id = R.string.posts_saved_on, post.formattedTime),
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )

            if (post.private == true || post.readLater == true) {
                BookmarkFlags(
                    private = post.private,
                    readLater = post.readLater,
                )
            }

            if (showDescription && post.description.isNotBlank()) {
                TextWithBlockquote(
                    text = post.description,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(size = 6.dp)),
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    textSize = 14.sp,
                    maxLines = 5,
                    clickableLinks = false,
                )
            }

            if (!post.tags.isNullOrEmpty()) {
                val tags = remember(post.tags) {
                    post.tags.map { tag -> ChipGroup.Item(text = tag.name) }.toStableList()
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
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_sync),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
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
    private: Boolean?,
    readLater: Boolean?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (private == true) {
            Icon(
                painter = painterResource(id = R.drawable.ic_private),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = stringResource(id = R.string.posts_item_private),
                modifier = Modifier.padding(end = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            )
        }

        if (readLater == true) {
            Icon(
                painter = painterResource(id = R.drawable.ic_read_later),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = stringResource(id = R.string.posts_item_read_later),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

// region Previews
@Composable
@ThemePreviews
private fun BookmarkListScreenPreview(
    @PreviewParameter(provider = PostListProvider::class) posts: List<Post>,
) {
    ExtendedTheme {
        BookmarkListScreen(
            posts = PostList(
                list = posts,
                totalCount = posts.size,
                canPaginate = false,
                shouldScrollToTop = false,
            ),
            isLoading = true,
            onScrollDirectionChanged = {},
            onNextPageRequested = {},
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
                post = post,
                onPostClicked = {},
                onPostLongClicked = {},
                showDescription = true,
                onTagClicked = {},
            )
        }
    }
}
// endregion Previews
