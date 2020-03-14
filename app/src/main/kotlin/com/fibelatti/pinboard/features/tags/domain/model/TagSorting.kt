package com.fibelatti.pinboard.features.tags.domain.model

sealed class TagSorting {

    object AtoZ : TagSorting()
    object MoreFirst : TagSorting()
    object LessFirst : TagSorting()
}
