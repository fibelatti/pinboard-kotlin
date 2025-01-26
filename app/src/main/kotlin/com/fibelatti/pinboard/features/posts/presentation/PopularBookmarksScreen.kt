package com.fibelatti.pinboard.features.posts.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.CrossfadeLoadingLayout
import com.fibelatti.pinboard.core.android.composable.EmptyListContent
import com.fibelatti.pinboard.core.android.composable.PullRefreshLayout
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.PopularPostsContent
import com.fibelatti.pinboard.features.appstate.RefreshPopular
import com.fibelatti.pinboard.features.appstate.SidePanelContent
import com.fibelatti.pinboard.features.appstate.ViewPost
import com.fibelatti.pinboard.features.appstate.find
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import java.util.UUID
import kotlinx.coroutines.flow.onEach

@Composable
fun PopularBookmarksScreen(
    appStateViewModel: AppStateViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
    popularPostsViewModel: PopularPostsViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    onError: (Throwable?, () -> Unit) -> Unit,
    onBookmarkLongClicked: (Post) -> Unit = {},
) {
    Surface(
        color = ExtendedTheme.colors.backgroundNoOverlay,
    ) {
        val content by appStateViewModel.content.collectAsStateWithLifecycle()
        val popularPostsContent by rememberUpdatedState(
            newValue = content.find<PopularPostsContent>() ?: return@Surface,
        )

        val popularPostsScreenState by popularPostsViewModel.screenState.collectAsStateWithLifecycle()

        val multiPanelEnabled by mainViewModel.state.collectAsStateWithLifecycle()
        val sidePanelVisible by remember {
            derivedStateOf { content is SidePanelContent && multiPanelEnabled.multiPanelEnabled }
        }

        val actionId = remember { UUID.randomUUID().toString() }

        val localContext = LocalContext.current
        val localView = LocalView.current
        val localLifecycleOwner = LocalLifecycleOwner.current

        LaunchedEffect(content) {
            if (!(content is PopularPostsContent || sidePanelVisible)) return@LaunchedEffect
            mainViewModel.updateState { currentState ->
                currentState.copy(
                    title = MainState.TitleComponent.Visible(localContext.getString(R.string.popular_title)),
                    subtitle = MainState.TitleComponent.Gone,
                    navigation = MainState.NavigationComponent.Visible(actionId),
                    bottomAppBar = MainState.BottomAppBarComponent.Gone,
                    floatingActionButton = MainState.FabComponent.Gone,
                )
            }
        }

        LaunchedEffect(popularPostsContent.shouldLoad) {
            if (popularPostsContent.shouldLoad) {
                popularPostsViewModel.getPosts()
            }
        }

        LaunchedEffect(popularPostsScreenState.savedMessage) {
            popularPostsScreenState.savedMessage?.let { messageRes ->
                localView.showBanner(localContext.getString(messageRes))
                popularPostsViewModel.userNotified()
            }
        }

        LaunchedEffect(Unit) {
            mainViewModel.navigationClicks(actionId)
                .onEach { onBackPressed() }
                .launchInAndFlowWith(localLifecycleOwner)

            popularPostsViewModel.error
                .onEach { throwable -> onError(throwable, popularPostsViewModel::errorHandled) }
                .launchInAndFlowWith(localLifecycleOwner)
        }

        CrossfadeLoadingLayout(
            data = popularPostsContent.posts
                .takeUnless { popularPostsContent.shouldLoad || popularPostsScreenState.isLoading },
            modifier = Modifier.fillMaxSize(),
        ) { posts ->
            PopularBookmarksContent(
                posts = posts,
                onPullToRefresh = { appStateViewModel.runAction(RefreshPopular) },
                onBookmarkClicked = { appStateViewModel.runAction(ViewPost(it)) },
                onBookmarkLongClicked = onBookmarkLongClicked,
                sidePanelVisible = sidePanelVisible,
            )
        }
    }
}

@Composable
fun PopularBookmarksContent(
    posts: Map<Post, Int>,
    onPullToRefresh: () -> Unit = {},
    onBookmarkClicked: (Post) -> Unit = {},
    onBookmarkLongClicked: (Post) -> Unit = {},
    sidePanelVisible: Boolean = false,
) {
    if (posts.isEmpty()) {
        EmptyListContent(
            icon = painterResource(id = R.drawable.ic_pin),
            title = stringResource(id = R.string.posts_empty_title),
            description = stringResource(id = R.string.posts_empty_description),
        )
    } else {
        val windowInsets = WindowInsets.safeDrawing
            .only(if (sidePanelVisible) WindowInsetsSides.Start else WindowInsetsSides.Horizontal)
            .add(WindowInsets(top = 4.dp, bottom = 100.dp))

        PullRefreshLayout(
            onPullToRefresh = onPullToRefresh,
            contentPadding = windowInsets.asPaddingValues(),
        ) {
            items(posts.keys.toList()) { bookmark ->
                PopularBookmarkItem(
                    post = bookmark,
                    count = posts.getOrDefault(bookmark, defaultValue = 1),
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
    count: Int,
    onPostClicked: (Post) -> Unit,
    onPostLongClicked: (Post) -> Unit,
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
            Text(
                text = post.displayTitle,
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

            Text(
                text = pluralStringResource(R.plurals.popular_count, count, count),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
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
            posts = posts.associateWith { 1 },
        )
    }
}
