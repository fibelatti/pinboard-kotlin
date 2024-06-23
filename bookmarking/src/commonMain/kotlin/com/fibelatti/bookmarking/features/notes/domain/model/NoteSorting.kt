package com.fibelatti.bookmarking.features.notes.domain.model

public sealed class NoteSorting {

    public data object ByDateUpdatedDesc : NoteSorting() // API default
    public data object ByDateUpdatedAsc : NoteSorting()
    public data object AtoZ : NoteSorting()
}
