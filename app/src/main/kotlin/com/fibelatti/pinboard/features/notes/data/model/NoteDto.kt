package com.fibelatti.pinboard.features.notes.data.model

import com.fibelatti.core.functional.Mapper
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.notes.domain.model.Note
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import javax.inject.Inject

@JsonClass(generateAdapter = true)
data class NoteDto(
    val id: String,
    val title: String?,
    @Json(name = "created_at") val createdAt: String?,
    @Json(name = "updated_at") val updatedAt: String?,
    val text: String?,
)

class NoteDtoMapper @Inject constructor(
    private val dateFormatter: DateFormatter,
) : Mapper<NoteDto, Note> {

    override fun map(param: NoteDto): Note {
        return Note(
            id = param.id,
            title = param.title.orEmpty(),
            createdAt = param.createdAt?.let(dateFormatter::notesFormatToDisplayFormat).orEmpty(),
            updatedAt = param.updatedAt?.let(dateFormatter::notesFormatToDisplayFormat).orEmpty(),
            text = param.text.orEmpty()
        )
    }
}
