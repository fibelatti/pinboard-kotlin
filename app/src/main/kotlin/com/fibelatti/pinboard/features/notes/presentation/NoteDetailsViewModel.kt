package com.fibelatti.pinboard.features.notes.presentation

import com.fibelatti.core.functional.coMapCatching
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.core.di.AppDispatchers
import com.fibelatti.pinboard.core.di.Scope
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.NoteDetailContent
import com.fibelatti.pinboard.features.appstate.SetNote
import com.fibelatti.pinboard.features.notes.domain.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

@HiltViewModel
class NoteDetailsViewModel @Inject constructor(
    @Scope(AppDispatchers.DEFAULT) dispatcher: CoroutineDispatcher,
    appStateRepository: AppStateRepository,
    private val notesRepository: NotesRepository,
) : BaseViewModel(dispatcher, appStateRepository) {

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
            .coMapCatching { runAction(SetNote(it)) }
            .onFailure(::handleError)
    }
}
