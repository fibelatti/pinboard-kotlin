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
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.bookmarking.core.AppMode
import com.fibelatti.bookmarking.features.appstate.AppStateViewModel
import com.fibelatti.bookmarking.features.appstate.PostsForTag
import com.fibelatti.bookmarking.features.appstate.RefreshTags
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.bookmarking.features.tags.domain.model.TagSorting
import com.fibelatti.bookmarking.features.tags.presentation.TagsViewModel
import com.fibelatti.core.randomUUID
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.EmptyListContent
import com.fibelatti.pinboard.core.android.composable.PullRefreshLayout
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.ui.foundation.asHorizontalPaddingDp
import com.fibelatti.ui.foundation.imeCompat
import com.fibelatti.ui.foundation.navigationBarsCompat
import com.fibelatti.ui.foundation.navigationBarsPaddingCompat
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun TagListScreen(
    appStateViewModel: AppStateViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinViewModel(),
    tagsViewModel: TagsViewModel = koinViewModel(),
    onBackPressed: () -> Unit,
    onError: (Throwable?, () -> Unit) -> Unit,
    onTagLongClicked: (Tag) -> Unit,
) {
    val appMode by appStateViewModel.appMode.collectAsStateWithLifecycle()
    val state by tagsViewModel.state.collectAsStateWithLifecycle()

    val screenTitle = stringResource(id = R.string.tags_title)
    val actionId = remember { randomUUID() }
    val localLifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        appStateViewModel.tagListContent
            .onEach { content ->
                if (content.shouldLoad) {
                    tagsViewModel.getAll(TagsViewModel.Source.MENU)
                } else {
                    tagsViewModel.sortTags(content.tags)
                }
            }
            .launchInAndFlowWith(localLifecycleOwner)

        mainViewModel.updateState { currentState ->
            currentState.copy(
                title = MainState.TitleComponent.Visible(screenTitle),
                subtitle = MainState.TitleComponent.Gone,
                navigation = MainState.NavigationComponent.Visible(actionId),
                bottomAppBar = MainState.BottomAppBarComponent.Gone,
                floatingActionButton = MainState.FabComponent.Gone,
            )
        }

        mainViewModel.navigationClicks(actionId)
            .onEach { onBackPressed() }
            .launchInAndFlowWith(localLifecycleOwner)

        tagsViewModel.error
            .onEach { throwable -> onError(throwable, tagsViewModel::errorHandled) }
            .launchInAndFlowWith(localLifecycleOwner)
    }

    TagList(
        header = {},
        items = state.filteredTags,
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
        onTagClicked = { appStateViewModel.runAction(PostsForTag(it)) },
        onTagLongClicked = { if (AppMode.PINBOARD == appMode) onTagLongClicked(it) },
        onPullToRefresh = { appStateViewModel.runAction(RefreshTags) },
    )
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
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
        val (leftPadding, rightPadding) = WindowInsets.navigationBarsCompat
            .asHorizontalPaddingDp(addStart = 16.dp, addEnd = 16.dp)

        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = leftPadding, end = rightPadding),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            val listState = rememberLazyListState()
            val showScrollToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 5 } }
            val paddingBottom = WindowInsets.imeCompat.asPaddingValues().calculateBottomPadding().coerceAtLeast(100.dp)

            PullRefreshLayout(
                onPullToRefresh = onPullToRefresh,
                listState = listState,
                contentPadding = WindowInsets(bottom = paddingBottom)
                    .add(WindowInsets.navigationBarsCompat)
                    .asPaddingValues(),
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
                    .padding(bottom = paddingBottom)
                    .navigationBarsPaddingCompat(),
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
@OptIn(ExperimentalMaterial3Api::class)
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

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            TagList.Sorting.entries.forEachIndexed { index, sorting ->
                SegmentedButton(
                    selected = index == selectedSortingIndex,
                    onClick = {
                        selectedSortingIndex = index
                        showFilter = sorting == TagList.Sorting.Search

                        onSortOptionClicked(sorting)

                        if (!showFilter) {
                            onSearchInputChanged("")
                            onSearchInputFocusChanged(false)
                        }
                    },
                    shape = when (index) {
                        0 -> RoundedCornerShape(
                            topStart = 16.dp,
                            bottomStart = 16.dp,
                        )

                        TagList.Sorting.entries.size - 1 -> RoundedCornerShape(
                            topEnd = 16.dp,
                            bottomEnd = 16.dp,
                        )

                        else -> RoundedCornerShape(0.dp)
                    },
                    label = {
                        Text(
                            text = stringResource(id = sorting.label),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                )
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

    enum class Sorting(val label: Int) {

        Alphabetically(label = R.string.tags_sorting_a_to_z),
        MoreFirst(label = R.string.tags_sorting_more_first),
        LessFirst(label = R.string.tags_sorting_less_first),
        Search(label = R.string.tags_sorting_filter),
        ;
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
