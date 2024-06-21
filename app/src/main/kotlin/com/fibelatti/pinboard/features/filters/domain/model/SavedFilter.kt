package com.fibelatti.pinboard.features.filters.domain.model

import com.fibelatti.bookmarking.features.tags.domain.model.Tag

data class SavedFilter(
    val searchTerm: String,
    val tags: List<Tag>,
)
