package com.fibelatti.pinboard.features.notes.data.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class NoteListDto(
    val count: Int,
    @Contextual
    val notes: List<NoteDto>,
)
