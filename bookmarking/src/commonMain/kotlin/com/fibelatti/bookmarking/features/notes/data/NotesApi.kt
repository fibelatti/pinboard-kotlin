package com.fibelatti.bookmarking.features.notes.data

import com.fibelatti.bookmarking.features.notes.data.model.NoteDto
import com.fibelatti.bookmarking.features.notes.data.model.NoteListDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory
internal class NotesApi(
    @Named("pinboard") private val httpClient: HttpClient,
) {

    suspend fun getAllNotes(): NoteListDto = httpClient.get(urlString = "notes/list").body()

    suspend fun getNote(id: String): NoteDto = httpClient.get(urlString = "notes/$id").body()
}
