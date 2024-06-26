package com.fibelatti.bookmarking.features.posts.domain.usecase

public data class UrlPreview(
    val url: String,
    val title: String,
    val description: String? = null,
)
