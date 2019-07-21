package com.fibelatti.pinboard.features.notes.domain.model

data class Note(
    val id: String,
    val title: String,
    val createdAt: String,
    val updatedAt: String,
    val text: String
)
