package com.fibelatti.pinboard.features.posts.domain.model

import androidx.compose.runtime.Stable
import com.fibelatti.pinboard.features.tags.domain.model.Tag

@Stable
data class Post(
    val url: String,
    val title: String,
    val description: String,
    val id: String = "",
    val time: String = "",
    val formattedTime: String = time,
    val private: Boolean? = null,
    val readLater: Boolean? = null,
    val tags: List<Tag>? = null,
    val notes: String? = null,
    val isArchived: Boolean? = null,
    val pendingSync: PendingSync? = null,
)
