package com.fibelatti.pinboard.features.notes.presentation

import com.fibelatti.core.functional.coMapCatching
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.NoteListContent
import com.fibelatti.pinboard.features.appstate.SetNotes
import com.fibelatti.pinboard.features.notes.domain.NotesRepository
import com.fibelatti.pinboard.features.notes.domain.model.Note
import com.fibelatti.pinboard.features.notes.domain.model.NoteSorting
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@HiltViewModel
class NoteListViewModel @Inject constructor(
    scope: CoroutineScope,
    appStateRepository: AppStateRepository,
    private val notesRepository: NotesRepository,
) : BaseViewModel(scope, appStateRepository) {

    init {
        scope.launch {
            filteredContent<NoteListContent>()
                .filter { it.shouldLoad }
                .collectLatest { getAllNotes() }
        }
    }

    private suspend fun getAllNotes() {
        notesRepository.getAllNotes()
            .coMapCatching { runAction(SetNotes(it)) }
            .onFailure(::handleError)
    }

    fun sort(notes: List<Note>, sorting: NoteSorting) {
        scope.launch {
            val updatedNotes = when (sorting) {
                NoteSorting.ByDateUpdatedDesc -> notes.sortedByDescending { it.updatedAt }
                NoteSorting.ByDateUpdatedAsc -> notes.sortedBy { it.updatedAt }
                NoteSorting.AtoZ -> notes.sortedBy(Note::title)
            }
            runAction(SetNotes(updatedNotes))
        }
    }
}
