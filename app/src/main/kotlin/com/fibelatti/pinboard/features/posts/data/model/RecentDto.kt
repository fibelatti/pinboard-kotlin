package com.fibelatti.pinboard.features.posts.data.model

import androidx.annotation.Keep

@Keep
class RecentDto(
    val date: String,
    val user: String,
    val posts: List<PostDto>
)
