@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.fibelatti.pinboard.features.notes.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.EmptyListContent
import com.fibelatti.pinboard.core.android.composable.LaunchedErrorHandlerEffect
import com.fibelatti.pinboard.core.android.composable.LoadingContent
import com.fibelatti.pinboard.core.android.composable.PullRefreshLayout
import com.fibelatti.pinboard.features.appstate.NoteListContent
import com.fibelatti.pinboard.features.appstate.RefreshNotes
import com.fibelatti.pinboard.features.appstate.ViewNote
import com.fibelatti.pinboard.features.appstate.find
import com.fibelatti.pinboard.features.notes.domain.model.Note
import com.fibelatti.pinboard.features.notes.domain.model.NoteSorting
import com.fibelatti.ui.components.AutoSizeText
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun NoteListScreen(
    modifier: Modifier = Modifier,
    noteListViewModel: NoteListViewModel = hiltViewModel(),
) {
    Surface(
        modifier = modifier,
        color = ExtendedTheme.colors.backgroundNoOverlay,
    ) {
        val appState by noteListViewModel.appState.collectAsStateWithLifecycle()
        val noteListContent by rememberUpdatedState(
            newValue = appState.content.find<NoteListContent>() ?: return@Surface,
        )

        val error by noteListViewModel.error.collectAsStateWithLifecycle()
        LaunchedErrorHandlerEffect(error = error, handler = noteListViewModel::errorHandled)

        if (noteListContent.shouldLoad) {
            LoadingContent()
        } else {
            NoteListContent(
                notes = noteListContent.notes,
                onSortOptionClicked = { noteListSorting ->
                    val sorting = when (noteListSorting) {
                        NoteList.Sorting.ByDateUpdatedDesc -> NoteSorting.ByDateUpdatedDesc
                        NoteList.Sorting.ByDateUpdatedAsc -> NoteSorting.ByDateUpdatedAsc
                        NoteList.Sorting.AtoZ -> NoteSorting.AtoZ
                    }

                    noteListViewModel.sort(noteListContent.notes, sorting)
                },
                onPullToRefresh = { noteListViewModel.runAction(RefreshNotes) },
                onNoteClicked = { note -> noteListViewModel.runAction(ViewNote(note.id)) },
                sidePanelVisible = appState.sidePanelVisible,
            )
        }
    }
}

@Composable
private fun NoteListContent(
    notes: List<Note>,
    onSortOptionClicked: (NoteList.Sorting) -> Unit = {},
    onPullToRefresh: () -> Unit = {},
    onNoteClicked: (Note) -> Unit = {},
    sidePanelVisible: Boolean = false,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        val listState = rememberLazyListState()
        var selectedSortingIndex by rememberSaveable { mutableIntStateOf(0) }

        LaunchedEffect(selectedSortingIndex) {
            listState.scrollToItem(index = 0)
        }

        if (notes.isEmpty()) {
            EmptyListContent(
                icon = painterResource(id = R.drawable.ic_notes),
                title = stringResource(id = R.string.notes_empty_title),
                description = stringResource(id = R.string.notes_empty_description),
            )
        } else {
            val windowInsets = WindowInsets.safeDrawing
                .only(if (sidePanelVisible) WindowInsetsSides.Start else WindowInsetsSides.Horizontal)
                .add(WindowInsets(left = 16.dp, right = 16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(windowInsets),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            ) {
                NoteList.Sorting.entries.forEachIndexed { index, sorting ->
                    val weight by animateFloatAsState(
                        targetValue = if (selectedSortingIndex == index) 1.2f else 1f,
                    )

                    ToggleButton(
                        checked = index == selectedSortingIndex,
                        onCheckedChange = {
                            selectedSortingIndex = index
                            onSortOptionClicked(sorting)
                        },
                        modifier = Modifier
                            .weight(weight)
                            .semantics { role = Role.RadioButton },
                        shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            NoteList.Sorting.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
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

            val listWindowInsets = WindowInsets.safeDrawing
                .only(if (sidePanelVisible) WindowInsetsSides.Start else WindowInsetsSides.Horizontal)
                .add(WindowInsets(top = 16.dp, bottom = 100.dp))

            PullRefreshLayout(
                onPullToRefresh = onPullToRefresh,
                listState = listState,
                contentPadding = listWindowInsets.asPaddingValues(),
            ) {
                items(notes) { note ->
                    NoteListItem(
                        note = note,
                        onNoteClicked = onNoteClicked,
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteListItem(
    note: Note,
    onNoteClicked: (Note) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .padding(horizontal = 8.dp)
            .clickable { onNoteClicked(note) },
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.CenterVertically),
        ) {
            Text(
                text = note.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )

            if (note.displayCreatedAt.isNotEmpty()) {
                Text(
                    text = stringResource(id = R.string.notes_saved_at, note.displayCreatedAt),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (note.displayUpdatedAt.isNotEmpty() && note.displayUpdatedAt != note.displayCreatedAt) {
                Text(
                    text = stringResource(id = R.string.notes_updated_at, note.displayUpdatedAt),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

object NoteList {

    enum class Sorting(val label: Int) {
        ByDateUpdatedDesc(label = R.string.note_sorting_date_updated_desc),
        ByDateUpdatedAsc(label = R.string.note_sorting_date_updated_asc),
        AtoZ(label = R.string.note_sorting_a_to_z),
    }
}

@Composable
@ThemePreviews
private fun EmptyNoteListScreenPreview() {
    ExtendedTheme {
        NoteListContent(
            notes = emptyList(),
        )
    }
}

@Composable
@ThemePreviews
private fun NoteListContentPreview() {
    ExtendedTheme {
        val notes = List(10) {
            Note(
                id = "$it",
                title = "Note $it",
                createdAt = "$it",
                displayCreatedAt = "$it",
                updatedAt = "${if (it % 2 == 0) it else it + 1}",
                displayUpdatedAt = "${if (it % 2 == 0) it else it + 1}",
                text = "Note text $it",
            )
        }

        NoteListContent(
            notes = notes,
        )
    }
}
