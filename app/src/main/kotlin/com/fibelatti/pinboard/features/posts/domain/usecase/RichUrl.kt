package com.fibelatti.pinboard.features.posts.domain.usecase

data class RichUrl(
    val url: String,
    val title: String,
    val imageUrl: String? = null
)
