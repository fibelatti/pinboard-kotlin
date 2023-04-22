package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.features.tags.domain.model.Tag

data class SearchParameters(
    val term: String = "",
    val tags: List<Tag> = listOf(),
) {
    fun isActive(): Boolean = term.isNotEmpty() or tags.isNotEmpty()
}
