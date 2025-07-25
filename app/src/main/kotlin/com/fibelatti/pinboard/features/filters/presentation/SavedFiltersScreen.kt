package com.fibelatti.pinboard.features.filters.presentation

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.SelectionDialog
import com.fibelatti.pinboard.core.android.composable.EmptyListContent
import com.fibelatti.pinboard.core.android.composable.LaunchedErrorHandlerEffect
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.features.appstate.ViewSavedFilter
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import com.fibelatti.ui.components.ChipGroup
import com.fibelatti.ui.components.MultilineChipGroup
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

        val localContext = LocalContext.current
        val localView = LocalView.current

        val error by savedFiltersViewModel.error.collectAsStateWithLifecycle()
        LaunchedErrorHandlerEffect(error = error, handler = savedFiltersViewModel::errorHandled)

        SavedFiltersScreen(
            savedFilters = savedFilters,
            onSavedFilterClicked = { savedFilter ->
                savedFiltersViewModel.runAction(ViewSavedFilter(savedFilter = savedFilter))
            },
            onSavedFilterLongClicked = { savedFilter ->
                SelectionDialog.show(
                    context = localContext,
                    title = localContext.getString(R.string.quick_actions_title),
                    options = SavedFiltersQuickActions.allOptions(savedFilter),
                    optionName = { localContext.getString(it.title) },
                    optionIcon = SavedFiltersQuickActions::icon,
                    onOptionSelected = { option ->
                        when (option) {
                            is SavedFiltersQuickActions.Delete -> {
                                savedFiltersViewModel.deleteSavedFilter(savedFilter)
                                localView.showBanner(R.string.saved_filters_deleted_feedback)
                            }
                        }
                    },
                )
            },
        )
    }
}

@Composable
private fun SavedFiltersScreen(
    savedFilters: List<SavedFilter>,
    onSavedFilterClicked: (SavedFilter) -> Unit,
    onSavedFilterLongClicked: (SavedFilter) -> Unit,
) {
    if (savedFilters.isEmpty()) {
        EmptyListContent(
            icon = painterResource(id = R.drawable.ic_filter),
            title = stringResource(id = R.string.saved_filters_empty_title),
            description = stringResource(id = R.string.saved_filters_empty_description),
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
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
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (savedFilter.searchTerm.isNotBlank()) {
                Text(
                    text = stringResource(id = R.string.saved_filters_item_keywords),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )

                Text(
                    text = savedFilter.searchTerm,
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
        }
    }
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
        )
    }
}
// endregion Previews
