package com.fibelatti.pinboard.features.posts.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.AnimatedVisibilityProgressIndicator
import com.fibelatti.pinboard.core.android.composable.EmptyListContent
import com.fibelatti.pinboard.core.android.composable.rememberAutoDismissPullRefreshState
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.RefreshPopular
import com.fibelatti.pinboard.features.appstate.ViewPost
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.ui.components.ChipGroup
import com.fibelatti.ui.components.MultilineChipGroup
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun PopularBookmarksScreen(
    appStateViewModel: AppStateViewModel = hiltViewModel(),
    popularPostsViewModel: PopularPostsViewModel = hiltViewModel(),
    onBookmarkLongClicked: (Post) -> Unit = {},
) {
    Surface(
        color = ExtendedTheme.colors.backgroundNoOverlay,
    ) {
        val appState by appStateViewModel.popularPostsContent.collectAsStateWithLifecycle(initialValue = null)
        val popularPostsContent = appState ?: return@Surface

        val popularPostsLoading by popularPostsViewModel.loading.collectAsStateWithLifecycle(initialValue = false)

        LaunchedEffect(key1 = popularPostsContent.shouldLoad) {
            if (popularPostsContent.shouldLoad) {
                popularPostsViewModel.getPosts()
            }
        }

        PopularBookmarksScreen(
            bookmarks = popularPostsContent.posts,
            isLoading = popularPostsContent.shouldLoad || popularPostsLoading,
            onPullToRefresh = { appStateViewModel.runAction(RefreshPopular) },
            onBookmarkClicked = { appStateViewModel.runAction(ViewPost(it)) },
            onBookmarkLongClicked = onBookmarkLongClicked,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun PopularBookmarksScreen(
    bookmarks: List<Post>,
    isLoading: Boolean,
    onPullToRefresh: () -> Unit = {},
    onBookmarkClicked: (Post) -> Unit = {},
    onBookmarkLongClicked: (Post) -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = ExtendedTheme.colors.backgroundNoOverlay),
    ) {
        AnimatedVisibilityProgressIndicator(
            isVisible = isLoading,
            modifier = Modifier.fillMaxSize(),
        )

        AnimatedVisibility(
            visible = !isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            if (bookmarks.isEmpty()) {
                EmptyListContent(
                    icon = painterResource(id = R.drawable.ic_notes),
                    title = stringResource(id = R.string.notes_empty_title),
                    description = stringResource(id = R.string.notes_empty_description),
                )
            } else {
                val listState = rememberLazyListState()
                val (pullRefreshState, refreshing) = rememberAutoDismissPullRefreshState(onPullToRefresh)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = WindowInsets(bottom = 100.dp)
                            .add(WindowInsets.navigationBars)
                            .asPaddingValues(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        state = listState,
                    ) {
                        items(bookmarks) { bookmark ->
                            PopularBookmarkItem(
                                post = bookmark,
                                onPostClicked = onBookmarkClicked,
                                onPostLongClicked = onBookmarkLongClicked,
                            )
                        }
                    }

                    PullRefreshIndicator(
                        refreshing = refreshing,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter),
                        scale = true,
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun PopularBookmarkItem(
    post: Post,
    onPostClicked: (Post) -> Unit,
    onPostLongClicked: (Post) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onPostClicked(post) },
                onLongClick = { onPostLongClicked(post) },
            ),
        color = MaterialTheme.colorScheme.surface,
        elevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = post.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (post.tags != null) {
                MultilineChipGroup(
                    items = post.tags.map { tag -> ChipGroup.Item(text = tag.name) },
                    onItemClick = {},
                )
            }
        }
    }
}

@Composable
@ThemePreviews
private fun PopularBookmarksScreenPreview(
    @PreviewParameter(provider = PostListProvider::class) posts: List<Post>,
) {
    ExtendedTheme {
        PopularBookmarksScreen(
            bookmarks = posts,
            isLoading = false,
        )
    }
}
