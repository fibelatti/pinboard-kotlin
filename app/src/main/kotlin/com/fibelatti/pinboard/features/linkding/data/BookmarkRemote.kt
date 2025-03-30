package com.fibelatti.pinboard.features.linkding.data

import com.fibelatti.core.functional.Mapper
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import javax.inject.Inject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResponseRemote<T>(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<T>,
)

@Serializable
data class BookmarkRemote(
    val id: Int?,
    val url: String,
    val title: String?,
    val description: String?,
    val notes: String?,
    @SerialName(value = "date_added")
    val dateAdded: String,
    @SerialName(value = "date_modified")
    val dateModified: String? = null,
    @SerialName(value = "website_title")
    val websiteTitle: String? = null,
    @SerialName(value = "website_description")
    val websiteDescription: String? = null,
    @SerialName(value = "favicon_url")
    val faviconUrl: String? = null,
    @SerialName(value = "is_archived")
    val isArchived: Boolean? = false,
    val unread: Boolean? = false,
    val shared: Boolean? = true,
    @SerialName(value = "tag_names")
    val tagNames: List<String>? = null,
)

@Serializable
data class TagRemote(
    val id: Int,
    val name: String,
    @SerialName(value = "date_added")
    val dateAdded: String,
)

class BookmarkRemoteMapper @Inject constructor(
    private val dateFormatter: DateFormatter,
) : Mapper<BookmarkRemote, Post> {

    override fun map(param: BookmarkRemote): Post = with(param) {
        Post(
            url = url,
            title = title.orEmpty(),
            description = description.orEmpty(),
            id = requireNotNull(id?.toString()),
            dateAdded = dateAdded,
            displayDateAdded = dateFormatter.dataFormatToDisplayFormat(input = dateAdded),
            dateModified = dateModified ?: dateAdded,
            displayDateModified = dateFormatter.dataFormatToDisplayFormat(input = dateModified ?: dateAdded),
            private = shared == false,
            readLater = unread == true,
            tags = tagNames?.sorted()?.map(::Tag),
            notes = notes,
            websiteTitle = websiteTitle,
            websiteDescription = websiteDescription,
            faviconUrl = faviconUrl,
            isArchived = isArchived,
        )
    }
}
