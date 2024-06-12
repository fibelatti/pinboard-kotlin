package com.fibelatti.pinboard.features.notes.data.model

import com.fibelatti.pinboard.core.network.SkipBadElementsListSerializer
import kotlinx.serialization.Serializable

@Serializable
data class NoteListDto(
    val count: Int,
    @Serializable(with = SkipBadElementsListSerializer::class)
    val notes: List<NoteDto>,
)
