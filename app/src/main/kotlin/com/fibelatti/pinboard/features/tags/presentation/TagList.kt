package com.fibelatti.pinboard.features.tags.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.EmptyListContent
import com.fibelatti.pinboard.core.android.composable.PullRefreshLayout
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.domain.model.TagSorting
import com.fibelatti.ui.components.RowToggleButtonGroup
import com.fibelatti.ui.components.ToggleButtonGroup
import com.fibelatti.ui.foundation.StableList
import com.fibelatti.ui.foundation.toStableList
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import kotlinx.coroutines.launch

@Composable
fun TagListScreen(
    tagsViewModel: TagsViewModel = hiltViewModel(),
    onTagClicked: (Tag) -> Unit,
    onTagLongClicked: (Tag) -> Unit,
    onPullToRefresh: () -> Unit,
) {
    val state by tagsViewModel.state.collectAsStateWithLifecycle()

    TagList(
        header = {},
        items = state.filteredTags.toStableList(),
        isLoading = state.isLoading,
        modifier = Modifier.background(color = ExtendedTheme.colors.backgroundNoOverlay),
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
        searchInput = state.currentQuery,
        onSearchInputChanged = tagsViewModel::searchTags,
        onTagClicked = onTagClicked,
        onTagLongClicked = onTagLongClicked,
        onPullToRefresh = onPullToRefresh,
    )
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun TagList(
    header: @Composable LazyItemScope.() -> Unit,
    items: StableList<Tag>,
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
                    .padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            val listState = rememberLazyListState()
            val showScrollToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 5 } }
            val paddingBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding().coerceAtLeast(100.dp)

            PullRefreshLayout(
                onPullToRefresh = onPullToRefresh,
                listState = listState,
                paddingBottom = paddingBottom,
                verticalArrangement = Arrangement.Top,
            ) {
                item {
                    header()
                }

                if (items.value.isEmpty() && searchInput.isBlank()) {
                    item {
                        EmptyListContent(
                            icon = painterResource(id = R.drawable.ic_tag),
                            title = stringResource(id = R.string.tags_empty_title),
                            description = stringResource(id = R.string.tags_empty_description),
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

                    items(items.value) { item ->
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
                    .padding(bottom = paddingBottom),
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

        RowToggleButtonGroup(
            items = TagList.Sorting.values()
                .map { sorting ->
                    ToggleButtonGroup.Item(
                        id = sorting.id,
                        text = stringResource(id = sorting.label),
                    )
                }
                .toStableList(),
            onButtonClick = {
                val (index, sorting) = requireNotNull(TagList.Sorting.findByIdWithIndex(it.id))
                selectedSortingIndex = index
                showFilter = sorting == TagList.Sorting.Search

                onSortOptionClicked(sorting)

                if (!showFilter) {
                    onSearchInputChanged("")
                    onSearchInputFocusChanged(false)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            selectedIndex = selectedSortingIndex,
            buttonHeight = 40.dp,
            textStyle = MaterialTheme.typography.bodySmall,
        )

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
@OptIn(ExperimentalFoundationApi::class)
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
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        )
        Text(
            text = pluralStringResource(R.plurals.posts_quantity, item.posts, item.posts),
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onBackground,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun ScrollToTopButton(
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .padding(all = 16.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp),
            ),
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_top),
            contentDescription = stringResource(id = R.string.cd_scroll_to_top),
        )
    }
}

object TagList {

    enum class Sorting(val id: String, val label: Int) {

        Alphabetically(id = "alphabetically", label = R.string.tags_sorting_a_to_z),
        MoreFirst(id = "more-first", label = R.string.tags_sorting_more_first),
        LessFirst(id = "less-first", label = R.string.tags_sorting_less_first),
        Search(id = "search", label = R.string.tags_sorting_filter),
        ;

        companion object {

            fun findByIdWithIndex(id: String): IndexedValue<Sorting>? = values()
                .withIndex()
                .find { it.value.id == id }
        }
    }
}

// region Previews
@Composable
@ThemePreviews
private fun EmptyTagListPreview() {
    ExtendedTheme {
        TagList(
            header = {},
            items = StableList(),
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
            items = List(size = 5) { Tag(name = "Tag $it", posts = it * it) }.toStableList(),
            isLoading = false,
        )
    }
}
// endregion Previews
