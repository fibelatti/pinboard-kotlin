package com.fibelatti.pinboard.features.posts.domain.model

data class Post(
    val url: String,
    val description: String,
    val extendedDescription: String,
    val time: String,
    val public: Boolean,
    val unread: Boolean,
    val tags: List<String>
)
