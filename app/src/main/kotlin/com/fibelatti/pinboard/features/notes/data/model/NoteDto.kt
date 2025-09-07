package com.fibelatti.pinboard.features.notes.data.model

import com.fibelatti.core.functional.Mapper
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.notes.domain.model.Note
import javax.inject.Inject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NoteDto(
    val id: String,
    val title: String?,
    @SerialName(value = "created_at") val createdAt: String?,
    @SerialName(value = "updated_at") val updatedAt: String?,
    val text: String?,
)

class NoteDtoMapper @Inject constructor(
    private val dateFormatter: DateFormatter,
) : Mapper<NoteDto, Note> {

    override fun map(param: NoteDto): Note {
        return Note(
            id = param.id,
            title = param.title.orEmpty(),
            createdAt = param.createdAt.orEmpty(),
            displayCreatedAt = param.createdAt?.let(dateFormatter::dataFormatToDisplayFormat).orEmpty(),
            updatedAt = param.updatedAt.orEmpty(),
            displayUpdatedAt = param.updatedAt?.let(dateFormatter::dataFormatToDisplayFormat).orEmpty(),
            text = param.text.orEmpty(),
        )
    }
}
