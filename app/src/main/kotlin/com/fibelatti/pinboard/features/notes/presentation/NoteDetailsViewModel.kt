package com.fibelatti.pinboard.features.notes.presentation

import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.onFailure
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.NoteDetailContent
import com.fibelatti.pinboard.features.appstate.SetNote
import com.fibelatti.pinboard.features.notes.domain.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

@HiltViewModel
class NoteDetailsViewModel @Inject constructor(
    scope: CoroutineScope,
    appStateRepository: AppStateRepository,
    private val notesRepository: NotesRepository,
) : BaseViewModel(scope, appStateRepository) {

    val noteDetailContent: Flow<NoteDetailContent> = filteredContent<NoteDetailContent>()

    init {
        scope.launch {
            noteDetailContent
                .mapNotNull { content ->
                    content.id.takeIf { content.note.isLeft }
                }
                .collectLatest(::getNoteDetails)
        }
    }

    private suspend fun getNoteDetails(id: String) {
        notesRepository.getNote(id)
            .mapCatching { runAction(SetNote(it)) }
            .onFailure(::handleError)
    }
}
