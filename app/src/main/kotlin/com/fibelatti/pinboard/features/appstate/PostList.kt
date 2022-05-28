package com.fibelatti.pinboard.features.appstate

import androidx.recyclerview.widget.DiffUtil
import com.fibelatti.pinboard.features.posts.domain.model.Post

data class PostList(
    val totalCount: Int,
    val list: List<Post>,
    val diffResult: DiffUtil.DiffResult,
    val alreadyDisplayed: Boolean = false,
)
