package com.fibelatti.pinboard.features.notes.data

import com.fibelatti.pinboard.core.di.RestApi
import com.fibelatti.pinboard.core.di.RestApiProvider
import com.fibelatti.pinboard.features.notes.data.model.NoteDto
import com.fibelatti.pinboard.features.notes.data.model.NoteListDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject

internal class NotesApi @Inject constructor(
    @RestApi(RestApiProvider.PINBOARD) private val httpClient: HttpClient,
) {

    suspend fun getAllNotes(): NoteListDto = httpClient.get(urlString = "v1/notes/list").body()

    suspend fun getNote(id: String): NoteDto = httpClient.get(urlString = "v1/notes/$id").body()
}
