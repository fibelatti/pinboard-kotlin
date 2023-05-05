package com.fibelatti.pinboard.features.posts.domain.model

import com.fibelatti.pinboard.features.tags.domain.model.Tag

data class Post(
    val url: String,
    val title: String,
    val description: String,
    val hash: String = "",
    val time: String = "",
    val formattedTime: String = time,
    val private: Boolean,
    val readLater: Boolean,
    val tags: List<Tag>? = null,
    val pendingSync: PendingSync? = null,
)
