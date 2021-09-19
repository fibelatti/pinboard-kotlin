package com.fibelatti.pinboard.features.notes.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NoteListDto(
    val count: Int,
    val notes: List<NoteDto>
)
