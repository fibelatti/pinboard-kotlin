package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.features.posts.domain.model.Post

data class PostList(
    val list: List<Post>,
    val totalCount: Int,
    val canPaginate: Boolean,
)
