package com.fibelatti.pinboard.features.posts.domain.model

import androidx.compose.runtime.Stable
import com.fibelatti.pinboard.features.tags.domain.model.Tag

@Stable
data class Post(
    val url: String,
    val title: String,
    val description: String,
    val id: String = "",
    val dateAdded: String = "",
    val displayDateAdded: String = dateAdded,
    val dateModified: String = dateAdded,
    val displayDateModified: String = dateModified,
    val private: Boolean? = null,
    val readLater: Boolean? = null,
    val tags: List<Tag>? = null,
    val notes: String? = null,
    val websiteTitle: String? = null,
    val websiteDescription: String? = null,
    val faviconUrl: String? = null,
    val isArchived: Boolean? = null,
    val pendingSync: PendingSync? = null,
) {

    val displayTitle: String
        get() = title.ifEmpty { websiteTitle ?: "" }
    val displayDescription: String
        get() = description.ifEmpty { websiteDescription ?: "" }
    val resolvedFaviconUrl: String?
        get() = faviconUrl ?: gstaticFaviconUrl

    private val gstaticFaviconUrl: String?
        get() = domainRegex.find(url)?.groupValues?.firstOrNull()?.let {
            "https://t1.gstatic.com/faviconV2?client=SOCIAL&type=FAVICON&fallback_opts=TYPE,SIZE,URL&url=$it&size=32"
        }

    companion object {

        val EMPTY = Post(url = "", title = "", description = "")

        private val domainRegex = Regex("[a-z]+://[^/]+")
    }
}
