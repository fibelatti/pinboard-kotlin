package com.fibelatti.pinboard.features.notes.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.AnimatedVisibilityProgressIndicator
import com.fibelatti.pinboard.core.android.composable.EmptyListContent
import com.fibelatti.pinboard.core.android.composable.rememberAutoDismissPullRefreshState
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.RefreshNotes
import com.fibelatti.pinboard.features.appstate.ViewNote
import com.fibelatti.pinboard.features.notes.domain.model.Note
import com.fibelatti.pinboard.features.notes.domain.model.NoteSorting
import com.fibelatti.ui.components.RowToggleButtonGroup
import com.fibelatti.ui.components.ToggleButtonGroup
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import kotlinx.coroutines.launch

@Composable
fun NoteListScreen(
    appStateViewModel: AppStateViewModel = hiltViewModel(),
    noteListViewModel: NoteListViewModel = hiltViewModel(),
) {
    Surface(
        color = ExtendedTheme.colors.backgroundNoOverlay,
    ) {
        val appState by appStateViewModel.noteListContent.collectAsStateWithLifecycle(initialValue = null)
        val noteListContent = appState ?: return@Surface

        LaunchedEffect(key1 = noteListContent.shouldLoad) {
            if (noteListContent.shouldLoad) {
                noteListViewModel.getAllNotes()
            }
        }

        NoteListScreen(
            notes = noteListContent.notes,
            isLoading = noteListContent.shouldLoad,
            onSortOptionClicked = { noteListSorting ->
                val sorting = when (noteListSorting) {
                    NoteList.Sorting.ByDateUpdatedDesc -> NoteSorting.ByDateUpdatedDesc
                    NoteList.Sorting.ByDateUpdatedAsc -> NoteSorting.ByDateUpdatedAsc
                    NoteList.Sorting.AtoZ -> NoteSorting.AtoZ
                }

                noteListViewModel.sort(noteListContent.notes, sorting)
            },
            onPullToRefresh = { appStateViewModel.runAction(RefreshNotes) },
            onNoteClicked = { note -> appStateViewModel.runAction(ViewNote(note.id)) },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun NoteListScreen(
    notes: List<Note>,
    isLoading: Boolean,
    onSortOptionClicked: (NoteList.Sorting) -> Unit = {},
    onPullToRefresh: () -> Unit = {},
    onNoteClicked: (Note) -> Unit = {},
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        AnimatedVisibilityProgressIndicator(
            isVisible = isLoading,
            modifier = Modifier.fillMaxSize(),
        )

        AnimatedVisibility(
            visible = !isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (notes.isEmpty()) {
                    EmptyListContent(
                        icon = painterResource(id = R.drawable.ic_notes),
                        title = stringResource(id = R.string.notes_empty_title),
                        description = stringResource(id = R.string.notes_empty_description),
                    )
                } else {
                    val scope = rememberCoroutineScope()
                    val listState = rememberLazyListState()
                    val (pullRefreshState, refreshing) = rememberAutoDismissPullRefreshState(onPullToRefresh)

                    RowToggleButtonGroup(
                        items = NoteList.Sorting.values().map { sorting ->
                            ToggleButtonGroup.Item(
                                id = sorting.id,
                                text = stringResource(id = sorting.label),
                            )
                        },
                        onButtonClick = {
                            val sorting = requireNotNull(NoteList.Sorting.findById(it.id))

                            onSortOptionClicked(sorting)

                            scope.launch {
                                listState.scrollToItem(index = 0)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        selectedIndex = 0,
                        buttonHeight = 40.dp,
                        textStyle = MaterialTheme.typography.bodySmall,
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pullRefresh(pullRefreshState),
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = WindowInsets(top = 16.dp, bottom = 100.dp)
                                .add(WindowInsets.navigationBars)
                                .asPaddingValues(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            state = listState,
                        ) {
                            items(count = notes.size, key = { notes[it].id }) { index ->
                                NoteListItem(
                                    note = notes[index],
                                    onNoteClicked = onNoteClicked,
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
        elevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNoteClicked(note) }
                .background(color = MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = note.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = stringResource(id = R.string.notes_saved_at, note.createdAt),
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )

            if (note.updatedAt != note.createdAt) {
                Text(
                    text = stringResource(id = R.string.notes_updated_at, note.updatedAt),
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

object NoteList {

    enum class Sorting(val id: String, val label: Int) {
        ByDateUpdatedDesc(id = "date-updated-desc", label = R.string.note_sorting_date_updated_desc),
        ByDateUpdatedAsc(id = "date-updated-asc", label = R.string.note_sorting_date_updated_asc),
        AtoZ(id = "a-to-z", label = R.string.note_sorting_a_to_z),
        ;

        companion object {

            fun findById(id: String): Sorting? = Sorting.values().find { it.id == id }
        }
    }
}

@Composable
@ThemePreviews
private fun EmptyNoteListScreenPreview() {
    ExtendedTheme {
        NoteListScreen(
            notes = emptyList(),
            isLoading = false,
        )
    }
}

@Composable
@ThemePreviews
private fun NoteListScreenPreview() {
    ExtendedTheme {
        val notes = List(10) {
            Note(
                id = "$it",
                title = "Note $it",
                createdAt = "$it",
                updatedAt = "${if (it % 2 == 0) it else it + 1}",
                text = "Note text $it",
            )
        }

        NoteListScreen(
            notes = notes,
            isLoading = false,
        )
    }
}
