package com.fibelatti.pinboard.features.posts.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetPostDto(
    val date: String,
    val user: String,
    val posts: List<PostDto>
)
