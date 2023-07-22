package com.fibelatti.pinboard.features.notes.domain.model

sealed class NoteSorting {

    data object ByDateUpdatedDesc : NoteSorting() // API default
    data object ByDateUpdatedAsc : NoteSorting()
    data object AtoZ : NoteSorting()
}
