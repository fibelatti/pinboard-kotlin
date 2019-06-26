package com.fibelatti.pinboard.features.posts.data.model

import androidx.annotation.Keep

@Keep
data class GetPostDto(
    val date: String,
    val user: String,
    val posts: List<PostDto>
)
