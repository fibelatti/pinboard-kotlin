package com.fibelatti.pinboard.features.filters.presentation

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.EmptyListContent
import com.fibelatti.pinboard.core.android.composable.LaunchedErrorHandlerEffect
import com.fibelatti.pinboard.core.android.composable.SelectionDialogBottomSheet
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.features.appstate.ViewSavedFilter
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import com.fibelatti.ui.components.AppSheetState
import com.fibelatti.ui.components.ChipGroup
import com.fibelatti.ui.components.MultilineChipGroup
import com.fibelatti.ui.components.bottomSheetData
import com.fibelatti.ui.components.rememberAppSheetState
import com.fibelatti.ui.components.showBottomSheet
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun SavedFiltersScreen(
    modifier: Modifier = Modifier,
    savedFiltersViewModel: SavedFiltersViewModel = hiltViewModel(),
) {
    Surface(
        modifier = modifier,
        color = ExtendedTheme.colors.backgroundNoOverlay,
    ) {
        val savedFilters by savedFiltersViewModel.state.collectAsStateWithLifecycle()

        val localView = LocalView.current

        val savedFilterMenuSheetState = rememberAppSheetState()

        val error by savedFiltersViewModel.error.collectAsStateWithLifecycle()
        LaunchedErrorHandlerEffect(error = error, handler = savedFiltersViewModel::errorHandled)

        SavedFiltersScreen(
            savedFilters = savedFilters,
            onSavedFilterClicked = { savedFilter ->
                savedFiltersViewModel.runAction(ViewSavedFilter(savedFilter = savedFilter))
            },
            onSavedFilterLongClicked = { savedFilter ->
                savedFilterMenuSheetState.showBottomSheet(data = savedFilter)
            },
        )

        SavedFiltersQuickActionsBottomSheet(
            sheetState = savedFilterMenuSheetState,
            onDeleteClick = { savedFilter ->
                savedFiltersViewModel.deleteSavedFilter(savedFilter)
                localView.showBanner(R.string.saved_filters_deleted_feedback)
            },
        )
    }
}

@Composable
private fun SavedFiltersScreen(
    savedFilters: List<SavedFilter>,
    onSavedFilterClicked: (SavedFilter) -> Unit,
    onSavedFilterLongClicked: (SavedFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (savedFilters.isEmpty()) {
        EmptyListContent(
            icon = painterResource(id = R.drawable.ic_filter),
            title = stringResource(id = R.string.saved_filters_empty_title),
            description = stringResource(id = R.string.saved_filters_empty_description),
            modifier = modifier,
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = WindowInsets.safeDrawing
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                .add(WindowInsets(bottom = 100.dp))
                .asPaddingValues(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(savedFilters) { savedFilter ->
                SavedFilterItem(
                    savedFilter = savedFilter,
                    onClicked = onSavedFilterClicked,
                    onLongClicked = onSavedFilterLongClicked,
                )
            }
        }
    }
}

@Composable
private fun SavedFilterItem(
    savedFilter: SavedFilter,
    onClicked: (SavedFilter) -> Unit,
    onLongClicked: (SavedFilter) -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .combinedClickable(
                onClick = { onClicked(savedFilter) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClicked(savedFilter)
                },
            ),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (savedFilter.term.isNotBlank()) {
                Text(
                    text = stringResource(id = R.string.saved_filters_item_keywords),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )

                Text(
                    text = savedFilter.term,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            if (savedFilter.tags.isNotEmpty()) {
                Text(
                    text = stringResource(id = R.string.saved_filters_item_tags),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )

                val tags = remember(savedFilter.tags) {
                    savedFilter.tags.map { tag -> ChipGroup.Item(text = tag.name) }
                }

                MultilineChipGroup(
                    items = tags,
                    onItemClick = {},
                    modifier = Modifier.padding(top = 4.dp),
                    itemTextStyle = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                )
            }

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(
                        id = if (savedFilter.matchAll) {
                            R.string.search_advanced_match_all
                        } else {
                            R.string.search_advanced_match_any
                        },
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.bodyMedium,
                )

                Text(
                    text = "•",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )

                Text(
                    text = stringResource(
                        id = if (savedFilter.exactMatch) {
                            R.string.search_advanced_exact_match
                        } else {
                            R.string.search_advanced_partial_match
                        },
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun SavedFiltersQuickActionsBottomSheet(
    sheetState: AppSheetState,
    onDeleteClick: (SavedFilter) -> Unit,
) {
    val savedFilter: SavedFilter = sheetState.bottomSheetData() ?: return
    val localResources = LocalResources.current

    SelectionDialogBottomSheet(
        sheetState = sheetState,
        title = stringResource(R.string.quick_actions_title),
        options = SavedFiltersQuickActions.allOptions(savedFilter),
        optionName = { localResources.getString(it.title) },
        optionIcon = SavedFiltersQuickActions::icon,
        onOptionSelected = { option ->
            when (option) {
                is SavedFiltersQuickActions.Delete -> onDeleteClick(savedFilter)
            }
        },
    )
}

// region Previews
@Composable
@ThemePreviews
private fun SavedFilterScreenPreview(
    @PreviewParameter(provider = SavedFilterProvider::class) savedFilters: List<SavedFilter>,
) {
    ExtendedTheme {
        SavedFiltersScreen(
            savedFilters = remember { savedFilters },
            onSavedFilterClicked = {},
            onSavedFilterLongClicked = {},
            modifier = Modifier.safeDrawingPadding(),
        )
    }
}

@Composable
@ThemePreviews
private fun SavedFilterScreenEmptyPreview() {
    ExtendedTheme {
        SavedFiltersScreen(
            savedFilters = emptyList(),
            onSavedFilterClicked = {},
            onSavedFilterLongClicked = {},
            modifier = Modifier.safeDrawingPadding(),
        )
    }
}
// endregion Previews
