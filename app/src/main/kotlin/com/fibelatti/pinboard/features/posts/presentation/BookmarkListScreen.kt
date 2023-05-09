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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig.DEFAULT_PAGE_SIZE
import com.fibelatti.pinboard.core.android.composable.EmptyListContent
import com.fibelatti.pinboard.core.android.composable.PullRefreshLayout
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.ClearSearch
import com.fibelatti.pinboard.features.appstate.GetNextPostPage
import com.fibelatti.pinboard.features.appstate.Loaded
import com.fibelatti.pinboard.features.appstate.PostList
import com.fibelatti.pinboard.features.appstate.PostsForTag
import com.fibelatti.pinboard.features.appstate.Refresh
import com.fibelatti.pinboard.features.appstate.SearchParameters
import com.fibelatti.pinboard.features.appstate.ShouldForceLoad
import com.fibelatti.pinboard.features.appstate.ShouldLoadFirstPage
import com.fibelatti.pinboard.features.appstate.ShouldLoadNextPage
import com.fibelatti.pinboard.features.appstate.ViewPost
import com.fibelatti.pinboard.features.posts.domain.model.PendingSync
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.ui.components.ChipGroup
import com.fibelatti.ui.components.MultilineChipGroup
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import kotlinx.coroutines.delay

@Composable
fun BookmarkListScreen(
    appStateViewModel: AppStateViewModel = hiltViewModel(),
    postListViewModel: PostListViewModel = hiltViewModel(),
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
    onPostLongClicked: (Post) -> Unit,
    onShareClicked: (SearchParameters) -> Unit,
) {
    Surface(
        color = ExtendedTheme.colors.backgroundNoOverlay,
    ) {
        val appState by appStateViewModel.postListContent.collectAsStateWithLifecycle(initialValue = null)
        val postListContent = appState ?: return@Surface

        val postListLoading = postListContent.shouldLoad != Loaded
        val postDetailLoading by postDetailViewModel.loading.collectAsStateWithLifecycle(initialValue = false)

        val postListError by postListViewModel.error.collectAsStateWithLifecycle()
        val postDetailError by postDetailViewModel.error.collectAsStateWithLifecycle()
        val hasError = postListError != null || postDetailError != null

        val shouldLoadContent = postListContent.shouldLoad is ShouldLoadFirstPage ||
            postListContent.shouldLoad is ShouldForceLoad ||
            postListContent.shouldLoad is ShouldLoadNextPage

        LaunchedEffect(postListContent, shouldLoadContent) {
            if (shouldLoadContent) postListViewModel.loadContent(postListContent)
        }

        BookmarkListScreen(
            posts = postListContent.posts,
            isLoading = (postListLoading || postDetailLoading) && !hasError,
            onNextPageRequested = { appStateViewModel.runAction(GetNextPostPage) },
            searchParameters = postListContent.searchParameters,
            onClearClicked = { appStateViewModel.runAction(ClearSearch) },
            onShareClicked = onShareClicked,
            onPullToRefresh = { appStateViewModel.runAction(Refresh()) },
            onPostClicked = { post -> appStateViewModel.runAction(ViewPost(post)) },
            onPostLongClicked = onPostLongClicked,
            onTagClicked = { post -> appStateViewModel.runAction(PostsForTag(post)) },
            showPostDescription = postListContent.showDescription,
        )
    }
}

