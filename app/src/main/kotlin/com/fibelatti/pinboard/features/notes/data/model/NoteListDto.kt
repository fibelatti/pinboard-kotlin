package com.fibelatti.pinboard.features.notes.data.model

import androidx.annotation.Keep

@Keep
data class NoteListDto(
    val count: Int,
    val notes: List<NoteDto>
)
