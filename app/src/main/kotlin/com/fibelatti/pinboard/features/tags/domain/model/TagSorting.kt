package com.fibelatti.pinboard.features.tags.domain.model

sealed class TagSorting {

    data object AtoZ : TagSorting()
    data object MoreFirst : TagSorting()
    data object LessFirst : TagSorting()
}
