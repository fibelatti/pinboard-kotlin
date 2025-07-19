package com.fibelatti.pinboard.features.posts.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.LaunchedErrorHandlerEffect
import com.fibelatti.pinboard.core.android.composable.RememberedEffect
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.features.appstate.AddSearchTag
import com.fibelatti.pinboard.features.appstate.ClearSearch
import com.fibelatti.pinboard.features.appstate.RefreshSearchTags
import com.fibelatti.pinboard.features.appstate.RemoveSearchTag
import com.fibelatti.pinboard.features.appstate.Search
import com.fibelatti.pinboard.features.appstate.SearchContent
import com.fibelatti.pinboard.features.appstate.SetTerm
import com.fibelatti.pinboard.features.appstate.ViewRandomSearch
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import com.fibelatti.pinboard.features.main.MainState
import com.fibelatti.pinboard.features.main.MainViewModel
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.domain.model.TagSorting
import com.fibelatti.pinboard.features.tags.presentation.TagList
import com.fibelatti.pinboard.features.tags.presentation.TagsViewModel
import com.fibelatti.ui.components.ChipGroup
import com.fibelatti.ui.components.SingleLineChipGroup
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun SearchBookmarksScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel(),
    searchPostViewModel: SearchPostViewModel = hiltViewModel(),
    tagsViewModel: TagsViewModel = hiltViewModel(),
) {
    Surface(
        modifier = modifier,
        color = ExtendedTheme.colors.backgroundNoOverlay,
    ) {
        val searchContent by searchPostViewModel.searchContent.collectAsStateWithLifecycle(null)
        val currentContent by rememberUpdatedState(newValue = searchContent ?: return@Surface)

        val tagsState by tagsViewModel.state.collectAsStateWithLifecycle()

        BackHandler {
            searchPostViewModel.runAction(Search)
        }

        val localView = LocalView.current
        val localLifecycle = LocalLifecycleOwner.current.lifecycle
        val keyboardController = LocalSoftwareKeyboardController.current

        LaunchedEffect(Unit) {
            mainViewModel.actionButtonClicks(contentType = SearchContent::class)
                .onEach { mainViewModel.runAction(ViewRandomSearch) }
                .flowWithLifecycle(localLifecycle)
                .launchIn(this)

            mainViewModel.menuItemClicks(contentType = SearchContent::class)
                .onEach { (menuItem, data) ->
                    when (menuItem) {
                        is MainState.MenuItemComponent.ClearSearch -> {
                            mainViewModel.runAction(ClearSearch)
                        }

                        is MainState.MenuItemComponent.SaveSearch -> {
                            (data as? SavedFilter)?.let(searchPostViewModel::saveFilter)
                            localView.showBanner(R.string.saved_filters_saved_feedback)
                        }

                        else -> Unit
                    }
                }
                .flowWithLifecycle(localLifecycle)
                .launchIn(this)

            mainViewModel.fabClicks(contentType = SearchContent::class)
                .onEach { mainViewModel.runAction(Search) }
                .flowWithLifecycle(localLifecycle)
                .launchIn(this)
        }

        val error by tagsViewModel.error.collectAsStateWithLifecycle()
        LaunchedErrorHandlerEffect(error = error, handler = tagsViewModel::errorHandled)

        DisposableEffect(Unit) {
            onDispose {
                keyboardController?.hide()
            }
        }

        SearchBookmarksScreen(
            searchTerm = currentContent.searchParameters.term,
            onSearchTermChanged = { newValue -> searchPostViewModel.runAction(SetTerm(newValue)) },
            onKeyboardSearch = { searchPostViewModel.runAction(Search) },
            selectedTags = currentContent.searchParameters.tags,
            onSelectedTagRemoved = { tag -> searchPostViewModel.runAction(RemoveSearchTag(tag)) },
            availableTags = tagsState.filteredTags,
            isLoadingTags = tagsState.isLoading,
            onTagsSortOptionClicked = { sorting ->
                tagsViewModel.sortTags(
                    sorting = when (sorting) {
                        TagList.Sorting.Alphabetically -> TagSorting.AtoZ
                        TagList.Sorting.MoreFirst -> TagSorting.MoreFirst
                        TagList.Sorting.LessFirst -> TagSorting.LessFirst
                        TagList.Sorting.Search -> TagSorting.AtoZ
                    },
                    searchQuery = "",
                )
            },
            tagsSearchTerm = tagsState.currentQuery,
            onTagsSearchInputChanged = tagsViewModel::searchTags,
            onAvailableTagClicked = { tag -> searchPostViewModel.runAction(AddSearchTag(tag)) },
            onTagsPullToRefresh = { searchPostViewModel.runAction(RefreshSearchTags) },
        )
    }
}

