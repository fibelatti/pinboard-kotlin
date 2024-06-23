package com.fibelatti.bookmarking.features.notes.data.model

import com.fibelatti.bookmarking.core.network.SkipBadElementsListSerializer
import kotlinx.serialization.Serializable

@Serializable
public data class NoteListDto(
    val count: Int,
    @Serializable(with = SkipBadElementsListSerializer::class)
    val notes: List<NoteDto>,
)
