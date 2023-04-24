package com.fibelatti.pinboard.features.notes.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
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
import com.fibelatti.pinboard.core.android.composable.AnimatedVisibilityProgressIndicator
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.NoteDetailContent
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
        val appState by appStateViewModel.content.collectAsStateWithLifecycle()
        val noteDetailContent = appState as? NoteDetailContent ?: return@Surface
        val isLoading = noteDetailContent.note.isLeft
        val note = noteDetailContent.note.rightOrNull()

        LaunchedEffect(isLoading) {
            if (isLoading) {
                noteDetailsViewModel.getNoteDetails(noteDetailContent.id)
            }
        }

        NoteDetailsScreen(
            isLoading = isLoading,
            title = note?.title ?: "",
            savedAt = note?.createdAt ?: "",
            updatedAt = note?.updatedAt ?: "",
            text = note?.text ?: "",
        )
    }
}

@Composable
private fun NoteDetailsScreen(
    isLoading: Boolean,
    title: String,
    savedAt: String,
    updatedAt: String,
    text: String,
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
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(start = 16.dp, end = 16.dp, bottom = 100.dp),
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                )

                Text(
                    text = stringResource(id = R.string.notes_saved_at, savedAt),
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )

                if (updatedAt != savedAt) {
                    Text(
                        text = stringResource(id = R.string.notes_updated_at, updatedAt),
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
                    text = text,
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
@ThemePreviews
private fun NoteDetailScreenPreview(
    @PreviewParameter(provider = LoremIpsum::class) text: String,
) {
    ExtendedTheme {
        NoteDetailsScreen(
            isLoading = false,
            title = "Note title",
            savedAt = "21/04/2023",
            updatedAt = "22/04/2023",
            text = text,
        )
    }
}