@Composable
fun SearchBookmarksScreen(
    modifier: Modifier = Modifier,
    searchTerm: String = "",
    onSearchTermChanged: (String) -> Unit = {},
    onKeyboardSearch: () -> Unit = {},
    selectedTags: List<Tag> = emptyList(),
    onSelectedTagRemoved: (Tag) -> Unit = {},
    availableTags: List<Tag> = emptyList(),
    isLoadingTags: Boolean = false,
    onTagsSortOptionClicked: (TagList.Sorting) -> Unit = {},
    tagsSearchTerm: String = "",
    onTagsSearchInputChanged: (newValue: String) -> Unit = {},
    onAvailableTagClicked: (Tag) -> Unit = {},
    onTagsPullToRefresh: () -> Unit = {},
) {
    TagList(
        header = {
            val keyboardController = LocalSoftwareKeyboardController.current
            val searchTermFieldState = rememberTextFieldState(initialText = searchTerm)

            RememberedEffect(searchTermFieldState.text) {
                onSearchTermChanged(searchTermFieldState.text.toString())
            }

            OutlinedTextField(
                state = searchTermFieldState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                label = { Text(text = stringResource(id = R.string.search_term)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                onKeyboardAction = KeyboardActionHandler {
                    keyboardController?.hide()
                    onKeyboardSearch()
                },
                lineLimits = TextFieldLineLimits.SingleLine,
                contentPadding = OutlinedTextFieldDefaults.contentPadding(
                    start = 8.dp,
                    end = 8.dp,
                    bottom = 8.dp,
                ),
            )

            AnimatedVisibility(
                visible = selectedTags.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(id = R.string.search_selected_tags),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                    )

                    SingleLineChipGroup(
                        items = selectedTags.map {
                            ChipGroup.Item(
                                text = it.name,
                                icon = painterResource(id = R.drawable.ic_close),
                            )
                        },
                        onItemClick = { item ->
                            onSelectedTagRemoved(selectedTags.first { it.name == item.text })
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        onItemIconClick = { item ->
                            onSelectedTagRemoved(selectedTags.first { it.name == item.text })
                        },
                        itemTextStyle = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                    )
                }
            }

            Text(
                text = stringResource(id = R.string.search_tags),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        items = availableTags,
        isLoading = isLoadingTags,
        modifier = modifier,
        onSortOptionClicked = onTagsSortOptionClicked,
        searchInput = tagsSearchTerm,
        onSearchInputChanged = onTagsSearchInputChanged,
        onTagClicked = onAvailableTagClicked,
        onPullToRefresh = onTagsPullToRefresh,
    )
}

// region Previews
@Composable
@ThemePreviews
private fun DefaultSearchBookmarksScreenPreview() {
    ExtendedTheme {
        SearchBookmarksScreen()
    }
}

@Composable
@ThemePreviews
private fun ActiveSearchBookmarksScreenPreview() {
    ExtendedTheme {
        SearchBookmarksScreen(
            selectedTags = listOf(Tag(name = "dev")),
            availableTags = listOf(Tag(name = "compose"), Tag(name = "ui")),
        )
    }
}
// endregion Previews
