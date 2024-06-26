package com.fibelatti.bookmarking.features.appstate

import com.fibelatti.bookmarking.features.tags.domain.model.Tag

public data class SearchParameters(
    val term: String = "",
    val tags: List<Tag> = listOf(),
) {

    public fun isActive(): Boolean = term.isNotEmpty() or tags.isNotEmpty()
}
