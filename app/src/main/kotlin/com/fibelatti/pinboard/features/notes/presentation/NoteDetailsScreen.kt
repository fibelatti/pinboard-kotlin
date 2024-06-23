package com.fibelatti.pinboard.features.notes.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.bookmarking.features.notes.domain.model.Note
import com.fibelatti.core.randomUUID
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.CrossfadeLoadingLayout
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.koinViewModel

@Composable
fun NoteDetailsScreen(
    appStateViewModel: AppStateViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinViewModel(),
    noteDetailsViewModel: NoteDetailsViewModel = koinViewModel(),
    onBackPressed: () -> Unit,
    onError: (Throwable?, () -> Unit) -> Unit,
) {
    Surface(
        color = ExtendedTheme.colors.backgroundNoOverlay,
    ) {
        val appState by appStateViewModel.noteDetailContent.collectAsStateWithLifecycle(initialValue = null)
        val noteDetailContent by rememberUpdatedState(newValue = appState ?: return@Surface)
        val isLoading = noteDetailContent.note.isLeft

        val actionId = remember { randomUUID() }
        val localLifecycleOwner = LocalLifecycleOwner.current

        LaunchedEffect(noteDetailContent) {
            mainViewModel.updateState { currentState ->
                if (currentState.multiPanelEnabled) {
                    currentState.copy(
                        sidePanelAppBar = MainState.SidePanelAppBarComponent.Visible(
                            id = actionId,
                            menuItems = listOf(MainState.MenuItemComponent.CloseSidePanel),
                        ),
                    )
                } else {
                    currentState.copy(
                        title = MainState.TitleComponent.Gone,
                        subtitle = MainState.TitleComponent.Gone,
                        navigation = MainState.NavigationComponent.Visible(actionId),
                        bottomAppBar = MainState.BottomAppBarComponent.Gone,
                        floatingActionButton = MainState.FabComponent.Gone,
                    )
                }
            }
        }

        LaunchedEffect(isLoading, noteDetailContent) {
            if (isLoading) {
                noteDetailsViewModel.getNoteDetails(noteDetailContent.id)
            }
        }

        LaunchedEffect(Unit) {
            mainViewModel.navigationClicks(actionId)
                .onEach { onBackPressed() }
                .launchInAndFlowWith(localLifecycleOwner)
        }

        LaunchedEffect(Unit) {
            mainViewModel.menuItemClicks(actionId)
                .onEach { (menuItem, _) ->
                    if (menuItem is MainState.MenuItemComponent.CloseSidePanel) {
                        onBackPressed()
                    }
                }
                .launchInAndFlowWith(localLifecycleOwner)
        }

        LaunchedEffect(Unit) {
            noteDetailsViewModel.error
                .onEach { throwable -> onError(throwable, noteDetailsViewModel::errorHandled) }
                .launchInAndFlowWith(localLifecycleOwner)
        }

        CrossfadeLoadingLayout(
            data = noteDetailContent.note.rightOrNull(),
            modifier = Modifier.fillMaxSize(),
        ) {
            NoteContent(note = it)
        }
    }
}

@Composable
private fun NoteContent(
    note: Note,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 100.dp),
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

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = note.text,
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
@ThemePreviews
private fun NoteContentPreview(
    @PreviewParameter(provider = LoremIpsum::class) text: String,
) {
    ExtendedTheme {
        NoteContent(
            note = Note(
                id = "note-id",
                title = "Note title",
                createdAt = "21/04/2023",
                updatedAt = "22/04/2023",
                text = text,
            ),
        )
    }
}
