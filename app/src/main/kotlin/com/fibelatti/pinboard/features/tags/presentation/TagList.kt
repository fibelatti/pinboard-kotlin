@file:Suppress("LongMethod", "MagicNumber")

package com.fibelatti.pinboard.features.tags.presentation

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.customview.EmptyListLayout
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.domain.model.TagSorting
import com.fibelatti.ui.components.RowToggleButtonGroup
import com.fibelatti.ui.components.ToggleButtonGroup
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TagList(
    tagsViewModel: TagsViewModel = hiltViewModel(),
    onTagClicked: (Tag) -> Unit = {},
    onTagLongClicked: (Tag) -> Unit = {},
    onPullToRefresh: () -> Unit = {},
) {
    val state by tagsViewModel.state.collectAsStateWithLifecycle()

    TagList(
        items = state.filteredTags,
        isLoading = state.isLoading,
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
        searchQuery = state.currentQuery,
        onSearchInputFocusChanged = tagsViewModel::searchFocusChanged,
        onSearchInputChanged = tagsViewModel::searchTags,
        onTagClicked = onTagClicked,
        onTagLongClicked = onTagLongClicked,
        onPullToRefresh = onPullToRefresh,
    )
}

@Composable
fun TagList(
    items: List<Tag>,
    isLoading: Boolean,
    onSortOptionClicked: (TagList.Sorting) -> Unit = {},
    searchQuery: String = "",
    onSearchInputChanged: (newValue: String) -> Unit = {},
    onSearchInputFocusChanged: (hasFocus: Boolean) -> Unit = {},
    onTagClicked: (Tag) -> Unit = {},
    onTagLongClicked: (Tag) -> Unit = {},
    onPullToRefresh: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            LinearProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }

        if (items.isEmpty() && searchQuery.isBlank()) {
            AndroidView(
                factory = { context: Context ->
                    EmptyListLayout(context).apply {
                        setIcon(R.drawable.ic_tag)
                        setTitle(R.string.tags_empty_title)
                        setDescription(R.string.tags_empty_description)
                    }
                }
            )
        } else {
            TagList(
                items = items,
                onSortOptionClicked = onSortOptionClicked,
                onSearchInputChanged = onSearchInputChanged,
                onSearchInputFocusChanged = onSearchInputFocusChanged,
                onTagClicked = onTagClicked,
                onTagLongClicked = onTagLongClicked,
                onPullToRefresh = onPullToRefresh,
            )
        }
    }
}

@Composable
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class,
)
private fun TagList(
    items: List<Tag>,
    onSortOptionClicked: (TagList.Sorting) -> Unit,
    onSearchInputChanged: (newValue: String) -> Unit,
    onSearchInputFocusChanged: (hasFocus: Boolean) -> Unit,
    onTagClicked: (Tag) -> Unit,
    onTagLongClicked: (Tag) -> Unit,
    onPullToRefresh: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var showFilter by rememberSaveable { mutableStateOf(false) }

    RowToggleButtonGroup(
        items = TagList.Sorting.values().map { sorting ->
            ToggleButtonGroup.Item(
                id = sorting.id,
                text = stringResource(id = sorting.label),
            )
        },
        onButtonClick = {
            val sorting = requireNotNull(TagList.Sorting.findById(it.id))
            showFilter = sorting == TagList.Sorting.Search

            onSortOptionClicked(sorting)

            scope.launch {
                listState.scrollToItem(index = 0)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        selectedIndex = 0,
        buttonHeight = 40.dp,
        textStyle = ExtendedTheme.typography.caveat,
    )

    var currentQuery by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    AnimatedVisibility(
        visible = showFilter,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        OutlinedTextField(
            value = currentQuery,
            onValueChange = { newValue ->
                currentQuery = newValue
                onSearchInputChanged(newValue)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .onFocusChanged { onSearchInputFocusChanged(it.hasFocus) },
            label = { Text(text = stringResource(id = R.string.tag_filter_hint)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            singleLine = true,
            maxLines = 1,
        )
    }

    if (!showFilter) {
        focusManager.clearFocus()
        currentQuery = ""
    }

    var refreshing by rememberSaveable { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            scope.launch {
                refreshing = true
                onPullToRefresh()
                delay(300L)
                refreshing = false
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            state = listState,
        ) {
            items(items.size) { index ->
                TagListItem(
                    item = items[index],
                    onTagClicked = onTagClicked,
                    onTagLongClicked = onTagLongClicked
                )
            }
        }

        PullRefreshIndicator(
            refreshing = refreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            scale = true,
        )
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun TagListItem(
    item: Tag,
    onTagClicked: (Tag) -> Unit,
    onTagLongClicked: (Tag) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onTagClicked(item) },
                onLongClick = { onTagLongClicked(item) },
            )
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = item.name,
            color = MaterialTheme.colorScheme.secondary,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = ExtendedTheme.typography.listItem,
        )
        Text(
            text = pluralStringResource(R.plurals.posts_quantity, item.posts, item.posts),
            color = MaterialTheme.colorScheme.onBackground,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = ExtendedTheme.typography.detail,
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

            fun findById(id: String): Sorting? = values().find { it.id == id }
        }
    }
}

@Composable
@ThemePreviews
private fun EmptyTagListPreview() {
    ExtendedTheme {
        TagList(
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
            items = List(size = 5) { Tag(name = "Tag $it", posts = it * it) },
            isLoading = false,
        )
    }
}
