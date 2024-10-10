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
    @SerialName(value = "website_title")
    val websiteTitle: String? = null,
    @SerialName(value = "website_description")
    val websiteDescription: String? = null,
    @SerialName(value = "is_archived")
    val isArchived: Boolean? = false,
    val unread: Boolean? = false,
    val shared: Boolean? = true,
    @SerialName(value = "tag_names")
    val tagNames: List<String>? = null,
    @SerialName(value = "date_added")
    val dateAdded: String? = null,
    @SerialName(value = "date_modified")
    val dateModified: String? = null,
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

    private val dateRegex = "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}).\\d+Z$".toRegex()

    override fun map(param: BookmarkRemote): Post = with(param) {
        val dateAdded = dateWithoutMillis(input = this.dateAdded)
        val dateModified = dateWithoutMillis(input = this.dateModified ?: this.dateAdded)

        Post(
            url = url,
            title = title.orEmpty(),
            description = description.orEmpty(),
            id = requireNotNull(id?.toString()),
            dateAdded = dateAdded,
            displayDateAdded = dateFormatter.tzFormatToDisplayFormat(dateAdded),
            dateModified = dateModified,
            displayDateModified = dateFormatter.tzFormatToDisplayFormat(dateModified),
            private = shared == false,
            readLater = unread == true,
            tags = tagNames?.sorted()?.map(::Tag),
            notes = notes,
            websiteTitle = websiteTitle,
            websiteDescription = websiteDescription,
            isArchived = isArchived,
        )
    }

    private fun dateWithoutMillis(input: String?): String {
        return dateRegex.find(input.orEmpty())?.groupValues?.getOrNull(index = 1)
            ?.plus("Z")
            ?: dateFormatter.nowAsTzFormat()
    }
}
