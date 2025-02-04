@file:Suppress("unused")

package com.fibelatti.pinboard

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.ByDateModifiedNewestFirst
import com.fibelatti.pinboard.features.appstate.PostList
import com.fibelatti.pinboard.features.appstate.SearchParameters
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.presentation.BookmarkListScreen
import com.fibelatti.pinboard.features.posts.presentation.EditBookmarkScreen
import com.fibelatti.pinboard.features.posts.presentation.ScreenshotTestPostListProvider
import com.fibelatti.pinboard.features.posts.presentation.SearchBookmarksScreen
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.presentation.AuthScreen
import com.fibelatti.ui.theme.ExtendedTheme

internal class ScreenshotTests {

    @Composable
    @PreviewLightDark
    private fun AuthScreenPreview() {
        ExtendedTheme {
            AuthScreen(
                useLinkding = false,
                linkdingInstanceUrl = "",
                onUseLinkdingChanged = {},
                onAuthRequested = { _, _ -> },
                isLoading = false,
                apiTokenError = null,
                instanceUrlError = null,
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun AuthScreenLinkdingPreview() {
        ExtendedTheme {
            AuthScreen(
                useLinkding = true,
                linkdingInstanceUrl = "",
                onUseLinkdingChanged = {},
                onAuthRequested = { _, _ -> },
                isLoading = false,
                apiTokenError = null,
                instanceUrlError = null,
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun BookmarkListScreenPreview(
        @PreviewParameter(provider = ScreenshotTestPostListProvider::class) posts: List<Post>,
    ) {
        ExtendedTheme {
            Surface(
                color = ExtendedTheme.colors.backgroundNoOverlay,
            ) {
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
    }

    @Composable
    @PreviewLightDark
    private fun EditBookmarkScreenPreview(
        @PreviewParameter(provider = ScreenshotTestPostListProvider::class) posts: List<Post>,
    ) {
        ExtendedTheme {
            EditBookmarkScreen(
                appMode = AppMode.PINBOARD,
                post = posts.first(),
                isLoading = false,
                onUrlChanged = {},
                urlError = "",
                onTitleChanged = {},
                titleError = "",
                onDescriptionChanged = {},
                onNotesChanged = {},
                onPrivateChanged = {},
                onReadLaterChanged = {},
                searchTagInput = "",
                onSearchTagInputChanged = {},
                onAddTagClicked = {},
                suggestedTags = emptyList(),
                onSuggestedTagClicked = {},
                currentTagsTitle = stringResource(id = R.string.tags_added_title),
                currentTags = posts.first().tags.orEmpty(),
                onRemoveCurrentTagClicked = {},
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun ActiveSearchBookmarksScreenPreview() {
        ExtendedTheme {
            Surface(
                color = ExtendedTheme.colors.backgroundNoOverlay,
            ) {
                SearchBookmarksScreen(
                    selectedTags = listOf(Tag(name = "dev")),
                    availableTags = listOf(Tag(name = "compose"), Tag(name = "ui")),
                )
            }
        }
    }
}
