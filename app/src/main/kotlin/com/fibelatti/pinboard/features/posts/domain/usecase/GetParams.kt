package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.pinboard.features.appstate.NewestFirst
import com.fibelatti.pinboard.features.appstate.SortType

data class GetParams(
    val sorting: SortType = NewestFirst,
    val searchTerm: String = "",
    val tags: List<String>? = null
)
