package com.fibelatti.bookmarking.features.posts.domain.model

import com.fibelatti.bookmarking.features.tags.domain.model.Tag

public data class Post(
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
    val websiteTitle: String? = null,
    val websiteDescription: String? = null,
    val isArchived: Boolean? = null,
    val pendingSync: PendingSync? = null,
) {

    val displayTitle: String
        get() = title.ifEmpty { websiteTitle ?: "" }
    val displayDescription: String
        get() = description.ifEmpty { websiteDescription ?: "" }
}
