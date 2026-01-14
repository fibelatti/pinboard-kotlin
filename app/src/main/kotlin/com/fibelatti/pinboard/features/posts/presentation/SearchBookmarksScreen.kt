@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.fibelatti.pinboard.features.posts.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.LaunchedErrorHandlerEffect
import com.fibelatti.pinboard.core.android.composable.RememberedEffect
import com.fibelatti.pinboard.core.android.composable.SettingToggle
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.features.appstate.AddSearchTag
import com.fibelatti.pinboard.features.appstate.ClearSearch
import com.fibelatti.pinboard.features.appstate.RefreshSearchTags
import com.fibelatti.pinboard.features.appstate.RemoveSearchTag
import com.fibelatti.pinboard.features.appstate.Search
import com.fibelatti.pinboard.features.appstate.SearchContent
import com.fibelatti.pinboard.features.appstate.SearchParameters
import com.fibelatti.pinboard.features.appstate.SetAdvancedSearchParameters
import com.fibelatti.pinboard.features.appstate.SetTerm
import com.fibelatti.pinboard.features.appstate.ViewRandomSearch
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import com.fibelatti.pinboard.features.main.MainState
import com.fibelatti.pinboard.features.main.MainViewModel
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.domain.model.TagSorting
import com.fibelatti.pinboard.features.tags.presentation.TagList
import com.fibelatti.pinboard.features.tags.presentation.TagsViewModel
import com.fibelatti.ui.components.AppBottomSheet
import com.fibelatti.ui.components.AutoSizeText
import com.fibelatti.ui.components.ChipGroup
import com.fibelatti.ui.components.SingleLineChipGroup
import com.fibelatti.ui.components.rememberAppSheetState
import com.fibelatti.ui.components.showBottomSheet
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
        val searchContent: SearchContent? by searchPostViewModel.searchContent.collectAsStateWithLifecycle(null)
        val currentContent: SearchContent by rememberUpdatedState(newValue = searchContent ?: return@Surface)

        val tagsState: TagsViewModel.State by tagsViewModel.state.collectAsStateWithLifecycle()

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

        val error: Throwable? by tagsViewModel.error.collectAsStateWithLifecycle()
        LaunchedErrorHandlerEffect(error = error, handler = tagsViewModel::errorHandled)

        DisposableEffect(Unit) {
            onDispose {
                keyboardController?.hide()
            }
        }

        SearchBookmarksScreen(
            searchParameters = currentContent.searchParameters,
            onSearchTermChanged = { newValue -> searchPostViewModel.runAction(SetTerm(newValue)) },
            onKeyboardSearch = { searchPostViewModel.runAction(Search) },
            onSelectedTagRemoved = { tag -> searchPostViewModel.runAction(RemoveSearchTag(tag)) },
            onUpdateSearchParameters = { matchAll: Boolean, exactMatch: Boolean ->
                searchPostViewModel.runAction(SetAdvancedSearchParameters(matchAll, exactMatch))
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
            onAvailableTagClicked = { tag -> searchPostViewModel.runAction(AddSearchTag(tag)) },
            onTagsPullToRefresh = { searchPostViewModel.runAction(RefreshSearchTags) },
        )
    }
}

