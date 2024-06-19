package com.fibelatti.pinboard.features.linkding.data

import com.fibelatti.core.functional.Mapper
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Factory
import javax.inject.Inject

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

@Factory
class BookmarkRemoteMapper @Inject constructor(
    private val dateFormatter: DateFormatter,
) : Mapper<BookmarkRemote, Post> {

    override fun map(param: BookmarkRemote): Post = with(param) {
        val time = (dateModified ?: dateAdded)
            ?.substringBeforeLast(".")?.plus("Z") // Drop the milliseconds
            ?: dateFormatter.nowAsTzFormat()

        Post(
            url = url,
            title = title.orEmpty(),
            description = description.orEmpty(),
            id = requireNotNull(id?.toString()),
            time = time,
            formattedTime = dateFormatter.tzFormatToDisplayFormat(time) ?: time,
            private = shared == false,
            readLater = unread == true,
            tags = tagNames?.sorted()?.map(::Tag),
            notes = notes,
            websiteTitle = websiteTitle,
            websiteDescription = websiteDescription,
            isArchived = isArchived,
        )
    }
}
