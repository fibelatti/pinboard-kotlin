package com.fibelatti.bookmarking.features.notes.domain

import com.fibelatti.bookmarking.features.notes.domain.model.Note
import com.fibelatti.core.functional.Result

public interface NotesRepository {

    public suspend fun getAllNotes(): Result<List<Note>>

    public suspend fun getNote(id: String): Result<Note>
}
