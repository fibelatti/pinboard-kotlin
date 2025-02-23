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
import androidx.compose.ui.Modifier
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.fibelatti.core.android.extension.hideKeyboard
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.LaunchedErrorHandlerEffect
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
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import java.util.UUID
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun SearchBookmarksScreen(
    modifier: Modifier = Modifier,
    appStateViewModel: AppStateViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
    searchPostViewModel: SearchPostViewModel = hiltViewModel(),
    tagsViewModel: TagsViewModel = hiltViewModel(),
) {
    Surface(
        modifier = modifier,
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
                    navigation = MainState.NavigationComponent.Visible(),
                    bottomAppBar = MainState.BottomAppBarComponent.Visible(
                        id = actionId,
                        menuItems = if (isActive) {
                            listOf(
                                MainState.MenuItemComponent.ClearSearch,
                                MainState.MenuItemComponent.SaveSearch,
                            )
                        } else {
                            emptyList()
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
        val localLifecycle = LocalLifecycleOwner.current.lifecycle

        BackHandler {
            appStateViewModel.runAction(Search)
        }

        LaunchedEffect(Unit) {
            mainViewModel.menuItemClicks(actionId)
                .onEach { (menuItem, data) ->
                    when (menuItem) {
                        is MainState.MenuItemComponent.ClearSearch -> {
                            appStateViewModel.runAction(ClearSearch)
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

            mainViewModel.fabClicks(actionId)
                .onEach { appStateViewModel.runAction(Search) }
                .flowWithLifecycle(localLifecycle)
                .launchIn(this)
        }

        val error by tagsViewModel.error.collectAsStateWithLifecycle()
        LaunchedErrorHandlerEffect(error = error, handler = tagsViewModel::errorHandled)

        DisposableEffect(Unit) {
            onDispose {
                localView.hideKeyboard()
            }
        }

        SearchBookmarksScreen(
            searchTerm = searchContent.searchParameters.term,
            onSearchTermChanged = { newValue -> appStateViewModel.runAction(SetTerm(newValue)) },
            onKeyboardSearch = { appStateViewModel.runAction(Search) },
            selectedTags = searchContent.searchParameters.tags,
            onSelectedTagRemoved = { tag -> appStateViewModel.runAction(RemoveSearchTag(tag)) },
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
