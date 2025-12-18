package com.fibelatti.pinboard.features.filters.domain.model

import androidx.compose.runtime.Stable
import com.fibelatti.pinboard.features.tags.domain.model.Tag

@Stable
data class SavedFilter(
    val term: String,
    val tags: List<Tag>,
    val matchAll: Boolean = true,
    val exactMatch: Boolean = false,
)
