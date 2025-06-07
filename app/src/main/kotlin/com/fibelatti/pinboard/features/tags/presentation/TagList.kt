@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.fibelatti.pinboard.features.tags.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.android.SelectionDialog
import com.fibelatti.pinboard.core.android.composable.EmptyListContent
import com.fibelatti.pinboard.core.android.composable.LaunchedErrorHandlerEffect
import com.fibelatti.pinboard.core.android.composable.LongClickIconButton
import com.fibelatti.pinboard.core.android.composable.PullRefreshLayout
import com.fibelatti.pinboard.features.appstate.PostsForTag
import com.fibelatti.pinboard.features.appstate.RefreshTags
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.domain.model.TagSorting
import com.fibelatti.ui.components.AutoSizeText
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import kotlinx.coroutines.launch

@Composable
fun TagListScreen(
    modifier: Modifier = Modifier,
    tagsViewModel: TagsViewModel = hiltViewModel(),
) {
    val appState by tagsViewModel.appState.collectAsStateWithLifecycle()
    val tagsState by tagsViewModel.state.collectAsStateWithLifecycle()

    val localContext = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val error by tagsViewModel.error.collectAsStateWithLifecycle()
    LaunchedErrorHandlerEffect(error = error, handler = tagsViewModel::errorHandled)

    DisposableEffect(Unit) {
        onDispose { keyboardController?.hide() }
    }

    TagList(
        header = {},
        items = tagsState.filteredTags,
        isLoading = tagsState.isLoading,
        modifier = modifier.background(color = ExtendedTheme.colors.backgroundNoOverlay),
        onSortOptionClicked = { sorting ->
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
        searchInput = tagsState.currentQuery,
        onSearchInputChanged = tagsViewModel::searchTags,
        onTagClicked = { tagsViewModel.runAction(PostsForTag(it)) },
        onTagLongClicked = { tag ->
            if (AppMode.PINBOARD == appState.appMode) {
                SelectionDialog.show(
                    context = localContext,
                    title = localContext.getString(R.string.quick_actions_title),
                    options = TagQuickActions.allOptions(tag),
                    optionName = { localContext.getString(it.title) },
                    optionIcon = TagQuickActions::icon,
                    onOptionSelected = { option ->
                        when (option) {
                            is TagQuickActions.Rename -> {
                                RenameTagDialog.show(
                                    context = localContext,
                                    tag = option.tag,
                                    onRename = tagsViewModel::renameTag,
                                )
                            }
                        }
                    },
                )
            }
        },
        onPullToRefresh = { tagsViewModel.runAction(RefreshTags) },
    )
}

@Composable
fun TagList(
    header: @Composable LazyItemScope.() -> Unit,
    items: List<Tag>,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    onSortOptionClicked: (TagList.Sorting) -> Unit = {},
    searchInput: String = "",
    onSearchInputChanged: (newValue: String) -> Unit = {},
    onSearchInputFocusChanged: (hasFocus: Boolean) -> Unit = {},
    onTagClicked: (Tag) -> Unit = {},
    onTagLongClicked: (Tag) -> Unit = {},
    onPullToRefresh: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing
                            .add(WindowInsets(left = 16.dp, right = 16.dp))
                            .only(WindowInsetsSides.Horizontal),
                    ),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            val listState = rememberLazyListState()
            val showScrollToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 5 } }

            val windowInsets = WindowInsets.safeDrawing
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                .add(WindowInsets(bottom = 100.dp))

            PullRefreshLayout(
                onPullToRefresh = onPullToRefresh,
                listState = listState,
                contentPadding = windowInsets.asPaddingValues(),
                verticalArrangement = Arrangement.Top,
            ) {
                item {
                    header()
                }

                if (items.isEmpty() && searchInput.isBlank()) {
                    item {
                        EmptyListContent(
                            icon = painterResource(id = R.drawable.ic_tag),
                            title = stringResource(id = R.string.tags_empty_title),
                            description = stringResource(id = R.string.tags_empty_description),
                            scrollable = false,
                        )
                    }
                } else {
                    stickyHeader {
                        TagListSortingControls(
                            onSortOptionClicked = onSortOptionClicked,
                            onSearchInputChanged = onSearchInputChanged,
                            onSearchInputFocusChanged = onSearchInputFocusChanged,
                            searchInput = searchInput,
                        )
                    }

                    items(items) { item ->
                        TagListItem(
                            item = item,
                            onTagClicked = onTagClicked,
                            onTagLongClicked = onTagLongClicked,
                        )
                    }
                }
            }

            this@Column.AnimatedVisibility(
                visible = showScrollToTop,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .windowInsetsPadding(windowInsets),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
            ) {
                val scope = rememberCoroutineScope()

                ScrollToTopButton(
                    onClick = {
                        scope.launch {
                            listState.animateScrollToItem(index = 0)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun TagListSortingControls(
    onSortOptionClicked: (TagList.Sorting) -> Unit,
    onSearchInputChanged: (newValue: String) -> Unit,
    onSearchInputFocusChanged: (hasFocus: Boolean) -> Unit,
    searchInput: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = ExtendedTheme.colors.backgroundNoOverlay),
    ) {
        var selectedSortingIndex by rememberSaveable { mutableIntStateOf(0) }
        var showFilter by rememberSaveable { mutableStateOf(false) }
        val focusManager = LocalFocusManager.current

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        ) {
            TagList.Sorting.entries.forEachIndexed { index, sorting ->
                val weight by animateFloatAsState(
                    targetValue = if (selectedSortingIndex == index) 1.2f else 1f,
                )

                ToggleButton(
                    checked = index == selectedSortingIndex,
                    onCheckedChange = {
                        selectedSortingIndex = index
                        showFilter = sorting == TagList.Sorting.Search

                        onSortOptionClicked(sorting)

                        if (!showFilter) {
                            onSearchInputChanged("")
                            onSearchInputFocusChanged(false)
                        }
                    },
                    modifier = Modifier
                        .weight(weight)
                        .semantics { role = Role.RadioButton },
                    shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        TagList.Sorting.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                ) {
                    AutoSizeText(
                        text = stringResource(id = sorting.label),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showFilter,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            OutlinedTextField(
                value = searchInput,
                onValueChange = onSearchInputChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .onFocusChanged { onSearchInputFocusChanged(it.hasFocus) },
                label = { Text(text = stringResource(id = R.string.tag_filter_hint)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions { focusManager.clearFocus() },
                singleLine = true,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun TagListItem(
    item: Tag,
    onTagClicked: (Tag) -> Unit,
    onTagLongClicked: (Tag) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onTagClicked(item) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTagLongClicked(item)
                },
            )
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = item.name,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.secondary,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = pluralStringResource(R.plurals.posts_quantity, item.posts, item.posts),
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onBackground,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ScrollToTopButton(
    onClick: () -> Unit,
) {
    LongClickIconButton(
        painter = painterResource(id = R.drawable.ic_chevron_top),
        description = stringResource(id = R.string.cd_scroll_to_top),
        onClick = onClick,
        modifier = Modifier
            .padding(all = 16.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                shape = MaterialTheme.shapes.large,
            ),
    )
}

object TagList {

    enum class Sorting(val label: Int) {

        Alphabetically(label = R.string.tags_sorting_a_to_z),
        MoreFirst(label = R.string.tags_sorting_more_first),
        LessFirst(label = R.string.tags_sorting_less_first),
        Search(label = R.string.tags_sorting_filter),
    }
}

// region Previews
@Composable
@ThemePreviews
private fun EmptyTagListPreview() {
    ExtendedTheme {
        TagList(
            header = {},
            items = emptyList(),
            isLoading = false,
        )
    }
}

@Composable
@ThemePreviews
private fun TagListPreview() {
    ExtendedTheme {
        TagList(
            header = {},
            items = List(size = 5) { Tag(name = "Tag $it", posts = it * it) },
            isLoading = false,
        )
    }
}
// endregion Previews
