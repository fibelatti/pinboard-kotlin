package com.fibelatti.bookmarking.features.tags.domain.model

public sealed class TagSorting {

    public data object AtoZ : TagSorting()
    public data object MoreFirst : TagSorting()
    public data object LessFirst : TagSorting()
}
