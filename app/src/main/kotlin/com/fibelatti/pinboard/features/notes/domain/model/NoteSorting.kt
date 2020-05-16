package com.fibelatti.pinboard.features.notes.domain.model

sealed class NoteSorting {

    object ByDateUpdatedDesc : NoteSorting() // API default
    object ByDateUpdatedAsc : NoteSorting()
    object AtoZ : NoteSorting()
}
