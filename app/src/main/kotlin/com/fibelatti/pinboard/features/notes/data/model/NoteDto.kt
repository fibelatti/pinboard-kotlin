package com.fibelatti.pinboard.features.notes.data.model

import com.fibelatti.core.functional.Mapper
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.notes.domain.model.Note
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Factory
import javax.inject.Inject

@Serializable
data class NoteDto(
    val id: String,
    val title: String?,
    @SerialName(value = "created_at") val createdAt: String?,
    @SerialName(value = "updated_at") val updatedAt: String?,
    val text: String?,
)

@Factory
class NoteDtoMapper @Inject constructor(
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
