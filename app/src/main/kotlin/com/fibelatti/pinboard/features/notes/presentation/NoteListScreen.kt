package com.fibelatti.pinboard.features.notes.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.EmptyListContent
import com.fibelatti.pinboard.core.android.composable.LoadingContent
import com.fibelatti.pinboard.core.android.composable.PullRefreshLayout
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.NoteDetailContent
import com.fibelatti.pinboard.features.appstate.NoteListContent
import com.fibelatti.pinboard.features.appstate.RefreshNotes
import com.fibelatti.pinboard.features.appstate.ViewNote
import com.fibelatti.pinboard.features.notes.domain.model.Note
import com.fibelatti.pinboard.features.notes.domain.model.NoteSorting
import com.fibelatti.ui.components.RowToggleButtonGroup
import com.fibelatti.ui.components.ToggleButtonGroup
import com.fibelatti.ui.foundation.StableList
import com.fibelatti.ui.foundation.asHorizontalPaddingDp
import com.fibelatti.ui.foundation.navigationBarsCompat
import com.fibelatti.ui.foundation.toStableList
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import kotlinx.coroutines.flow.onEach
import java.util.UUID

@Composable
fun NoteListScreen(
    appStateViewModel: AppStateViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
    noteListViewModel: NoteListViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    onError: (Throwable?, () -> Unit) -> Unit,
) {
    Surface(
        color = ExtendedTheme.colors.backgroundNoOverlay,
    ) {
        val content by appStateViewModel.content.collectAsStateWithLifecycle()
        val noteListContent by rememberUpdatedState(
            newValue = when (val value = content) {
                is NoteListContent -> value
                is NoteDetailContent -> value.previousContent
                else -> return@Surface
            },
        )

        val actionId = remember { UUID.randomUUID().toString() }
        val localContext = LocalContext.current
        val localLifecycleOwner = LocalLifecycleOwner.current

        LaunchedEffect(content) {
            if (content !is NoteListContent) return@LaunchedEffect
            mainViewModel.updateState { mainViewModelState ->
                mainViewModelState.copy(
                    title = MainState.TitleComponent.Visible(localContext.getString(R.string.notes_title)),
                    subtitle = when {
                        noteListContent.shouldLoad -> MainState.TitleComponent.Gone
                        noteListContent.notes.isEmpty() -> MainState.TitleComponent.Gone
                        else -> MainState.TitleComponent.Visible(
                            localContext.resources.getQuantityString(
                                R.plurals.notes_quantity,
                                noteListContent.notes.size,
                                noteListContent.notes.size,
                            ),
                        )
                    },
                    navigation = MainState.NavigationComponent.Visible(actionId),
                    bottomAppBar = MainState.BottomAppBarComponent.Gone,
                    floatingActionButton = MainState.FabComponent.Gone,
                )
            }
        }

        LaunchedEffect(noteListContent.shouldLoad) {
            if (noteListContent.shouldLoad) {
                noteListViewModel.getAllNotes()
            }
        }

        LaunchedEffect(Unit) {
            mainViewModel.navigationClicks(actionId)
                .onEach { onBackPressed() }
                .launchInAndFlowWith(localLifecycleOwner)
        }

        LaunchedEffect(Unit) {
            noteListViewModel.error
                .onEach { throwable -> onError(throwable, noteListViewModel::errorHandled) }
                .launchInAndFlowWith(localLifecycleOwner)
        }

        if (noteListContent.shouldLoad) {
            LoadingContent()
        } else {
            NoteListContent(
                notes = noteListContent.notes.toStableList(),
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
}

@Composable
private fun NoteListContent(
    notes: StableList<Note>,
    onSortOptionClicked: (NoteList.Sorting) -> Unit = {},
    onPullToRefresh: () -> Unit = {},
    onNoteClicked: (Note) -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxSize()) {
        val listState = rememberLazyListState()
        var selectedSortingIndex by rememberSaveable { mutableIntStateOf(0) }

        LaunchedEffect(selectedSortingIndex) {
            listState.scrollToItem(index = 0)
        }

        if (notes.value.isEmpty()) {
            EmptyListContent(
                icon = painterResource(id = R.drawable.ic_notes),
                title = stringResource(id = R.string.notes_empty_title),
                description = stringResource(id = R.string.notes_empty_description),
            )
        } else {
            val (leftPadding, rightPadding) = WindowInsets.navigationBarsCompat
                .asHorizontalPaddingDp(addStart = 16.dp, addEnd = 16.dp)

            RowToggleButtonGroup(
                items = NoteList.Sorting.entries
                    .map { sorting ->
                        ToggleButtonGroup.Item(
                            id = sorting.id,
                            text = stringResource(id = sorting.label),
                        )
                    }
                    .toStableList(),
                onButtonClick = {
                    val (index, sorting) = requireNotNull(NoteList.Sorting.findByIdWithIndex(it.id))
                    selectedSortingIndex = index
                    onSortOptionClicked(sorting)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = leftPadding, end = rightPadding),
                selectedIndex = selectedSortingIndex,
                buttonHeight = 40.dp,
                textStyle = MaterialTheme.typography.bodySmall,
            )

            val (listLeftPadding, listRightPadding) = WindowInsets.navigationBarsCompat.asHorizontalPaddingDp()

            PullRefreshLayout(
                onPullToRefresh = onPullToRefresh,
                listState = listState,
                contentPadding = PaddingValues(
                    start = listLeftPadding,
                    top = 16.dp,
                    end = listRightPadding,
                    bottom = 100.dp,
                ),
            ) {
                items(notes.value) { note ->
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
            .padding(horizontal = 8.dp)
            .clickable { onNoteClicked(note) },
        shape = RoundedCornerShape(6.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(all = 8.dp),
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

            fun findByIdWithIndex(id: String): IndexedValue<Sorting>? = entries
                .withIndex()
                .find { it.value.id == id }
        }
    }
}

@Composable
@ThemePreviews
private fun EmptyNoteListScreenPreview() {
    ExtendedTheme {
        NoteListContent(
            notes = StableList(),
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
                updatedAt = "${if (it % 2 == 0) it else it + 1}",
                text = "Note text $it",
            )
        }.toStableList()

        NoteListContent(
            notes = notes,
        )
    }
}
