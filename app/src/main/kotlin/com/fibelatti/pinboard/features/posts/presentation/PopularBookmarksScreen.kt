package com.fibelatti.pinboard.features.posts.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
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
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.PopularPostDetailContent
import com.fibelatti.pinboard.features.appstate.PopularPostsContent
import com.fibelatti.pinboard.features.appstate.RefreshPopular
import com.fibelatti.pinboard.features.appstate.SidePanelContent
import com.fibelatti.pinboard.features.appstate.ViewPost
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.ui.components.ChipGroup
import com.fibelatti.ui.components.MultilineChipGroup
import com.fibelatti.ui.foundation.StableList
import com.fibelatti.ui.foundation.asHorizontalPaddingDp
import com.fibelatti.ui.foundation.navigationBarsCompat
import com.fibelatti.ui.foundation.toStableList
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import kotlinx.coroutines.flow.onEach
import java.util.UUID

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
            newValue = when (val current = content) {
                is PopularPostsContent -> current
                is PopularPostDetailContent -> current.previousContent
                else -> return@Surface
            },
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
                .takeUnless { popularPostsContent.shouldLoad || popularPostsScreenState.isLoading }
                ?.toStableList(),
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
    posts: StableList<Post>,
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
        val (listLeftPadding, listRightPadding) = WindowInsets.navigationBarsCompat.asHorizontalPaddingDp()

        PullRefreshLayout(
            onPullToRefresh = onPullToRefresh,
            contentPadding = PaddingValues(
                start = listLeftPadding,
                top = 4.dp,
                end = if (sidePanelVisible) 0.dp else listRightPadding,
                bottom = 100.dp,
            ),
        ) {
            items(posts) { bookmark ->
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
            Text(
                text = post.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )

            if (post.tags != null) {
                val items = remember(post.tags) {
                    post.tags.map { tag -> ChipGroup.Item(text = tag.name) }.toStableList()
                }
                MultilineChipGroup(
                    items = items,
                    onItemClick = {},
                    modifier = Modifier.padding(top = 8.dp),
                    itemTonalElevation = 16.dp,
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
