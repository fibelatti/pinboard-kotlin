package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.presentation.PostListDiffUtil

data class PostList(
    val totalCount: Int,
    val list: List<Post>,
    val diffUtil: PostListDiffUtil,
    val alreadyDisplayed: Boolean = false
)
