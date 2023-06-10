package com.fibelatti.pinboard.features.appstate

import androidx.compose.runtime.Stable
import com.fibelatti.pinboard.features.posts.domain.model.Post

@Stable
data class PostList(
    val list: List<Post>,
    val totalCount: Int,
    val canPaginate: Boolean,
    val shouldScrollToTop: Boolean,
)
