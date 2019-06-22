package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.pinboard.features.appstate.NewestFirst
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.tags.domain.model.Tag

data class GetParams(
    val sorting: SortType = NewestFirst,
    val searchTerm: String = "",
    val tags: List<Tag> = emptyList()
)
