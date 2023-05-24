package com.fibelatti.pinboard.features.notes.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.CrossfadeLoadingLayout
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.notes.domain.model.Note
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun NoteDetailsScreen(
    appStateViewModel: AppStateViewModel = hiltViewModel(),
    noteDetailsViewModel: NoteDetailsViewModel = hiltViewModel(),
) {
    Surface(
        color = ExtendedTheme.colors.backgroundNoOverlay,
    ) {
        val appState by appStateViewModel.noteDetailContent.collectAsStateWithLifecycle(initialValue = null)
        val noteDetailContent = appState ?: return@Surface
        val isLoading = noteDetailContent.note.isLeft

        LaunchedEffect(isLoading, noteDetailContent) {
            if (isLoading) {
                noteDetailsViewModel.getNoteDetails(noteDetailContent.id)
            }
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

        Divider(
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
