@file:Suppress("LongMethod", "LongParameterList")

package com.fibelatti.pinboard.features.posts.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.appstate.AddSearchTag
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.RefreshSearchTags
import com.fibelatti.pinboard.features.appstate.RemoveSearchTag
import com.fibelatti.pinboard.features.appstate.Search
import com.fibelatti.pinboard.features.appstate.SearchContent
import com.fibelatti.pinboard.features.appstate.SetTerm
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.domain.model.TagSorting
import com.fibelatti.pinboard.features.tags.presentation.TagList
import com.fibelatti.pinboard.features.tags.presentation.TagsViewModel
import com.fibelatti.ui.components.ChipGroup
import com.fibelatti.ui.components.SingleLineChipGroup
import com.fibelatti.ui.foundation.rememberKeyboardState
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun SearchBookmarksScreen(
    appStateViewModel: AppStateViewModel = hiltViewModel(),
    searchPostViewModel: SearchPostViewModel = hiltViewModel(),
    tagsViewModel: TagsViewModel = hiltViewModel(),
) {
    val appState by appStateViewModel.content.collectAsStateWithLifecycle()

    val searchContent = appState as? SearchContent ?: return

    val queryResultSize by searchPostViewModel.queryResultSize.collectAsStateWithLifecycle()

    val tagsState by tagsViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(searchContent.searchParameters) {
        if (searchContent.searchParameters.isActive()) {
            searchPostViewModel.searchParametersChanged(searchContent.searchParameters)
        }
    }

    LaunchedEffect(searchContent.shouldLoadTags, searchContent.availableTags) {
        if (searchContent.shouldLoadTags) {
            tagsViewModel.getAll(TagsViewModel.Source.SEARCH)
        } else {
            tagsViewModel.sortTags(searchContent.availableTags)
        }
    }

    SearchBookmarksScreen(
        searchTerm = searchContent.searchParameters.term,
        onSearchTermChanged = { newValue -> appStateViewModel.runAction(SetTerm(newValue)) },
        onKeyboardSearch = { appStateViewModel.runAction(Search) },
        selectedTags = searchContent.searchParameters.tags,
        onSelectedTagRemoved = { tag -> appStateViewModel.runAction(RemoveSearchTag(tag)) },
        activeSearchResult = if (searchContent.searchParameters.isActive()) {
            stringResource(id = R.string.search_result_size, queryResultSize)
        } else {
            ""
        },
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
        onAvailableTagClicked = { tag -> appStateViewModel.runAction(AddSearchTag(tag)) },
        onTagsPullToRefresh = { appStateViewModel.runAction(RefreshSearchTags) },
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
fun SearchBookmarksScreen(
    searchTerm: String = "",
    onSearchTermChanged: (String) -> Unit = {},
    onKeyboardSearch: () -> Unit = {},
    selectedTags: List<Tag> = emptyList(),
    onSelectedTagRemoved: (Tag) -> Unit = {},
    activeSearchResult: String = "",
    availableTags: List<Tag> = emptyList(),
    isLoadingTags: Boolean = false,
    onTagsSortOptionClicked: (TagList.Sorting) -> Unit = {},
    tagsSearchTerm: String = "",
    onTagsSearchInputChanged: (newValue: String) -> Unit = {},
    onAvailableTagClicked: (Tag) -> Unit = {},
    onTagsPullToRefresh: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        var tagSearchFocused by rememberSaveable { mutableStateOf(false) }
        val imeVisible by rememberKeyboardState()

        AnimatedVisibility(
            visible = !(tagSearchFocused && imeVisible),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                val keyboardController = LocalSoftwareKeyboardController.current

                OutlinedTextField(
                    value = searchTerm,
                    onValueChange = onSearchTermChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                    label = { Text(text = stringResource(id = R.string.search_term)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions {
                        // TODO: hide is not working
                        keyboardController?.hide()
                        onKeyboardSearch()
                    },
                    singleLine = true,
                    maxLines = 1,
                )

                Text(
                    text = stringResource(id = R.string.search_term_caveat),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = ExtendedTheme.typography.caveat,
                )
            }
        }

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
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    style = ExtendedTheme.typography.sectionTitle,
                )

                SingleLineChipGroup(
                    items = selectedTags.map {
                        ChipGroup.Item(
                            text = it.name,
                            icon = painterResource(id = R.drawable.ic_close)
                        )
                    },
                    onItemClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    onItemIconClick = { item ->
                        onSelectedTagRemoved(selectedTags.first { it.name == item.text })
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp),
                )
            }
        }

        AnimatedVisibility(
            visible = activeSearchResult.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Text(
                text = activeSearchResult,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(size = 8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = ExtendedTheme.typography.detail,
            )
        }

        Text(
            text = stringResource(id = R.string.search_tags),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            style = ExtendedTheme.typography.sectionTitle,
        )

        TagList(
            items = availableTags,
            isLoading = isLoadingTags,
            onSortOptionClicked = onTagsSortOptionClicked,
            searchQuery = tagsSearchTerm,
            onSearchInputChanged = onTagsSearchInputChanged,
            onSearchInputFocusChanged = { hasFocus -> tagSearchFocused = hasFocus },
            onTagClicked = onAvailableTagClicked,
            onPullToRefresh = onTagsPullToRefresh,
        )
    }
}

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
            activeSearchResult = "10 bookmarks match the current query",
            availableTags = listOf(Tag(name = "compose"), Tag(name = "ui"))
        )
    }
}
