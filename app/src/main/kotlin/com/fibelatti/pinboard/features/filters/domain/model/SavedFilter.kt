package com.fibelatti.pinboard.features.filters.domain.model

import androidx.compose.runtime.Stable
import com.fibelatti.pinboard.features.tags.domain.model.Tag

@Stable
data class SavedFilter(
    val searchTerm: String,
    val tags: List<Tag>,
)
