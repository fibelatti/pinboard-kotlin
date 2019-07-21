package com.fibelatti.pinboard.features.notes.domain

import com.fibelatti.core.functional.Result
import com.fibelatti.pinboard.features.notes.domain.model.Note

interface NotesRepository {

    suspend fun getAllNotes(): Result<List<Note>>

    suspend fun getNote(id: String): Result<Note>
}
