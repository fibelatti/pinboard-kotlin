package com.fibelatti.pinboard.features.notes.data

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.pinboard.core.functional.resultFrom
import com.fibelatti.pinboard.core.network.RateLimitRunner
import com.fibelatti.pinboard.features.notes.data.model.NoteDtoMapper
import com.fibelatti.pinboard.features.notes.domain.model.Note
import com.fibelatti.pinboard.features.notes.domain.NotesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NotesDataSource @Inject constructor(
    private val notesApi: NotesApi,
    private val noteDtoMapper: NoteDtoMapper,
    private val rateLimitRunner: RateLimitRunner
) : NotesRepository {

    override suspend fun getAllNotes(): Result<List<Note>> = withContext(Dispatchers.IO) {
        resultFrom { rateLimitRunner.run(notesApi::getAllNotes) }
            .mapCatching { noteDtoMapper.mapList(it.notes) }
    }

    override suspend fun getNote(id: String): Result<Note> = withContext(Dispatchers.IO) {
        resultFrom { rateLimitRunner.run { notesApi.getNote(id) } }
            .mapCatching(noteDtoMapper::map)
    }
}
