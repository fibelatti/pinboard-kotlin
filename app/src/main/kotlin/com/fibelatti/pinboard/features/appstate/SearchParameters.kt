package com.fibelatti.pinboard.features.appstate

import androidx.compose.runtime.Stable
import com.fibelatti.pinboard.features.tags.domain.model.Tag

@Stable
data class SearchParameters(
    val term: String = "",
    val tags: List<Tag> = listOf(),
) {
    fun isActive(): Boolean = term.isNotEmpty() or tags.isNotEmpty()
}
