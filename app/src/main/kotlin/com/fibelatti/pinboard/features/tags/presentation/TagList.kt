package com.fibelatti.pinboard.features.tags.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.android.composable.EmptyListContent
import com.fibelatti.pinboard.core.android.composable.ErrorHandlerEffect
import com.fibelatti.pinboard.core.android.composable.PullRefreshLayout
import com.fibelatti.pinboard.core.android.composable.SelectionDialogBottomSheet
import com.fibelatti.pinboard.core.android.composable.rememberJsonSaver
import com.fibelatti.pinboard.core.android.icons.AppIcons
import com.fibelatti.pinboard.core.android.icons.Tag
import com.fibelatti.pinboard.features.appstate.PostsForTag
import com.fibelatti.pinboard.features.appstate.RefreshTags
import com.fibelatti.pinboard.features.main.MainBottomAppBar
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.domain.model.TagSorting
import com.fibelatti.ui.components.AutoSizeText
import com.fibelatti.ui.components.ListItem
import com.fibelatti.ui.components.rememberAppSheetState
import com.fibelatti.ui.foundation.Shapes
import com.fibelatti.ui.foundation.pxToDp
import com.fibelatti.ui.preview.PreviewAll
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun TagListScreen(
    modifier: Modifier = Modifier,
    tagsViewModel: TagsViewModel = hiltViewModel(),
) {
    val appState by tagsViewModel.appState.collectAsStateWithLifecycle()
    val tagsState by tagsViewModel.state.collectAsStateWithLifecycle()

    var quickActionTag: Tag? by rememberSaveable(stateSaver = rememberJsonSaver(Tag.serializer())) {
        mutableStateOf(null)
    }

    val tagQuickActionsSheetState = rememberAppSheetState()
    val renameTagSheetState = rememberAppSheetState()

    val localResources = LocalResources.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val error by tagsViewModel.error.collectAsStateWithLifecycle()
    ErrorHandlerEffect(error = error, handler = tagsViewModel::errorHandled)

    DisposableEffect(Unit) {
        onDispose { keyboardController?.hide() }
    }

    TagList(
        header = {},
        items = tagsState.filteredTags,
        isLoading = tagsState.isLoading,
        modifier = modifier.background(color = ExtendedTheme.colors.backgroundNoOverlay),
        onSortOptionClick = { sorting ->
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
        onSearchInputChange = tagsViewModel::searchTags,
        onTagClick = { tagsViewModel.runAction(PostsForTag(it)) },
        onTagLongClick = { tag ->
            if (AppMode.PINBOARD == appState.appMode) {
                quickActionTag = tag
                tagQuickActionsSheetState.showBottomSheet()
            }
        },
        onPullToRefresh = { tagsViewModel.runAction(RefreshTags) },
    )

    quickActionTag?.let { tag ->
        SelectionDialogBottomSheet(
            sheetState = tagQuickActionsSheetState,
            title = stringResource(R.string.quick_actions_title),
            options = TagQuickActions.allOptions(tag = tag),
            optionName = { localResources.getString(it.title) },
            optionIcon = TagQuickActions::icon,
            onOptionSelect = { option ->
                when (option) {
                    is TagQuickActions.Rename -> renameTagSheetState.showBottomSheet()
                }
            },
        )

        RenameTagBottomSheet(
            sheetState = renameTagSheetState,
            tag = tag,
            onRename = tagsViewModel::renameTag,
        )
    }
}

@Composable
fun TagList(
    header: @Composable LazyItemScope.() -> Unit,
    items: List<Tag>,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    onSortOptionClick: (TagList.Sorting) -> Unit = {},
    searchInput: String = "",
    onSearchInputChange: (newValue: String) -> Unit = {},
    onSearchInputFocusChange: (hasFocus: Boolean) -> Unit = {},
    onTagClick: (Tag) -> Unit = {},
    onTagLongClick: (Tag) -> Unit = {},
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
            val windowInsets: WindowInsets = WindowInsets.safeDrawing
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                .add(WindowInsets(top = 8.dp, bottom = MainBottomAppBar.ContentClearance))
            var stickyHeaderHeight: Int by remember { mutableIntStateOf(0) }

            PullRefreshLayout(
                onPullToRefresh = onPullToRefresh,
                // `contentPadding` does not consume insets, so items applying `safeDrawing`
                // themselves (such as `EmptyListContent`) would otherwise inset twice.
                modifier = Modifier.consumeWindowInsets(windowInsets),
                contentPadding = windowInsets.asPaddingValues(),
                verticalArrangement = Arrangement.spacedBy(space = 1.dp, alignment = Alignment.Top),
                scrollToTopPadding = stickyHeaderHeight.pxToDp(),
            ) {
                item(key = "header") {
                    header()
                }

                if (items.isEmpty() && searchInput.isBlank()) {
                    item(key = "empty-list") {
                        EmptyListContent(
                            icon = AppIcons.Tag,
                            title = stringResource(id = R.string.tags_empty_title),
                            description = stringResource(id = R.string.tags_empty_description),
                            scrollable = false,
                        )
                    }
                } else {
                    stickyHeader(key = "sorting-controls") {
                        TagListSortingControls(
                            onSortOptionClick = onSortOptionClick,
                            onSearchInputChange = onSearchInputChange,
                            onSearchInputFocusChange = onSearchInputFocusChange,
                            searchInput = searchInput,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .onSizeChanged { stickyHeaderHeight = it.height },
                        )
                    }

                    itemsIndexed(items, key = { _, item -> item.hashCode() }) { idx, item ->
                        TagListItem(
                            item = item,
                            onTagClick = onTagClick,
                            onTagLongClick = onTagLongClick,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            shape = when (idx) {
                                0 if items.size == 1 -> Shapes.StandaloneShape
                                0 -> Shapes.TopShape
                                items.size - 1 -> Shapes.BottomShape
                                else -> Shapes.MiddleShape
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TagListSortingControls(
    onSortOptionClick: (TagList.Sorting) -> Unit,
    onSearchInputChange: (newValue: String) -> Unit,
    onSearchInputFocusChange: (hasFocus: Boolean) -> Unit,
    searchInput: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
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

                        onSortOptionClick(sorting)

                        if (!showFilter) {
                            onSearchInputChange("")
                            onSearchInputFocusChange(false)
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
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
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
                onValueChange = onSearchInputChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .onFocusChanged { onSearchInputFocusChange(it.hasFocus) },
                label = { Text(text = stringResource(id = R.string.tag_filter_hint)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions { focusManager.clearFocus() },
                singleLine = true,
                maxLines = 1,
                shape = Shapes.StandaloneShape,
            )
        }
    }
}

@Composable
private fun TagListItem(
    item: Tag,
    onTagClick: (Tag) -> Unit,
    onTagLongClick: (Tag) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = Shapes.StandaloneShape,
) {
    val haptic: HapticFeedback = LocalHapticFeedback.current
    ListItem(
        headlineText = item.name,
        supportingText = pluralStringResource(R.plurals.posts_quantity, item.posts, item.posts),
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .combinedClickable(
                onClick = { onTagClick(item) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTagLongClick(item)
                },
            ),
        shape = shape,
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
@PreviewAll
private fun EmptyTagListPreview() {
    ExtendedTheme {
        TagList(
            header = {},
            items = emptyList(),
            isLoading = false,
            modifier = Modifier.safeDrawingPadding(),
        )
    }
}

@Composable
@PreviewAll
private fun TagListPreview() {
    ExtendedTheme {
        TagList(
            header = {},
            items = List(size = 5) { Tag(name = "Tag $it", posts = it * it) },
            isLoading = false,
            modifier = Modifier.safeDrawingPadding(),
        )
    }
}
// endregion Previews
