package com.fibelatti.pinboard.features.notes.data

import com.fibelatti.core.functional.Result
import com.fibelatti.pinboard.core.network.resultFromNetwork
import com.fibelatti.pinboard.features.notes.data.model.NoteDtoMapper
import com.fibelatti.pinboard.features.notes.domain.NotesRepository
import com.fibelatti.pinboard.features.notes.domain.model.Note
import javax.inject.Inject

class NotesDataSource @Inject constructor(
    private val notesApi: NotesApi,
    private val noteDtoMapper: NoteDtoMapper
) : NotesRepository {

    override suspend fun getAllNotes(): Result<List<Note>> = resultFromNetwork {
        notesApi.getAllNotes().notes.let(noteDtoMapper::mapList)
    }

    override suspend fun getNote(id: String): Result<Note> = resultFromNetwork {
        notesApi.getNote(id).let(noteDtoMapper::map)
    }
}
