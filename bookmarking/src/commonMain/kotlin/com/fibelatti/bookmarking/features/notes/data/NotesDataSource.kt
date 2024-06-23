package com.fibelatti.bookmarking.features.notes.data

import com.fibelatti.bookmarking.core.network.resultFromNetwork
import com.fibelatti.bookmarking.features.notes.data.model.NoteDtoMapper
import com.fibelatti.bookmarking.features.notes.domain.NotesRepository
import com.fibelatti.bookmarking.features.notes.domain.model.Note
import com.fibelatti.core.functional.Result
import org.koin.core.annotation.Single

@Single
internal class NotesDataSource(
    private val notesApi: NotesApi,
    private val noteDtoMapper: NoteDtoMapper,
) : NotesRepository {

    override suspend fun getAllNotes(): Result<List<Note>> = resultFromNetwork {
        notesApi.getAllNotes().notes.let(noteDtoMapper::mapList)
    }

    override suspend fun getNote(id: String): Result<Note> = resultFromNetwork {
        notesApi.getNote(id).let(noteDtoMapper::map)
    }
}
