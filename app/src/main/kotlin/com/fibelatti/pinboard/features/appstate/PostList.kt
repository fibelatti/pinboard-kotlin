package com.fibelatti.pinboard.features.appstate

import com.fibelatti.bookmarking.features.posts.domain.model.Post

data class PostList(
    val list: List<Post>,
    val totalCount: Int,
    val canPaginate: Boolean,
)
