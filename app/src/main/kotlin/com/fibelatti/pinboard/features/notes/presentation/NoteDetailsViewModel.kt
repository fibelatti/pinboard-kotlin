package com.fibelatti.pinboard.features.notes.presentation

import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.onFailure
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.SetNote
import com.fibelatti.pinboard.features.notes.domain.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailsViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    private val appStateRepository: AppStateRepository,
) : BaseViewModel() {

    fun getNoteDetails(id: String) {
        launch {
            notesRepository.getNote(id)
                .mapCatching { appStateRepository.runAction(SetNote(it)) }
                .onFailure(::handleError)
        }
    }
}