@Composable
fun BookmarkListScreen(
    posts: PostList?,
    isLoading: Boolean,
    onNextPageRequested: () -> Unit,
    searchParameters: SearchParameters,
    onClearClicked: () -> Unit,
    onShareClicked: (SearchParameters) -> Unit,
    onPullToRefresh: () -> Unit = {},
    onPostClicked: (Post) -> Unit,
    onPostLongClicked: (Post) -> Unit,
    onTagClicked: (Tag) -> Unit,
    showPostDescription: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        AnimatedVisibility(visible = searchParameters.isActive()) {
            ActiveSearch(
                onClearClicked = onClearClicked,
                onShareClicked = { onShareClicked(searchParameters) },
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            )
        }

        AnimatedVisibility(visible = isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
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
            val shouldRequestNewPage by remember {
                derivedStateOf {
                    listState.layoutInfo.run {
                        visibleItemsInfo.isNotEmpty() &&
                            visibleItemsInfo.last().index >= totalItemsCount - (DEFAULT_PAGE_SIZE / 2)
                    }
                }
            }

            LaunchedEffect(posts.canPaginate, shouldRequestNewPage) {
                if (posts.canPaginate && shouldRequestNewPage) onNextPageRequested()
            }

            PullRefreshLayout(
                onPullToRefresh = onPullToRefresh,
                listState = listState,
                nestedScroll = true,
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

            LaunchedEffect(searchParameters) {
                delay(200L)
                listState.scrollToItem(index = 0)
            }
        }
    }
}

@Composable
private fun ActiveSearch(
    onClearClicked: () -> Unit,
    onShareClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val minHeight = 36.dp
        val corner = RoundedCornerShape(size = 8.dp)

        Box(
            modifier = Modifier
                .heightIn(min = minHeight)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = corner,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(id = R.string.search_active),
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        FilledTonalButton(
            onClick = onClearClicked,
            modifier = Modifier
                .heightIn(min = minHeight)
                .wrapContentWidth(),
            shape = corner,
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )

            Spacer(modifier = Modifier.size(4.dp))

            Text(
                text = stringResource(id = R.string.search_clear),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(modifier = Modifier.size(8.dp))

        FilledTonalButton(
            onClick = onShareClicked,
            modifier = Modifier
                .heightIn(min = minHeight)
                .wrapContentWidth(),
            shape = corner,
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_share),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )

            Spacer(modifier = Modifier.size(4.dp))

            Text(
                text = stringResource(id = R.string.search_share),
                style = MaterialTheme.typography.bodyMedium,
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
            .combinedClickable(
                onClick = { onPostClicked(post) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onPostLongClicked(post)
                },
            ),
        elevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            if (post.pendingSync != null) {
                PendingSync(post.pendingSync)
            }

            Text(
                text = post.title,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )

            Text(
                text = stringResource(id = R.string.posts_saved_on, post.formattedTime),
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )

            if (post.private || post.readLater) {
                BookmarkFlags(
                    private = post.private,
                    readLater = post.readLater,
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (showDescription && post.description.isNotBlank()) {
                Text(
                    text = post.description,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 5,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (!post.tags.isNullOrEmpty()) {
                val tags = remember(post.tags) {
                    post.tags.map { tag -> ChipGroup.Item(text = tag.name) }
                }

                MultilineChipGroup(
                    items = tags,
                    onItemClick = { item -> onTagClicked(post.tags.first { tag -> tag.name == item.text }) },
                )
            }
        }
    }
}

@Composable
private fun PendingSync(
    pendingSync: PendingSync,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_pending_sync),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = when (pendingSync) {
                PendingSync.ADD -> stringResource(id = R.string.posts_pending_add)
                PendingSync.UPDATE -> stringResource(id = R.string.posts_pending_update)
                PendingSync.DELETE -> stringResource(id = R.string.posts_pending_delete)
            },
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
        )
    }
}

@Composable
private fun BookmarkFlags(
    private: Boolean,
    readLater: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (private) {
            Icon(
                painter = painterResource(id = R.drawable.ic_private),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = stringResource(id = R.string.posts_item_private),
                modifier = Modifier.padding(end = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        if (readLater) {
            Icon(
                painter = painterResource(id = R.drawable.ic_read_later),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = stringResource(id = R.string.posts_item_read_later),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
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
            ),
            isLoading = true,
            onNextPageRequested = {},
            searchParameters = SearchParameters(term = "bookmark"),
            onClearClicked = {},
            onShareClicked = {},
            onPullToRefresh = {},
            onPostClicked = {},
            onPostLongClicked = {},
            showPostDescription = true,
            onTagClicked = {},
        )
    }
}

@Composable
@ThemePreviews
private fun ActiveSearchPreview() {
    ExtendedTheme {
        Box {
            ActiveSearch(
                onClearClicked = {},
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
