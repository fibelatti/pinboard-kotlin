package com.fibelatti.pinboard.features.notes.presentation

import com.fibelatti.core.archcomponents.BaseViewModel
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.onFailure
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.SetNotes
import com.fibelatti.pinboard.features.notes.domain.NotesRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class NoteListViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    private val appStateRepository: AppStateRepository
) : BaseViewModel() {

    fun getAllNotes() {
        launch {
            notesRepository.getAllNotes()
                .mapCatching { appStateRepository.runAction(SetNotes(it)) }
                .onFailure(::handleError)
        }
    }
}
