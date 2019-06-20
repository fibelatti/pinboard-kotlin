package com.fibelatti.pinboard.features.appstate

data class SearchParameters(
    val term: String = "",
    val tags: List<String> = listOf()
) {
    fun isActive(): Boolean = term.isNotEmpty() or tags.isNotEmpty()
}