@Composable
private fun SearchBookmarksScreen(
    searchParameters: SearchParameters,
    modifier: Modifier = Modifier,
    onSearchTermChanged: (String) -> Unit = {},
    onKeyboardSearch: () -> Unit = {},
    onSelectedTagRemoved: (Tag) -> Unit = {},
    onUpdateSearchParameters: (matchAll: Boolean, exactMatch: Boolean) -> Unit = { _, _ -> },
    availableTags: List<Tag> = emptyList(),
    isLoadingTags: Boolean = false,
    onTagsSortOptionClicked: (TagList.Sorting) -> Unit = {},
    tagsSearchTerm: String = "",
    onTagsSearchInputChanged: (newValue: String) -> Unit = {},
    onAvailableTagClicked: (Tag) -> Unit = {},
    onTagsPullToRefresh: () -> Unit = {},
) {
    val advancedDialogState = rememberAppSheetState()

    TagList(
        header = {
            val keyboardController = LocalSoftwareKeyboardController.current
            val focusRequester = remember { FocusRequester() }
            val searchTermFieldState = rememberTextFieldState(initialText = searchParameters.term)

            RememberedEffect(searchTermFieldState.text) {
                onSearchTermChanged(searchTermFieldState.text.toString())
            }

            RememberedEffect(Unit) {
                focusRequester.requestFocus()
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                OutlinedTextField(
                    state = searchTermFieldState,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    label = {
                        AutoSizeText(
                            text = stringResource(id = R.string.search_term),
                            maxLines = 1,
                        )
                    },
                    trailingIcon = {
                        if (searchTermFieldState.text.isNotEmpty()) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = null,
                                modifier = Modifier.clickable(onClick = searchTermFieldState::clearText),
                            )
                        }
                    },
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

                IconButton(
                    onClick = { advancedDialogState.showBottomSheet() },
                    shapes = IconButtonDefaults.shapes(),
                    modifier = Modifier.padding(bottom = 4.dp, end = 8.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_preferences),
                        contentDescription = stringResource(R.string.search_advanced_content_description),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            AnimatedVisibility(
                visible = searchParameters.tags.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                SingleLineChipGroup(
                    items = searchParameters.tags.map {
                        ChipGroup.Item(
                            text = it.name,
                            icon = painterResource(id = R.drawable.ic_close),
                        )
                    },
                    onItemClick = { item ->
                        onSelectedTagRemoved(searchParameters.tags.first { it.name == item.text })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    onItemIconClick = { item ->
                        onSelectedTagRemoved(searchParameters.tags.first { it.name == item.text })
                    },
                    itemTextStyle = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                    contentPadding = PaddingValues(end = 16.dp),
                    header = {
                        Box(
                            modifier = Modifier
                                .fillParentMaxHeight()
                                .heightIn(min = ChipGroup.MinSize)
                                .background(
                                    color = ExtendedTheme.colors.backgroundNoOverlay,
                                    shape = MaterialTheme.shapes.small.copy(
                                        topStart = CornerSize(0.dp),
                                        bottomStart = CornerSize(0.dp),
                                    ),
                                )
                                .padding(start = 16.dp, end = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(id = R.string.search_selected_tags, searchParameters.tags.size),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.search_tags),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall,
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

    AppBottomSheet(
        sheetState = advancedDialogState,
    ) {
        SettingToggle(
            title = stringResource(R.string.search_advanced_match_all),
            description = stringResource(R.string.search_advanced_match_all_description),
            checked = searchParameters.matchAll,
            onCheckedChange = { matchAll ->
                onUpdateSearchParameters(matchAll, searchParameters.exactMatch)
            },
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingToggle(
            title = stringResource(R.string.search_advanced_exact_match),
            description = stringResource(R.string.search_advanced_exact_match_description),
            checked = searchParameters.exactMatch,
            onCheckedChange = { exactMatch ->
                onUpdateSearchParameters(searchParameters.matchAll, exactMatch)
            },
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// region Previews
@Composable
@ThemePreviews
private fun DefaultSearchBookmarksScreenPreview() {
    ExtendedTheme {
        SearchBookmarksScreen(
            searchParameters = SearchParameters(),
            modifier = Modifier.safeDrawingPadding(),
        )
    }
}

@Composable
@ThemePreviews
private fun ActiveSearchBookmarksScreenPreview() {
    ExtendedTheme {
        SearchBookmarksScreen(
            modifier = Modifier.safeDrawingPadding(),
            searchParameters = SearchParameters(tags = listOf(Tag(name = "dev"))),
            availableTags = listOf(Tag(name = "compose"), Tag(name = "ui")),
        )
    }
}
// endregion Previews
