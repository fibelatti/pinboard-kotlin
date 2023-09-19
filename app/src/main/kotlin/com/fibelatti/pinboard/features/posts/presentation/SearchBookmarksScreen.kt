package com.fibelatti.pinboard.features.posts.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AddSearchTag
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.ClearSearch
import com.fibelatti.pinboard.features.appstate.RefreshSearchTags
import com.fibelatti.pinboard.features.appstate.RemoveSearchTag
import com.fibelatti.pinboard.features.appstate.Search
import com.fibelatti.pinboard.features.appstate.SetTerm
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.domain.model.TagSorting
import com.fibelatti.pinboard.features.tags.presentation.TagList
import com.fibelatti.pinboard.features.tags.presentation.TagsViewModel
import com.fibelatti.ui.components.ChipGroup
import com.fibelatti.ui.components.SingleLineChipGroup
import com.fibelatti.ui.foundation.StableList
import com.fibelatti.ui.foundation.stableListOf
import com.fibelatti.ui.foundation.toStableList
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import kotlinx.coroutines.flow.onEach
import java.util.UUID

@Composable
fun SearchBookmarksScreen(
    appStateViewModel: AppStateViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
    searchPostViewModel: SearchPostViewModel = hiltViewModel(),
    tagsViewModel: TagsViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    onError: (Throwable?, () -> Unit) -> Unit,
) {
    Surface(
        color = ExtendedTheme.colors.backgroundNoOverlay,
    ) {
        val appState by appStateViewModel.searchContent.collectAsStateWithLifecycle(initialValue = null)
        val searchContent by rememberUpdatedState(newValue = appState ?: return@Surface)

        val tagsState by tagsViewModel.state.collectAsStateWithLifecycle()

        val title = stringResource(id = R.string.search_title)
        val actionId = remember { UUID.randomUUID().toString() }
        val queryResultSize by searchPostViewModel.queryResultSize.collectAsStateWithLifecycle()
        val activeSearchLabel = stringResource(id = R.string.search_result_size, queryResultSize)

        LaunchedEffect(searchContent, queryResultSize) {
            val isActive = searchContent.searchParameters.isActive()

            if (isActive) {
                searchPostViewModel.searchParametersChanged(searchContent.searchParameters)
            }

            mainViewModel.updateState { mainState ->
                mainState.copy(
                    title = MainState.TitleComponent.Visible(title),
                    subtitle = if (isActive) {
                        MainState.TitleComponent.Visible(label = activeSearchLabel)
                    } else {
                        MainState.TitleComponent.Gone
                    },
                    navigation = MainState.NavigationComponent.Visible(actionId),
                    bottomAppBar = MainState.BottomAppBarComponent.Visible(
                        id = actionId,
                        menuItems = if (isActive) {
                            stableListOf(
                                MainState.MenuItemComponent.ClearSearch,
                                MainState.MenuItemComponent.SaveSearch,
                            )
                        } else {
                            stableListOf()
                        },
                        data = SavedFilter(
                            searchTerm = searchContent.searchParameters.term,
                            tags = searchContent.searchParameters.tags,
                        ),
                    ),
                    floatingActionButton = MainState.FabComponent.Visible(actionId, R.drawable.ic_search),
                )
            }
        }

        LaunchedEffect(searchContent.shouldLoadTags, searchContent.availableTags) {
            if (searchContent.shouldLoadTags) {
                tagsViewModel.getAll(TagsViewModel.Source.SEARCH)
            } else {
                tagsViewModel.sortTags(searchContent.availableTags)
            }
        }

        val localView = LocalView.current
        val localLifecycleOwner = LocalLifecycleOwner.current
        val savedFeedback = stringResource(id = R.string.saved_filters_saved_feedback)

        LaunchedEffect(Unit) {
            mainViewModel.navigationClicks(actionId)
                .onEach { onBackPressed() }
                .launchInAndFlowWith(localLifecycleOwner)

            mainViewModel.menuItemClicks(actionId)
                .onEach { (menuItem, data) ->
                    when (menuItem) {
                        is MainState.MenuItemComponent.ClearSearch -> {
                            appStateViewModel.runAction(ClearSearch)
                        }

                        is MainState.MenuItemComponent.SaveSearch -> {
                            (data as? SavedFilter)?.let(searchPostViewModel::saveFilter)
                            localView.showBanner(savedFeedback)
                        }

                        else -> Unit
                    }
                }
                .launchInAndFlowWith(localLifecycleOwner)

            mainViewModel.fabClicks(actionId)
                .onEach { appStateViewModel.runAction(Search) }
                .launchInAndFlowWith(localLifecycleOwner)

            tagsViewModel.error
                .onEach { throwable -> onError(throwable, tagsViewModel::errorHandled) }
                .launchInAndFlowWith(localLifecycleOwner)
        }

        DisposableEffect(Unit) {
            onDispose {
                localView.hideKeyboard()
            }
        }

        SearchBookmarksScreen(
            searchTerm = searchContent.searchParameters.term,
            onSearchTermChanged = { newValue -> appStateViewModel.runAction(SetTerm(newValue)) },
            onKeyboardSearch = { appStateViewModel.runAction(Search) },
            selectedTags = searchContent.searchParameters.tags.toStableList(),
            onSelectedTagRemoved = { tag -> appStateViewModel.runAction(RemoveSearchTag(tag)) },
            availableTags = tagsState.filteredTags.toStableList(),
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
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun SearchBookmarksScreen(
    searchTerm: String = "",
    onSearchTermChanged: (String) -> Unit = {},
    onKeyboardSearch: () -> Unit = {},
    selectedTags: StableList<Tag> = StableList(),
    onSelectedTagRemoved: (Tag) -> Unit = {},
    availableTags: StableList<Tag> = StableList(),
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

            var searchTermField by remember {
                mutableStateOf(TextFieldValue(text = searchTerm, selection = TextRange(searchTerm.length)))
            }

            OutlinedTextField(
                value = searchTermField,
                onValueChange = { newValue ->
                    searchTermField = newValue
                    onSearchTermChanged(newValue.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                label = { Text(text = stringResource(id = R.string.search_term)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions {
                    keyboardController?.hide()
                    onKeyboardSearch()
                },
                singleLine = true,
                maxLines = 1,
            )

            AnimatedVisibility(
                visible = selectedTags.value.isNotEmpty(),
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
                        style = MaterialTheme.typography.titleSmall,
                    )

                    SingleLineChipGroup(
                        items = selectedTags.value
                            .map {
                                ChipGroup.Item(
                                    text = it.name,
                                    icon = painterResource(id = R.drawable.ic_close),
                                )
                            }
                            .toStableList(),
                        onItemClick = { item ->
                            onSelectedTagRemoved(selectedTags.value.first { it.name == item.text })
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        onItemIconClick = { item ->
                            onSelectedTagRemoved(selectedTags.value.first { it.name == item.text })
                        },
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
                style = MaterialTheme.typography.titleSmall,
            )
        },
        items = availableTags,
        isLoading = isLoadingTags,
        onSortOptionClicked = onTagsSortOptionClicked,
        searchInput = tagsSearchTerm,
        onSearchInputChanged = onTagsSearchInputChanged,
        onTagClicked = onAvailableTagClicked,
        onPullToRefresh = onTagsPullToRefresh,
    )
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
            selectedTags = stableListOf(Tag(name = "dev")),
            availableTags = stableListOf(Tag(name = "compose"), Tag(name = "ui")),
        )
    }
}
