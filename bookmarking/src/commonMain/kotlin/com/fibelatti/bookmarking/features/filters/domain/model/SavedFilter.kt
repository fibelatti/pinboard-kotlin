package com.fibelatti.bookmarking.features.filters.domain.model

import com.fibelatti.bookmarking.features.tags.domain.model.Tag

public data class SavedFilter(
    val searchTerm: String,
    val tags: List<Tag>,
)
