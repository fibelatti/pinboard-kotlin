package com.fibelatti.pinboard.features.notes.presentation

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.CrossfadeLoadingLayout
import com.fibelatti.pinboard.core.android.composable.LaunchedErrorHandlerEffect
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.NoteDetailContent
import com.fibelatti.pinboard.features.notes.domain.model.Note
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun NoteDetailsScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel(),
    noteDetailsViewModel: NoteDetailsViewModel = hiltViewModel(),
) {
    Surface(
        modifier = modifier,
        color = ExtendedTheme.colors.backgroundNoOverlay,
    ) {
        val appState by mainViewModel.appState.collectAsStateWithLifecycle()
        val noteDetailContent by noteDetailsViewModel.noteDetailContent.collectAsStateWithLifecycle(null)
        val current by rememberUpdatedState(newValue = noteDetailContent ?: return@Surface)

        val localLifecycle = LocalLifecycleOwner.current.lifecycle
        val localOnBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

        LaunchedEffect(noteDetailContent, appState.multiPanelAvailable) {
            mainViewModel.updateState { currentState ->
                if (appState.multiPanelAvailable) {
                    currentState.copy(
                        sidePanelAppBar = MainState.SidePanelAppBarComponent.Visible(
                            contentType = NoteDetailContent::class,
                            menuItems = listOf(MainState.MenuItemComponent.CloseSidePanel),
                        ),
                    )
                } else {
                    currentState.copy(
                        title = MainState.TitleComponent.Gone,
                        subtitle = MainState.TitleComponent.Gone,
                        navigation = MainState.NavigationComponent.Visible(),
                        bottomAppBar = MainState.BottomAppBarComponent.Gone,
                        floatingActionButton = MainState.FabComponent.Gone,
                    )
                }
            }
        }

        LaunchedEffect(Unit) {
            mainViewModel.menuItemClicks(contentType = NoteDetailContent::class)
                .onEach { (menuItem, _) ->
                    if (menuItem is MainState.MenuItemComponent.CloseSidePanel) {
                        localOnBackPressedDispatcher?.onBackPressed()
                    }
                }
                .flowWithLifecycle(localLifecycle)
                .launchIn(this)
        }

        val error by noteDetailsViewModel.error.collectAsStateWithLifecycle()
        LaunchedErrorHandlerEffect(error = error, handler = noteDetailsViewModel::errorHandled)

        CrossfadeLoadingLayout(
            data = current.note.rightOrNull(),
            modifier = Modifier.fillMaxSize(),
        ) { note ->
            NoteContent(note = note)
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
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = note.title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
        )

        Text(
            text = stringResource(id = R.string.notes_saved_at, note.createdAt),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )

        if (note.updatedAt != note.createdAt) {
            Text(
                text = stringResource(id = R.string.notes_updated_at, note.updatedAt),
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
