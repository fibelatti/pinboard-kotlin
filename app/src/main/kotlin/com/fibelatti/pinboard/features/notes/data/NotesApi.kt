package com.fibelatti.pinboard.features.notes.data

import com.fibelatti.pinboard.features.notes.data.model.NoteDto
import com.fibelatti.pinboard.features.notes.data.model.NoteListDto
import retrofit2.http.GET
import retrofit2.http.Path

interface NotesApi {

    @GET("notes/list")
    suspend fun getAllNotes(): NoteListDto

    @GET("notes/{id}")
    suspend fun getNote(@Path("id") id: String): NoteDto
}
