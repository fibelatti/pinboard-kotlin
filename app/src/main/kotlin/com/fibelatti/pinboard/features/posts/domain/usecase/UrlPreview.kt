package com.fibelatti.pinboard.features.posts.domain.usecase

data class UrlPreview(
    val url: String,
    val title: String,
    val description: String? = null,
)
