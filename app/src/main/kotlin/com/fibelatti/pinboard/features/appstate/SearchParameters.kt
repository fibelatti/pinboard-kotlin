package com.fibelatti.pinboard.features.appstate

import androidx.compose.runtime.Stable
import com.fibelatti.pinboard.core.AppConfig.PINBOARD_USER_URL
import com.fibelatti.pinboard.features.tags.domain.model.Tag

@Stable
data class SearchParameters(
    val term: String = "",
    val tags: List<Tag> = listOf(),
    val matchAll: Boolean = true,
    val exactMatch: Boolean = false,
) {

    fun isActive(): Boolean = term.isNotEmpty() || tags.isNotEmpty()
}

fun SearchParameters.pinboardQueryUrl(username: String): String {
    return "$PINBOARD_USER_URL$username?query=$term"
}

fun SearchParameters.pinboardTagsUrl(username: String): String {
    return "$PINBOARD_USER_URL$username/${tags.joinToString { "t:${it.name}/" }}"
}
