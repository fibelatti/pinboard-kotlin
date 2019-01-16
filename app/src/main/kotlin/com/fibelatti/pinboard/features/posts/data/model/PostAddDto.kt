package com.fibelatti.pinboard.features.posts.data.model

data class PostAddDto(
    val url: String,
    val description: String,
    val extended: String? = null,
    val tags: String? = null
)
