package com.fibelatti.pinboard.features.posts.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.CrossfadeLoadingLayout
import com.fibelatti.pinboard.core.android.composable.EmptyListContent
import com.fibelatti.pinboard.core.android.composable.PullRefreshLayout
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.RefreshPopular
import com.fibelatti.pinboard.features.appstate.ViewPost
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.ui.components.ChipGroup
import com.fibelatti.ui.components.MultilineChipGroup
import com.fibelatti.ui.foundation.StableList
import com.fibelatti.ui.foundation.toStableList
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

        LaunchedEffect(popularPostsContent.shouldLoad) {
            if (popularPostsContent.shouldLoad) {
                popularPostsViewModel.getPosts()
            }
        }

        CrossfadeLoadingLayout(
            data = popularPostsContent.posts
                .takeUnless { popularPostsContent.shouldLoad || popularPostsLoading }
                ?.toStableList(),
            modifier = Modifier.fillMaxSize(),
        ) { posts ->
            PopularBookmarksContent(
                posts = posts,
                onPullToRefresh = { appStateViewModel.runAction(RefreshPopular) },
                onBookmarkClicked = { appStateViewModel.runAction(ViewPost(it)) },
                onBookmarkLongClicked = onBookmarkLongClicked,
            )
        }
    }
}

@Composable
fun PopularBookmarksContent(
    posts: StableList<Post>,
    onPullToRefresh: () -> Unit = {},
    onBookmarkClicked: (Post) -> Unit = {},
    onBookmarkLongClicked: (Post) -> Unit = {},
) {
    if (posts.value.isEmpty()) {
        EmptyListContent(
            icon = painterResource(id = R.drawable.ic_notes),
            title = stringResource(id = R.string.notes_empty_title),
            description = stringResource(id = R.string.notes_empty_description),
        )
    } else {
        PullRefreshLayout(
            onPullToRefresh = onPullToRefresh,
        ) {
            items(posts.value) { bookmark ->
                PopularBookmarkItem(
                    post = bookmark,
                    onPostClicked = onBookmarkClicked,
                    onPostLongClicked = onBookmarkLongClicked,
                )
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
                val items = remember(post.tags) {
                    post.tags.map { tag -> ChipGroup.Item(text = tag.name) }.toStableList()
                }
                MultilineChipGroup(
                    items = items,
                    onItemClick = {},
                )
            }
        }
    }
}

@Composable
@ThemePreviews
private fun PopularBookmarksContentPreview(
    @PreviewParameter(provider = PostListProvider::class) posts: List<Post>,
) {
    ExtendedTheme {
        PopularBookmarksContent(
            posts = posts.toStableList(),
        )
    }
}
