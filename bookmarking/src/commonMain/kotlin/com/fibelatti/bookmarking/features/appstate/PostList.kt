package com.fibelatti.bookmarking.features.appstate

import com.fibelatti.bookmarking.features.posts.domain.model.Post

public data class PostList(
    val list: List<Post>,
    val totalCount: Int,
    val canPaginate: Boolean,
)
