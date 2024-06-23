package com.fibelatti.bookmarking.features.notes.data.model

import com.fibelatti.bookmarking.core.util.DateFormatter
import com.fibelatti.bookmarking.features.notes.domain.model.Note
import com.fibelatti.core.functional.Mapper
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Factory

@Serializable
public data class NoteDto(
    val id: String,
    val title: String?,
    @SerialName(value = "created_at") val createdAt: String?,
    @SerialName(value = "updated_at") val updatedAt: String?,
    val text: String?,
)

@Factory
internal class NoteDtoMapper(
    private val dateFormatter: DateFormatter,
) : Mapper<NoteDto, Note> {

    override fun map(param: NoteDto): Note {
        return Note(
            id = param.id,
            title = param.title.orEmpty(),
            createdAt = param.createdAt?.let(dateFormatter::notesFormatToDisplayFormat).orEmpty(),
            updatedAt = param.updatedAt?.let(dateFormatter::notesFormatToDisplayFormat).orEmpty(),
            text = param.text.orEmpty(),
        )
    }
}
