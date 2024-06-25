package com.fibelatti.bookmarking.linkding.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fibelatti.bookmarking.core.util.DateFormatter
import com.fibelatti.bookmarking.features.posts.data.model.PendingSyncDto
import com.fibelatti.bookmarking.features.posts.domain.model.PendingSync
import com.fibelatti.bookmarking.features.posts.domain.model.Post
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.bookmarking.linkding.data.BookmarkLocal.Companion.TABLE_NAME
import com.fibelatti.core.functional.TwoWayMapper
import org.koin.core.annotation.Factory

@Entity(
    tableName = TABLE_NAME,
    indices = [Index(value = ["shared"]), Index(value = ["unread"])],
)
internal data class BookmarkLocal(
    @PrimaryKey
    val id: String,
    val url: String,
    val title: String,
    val description: String,
    val notes: String? = null,
    val websiteTitle: String? = null,
    val websiteDescription: String? = null,
    val isArchived: Boolean? = false,
    val unread: Boolean? = false,
    val shared: Boolean? = true,
    val tagNames: String? = null,
    val dateModified: String = "",
    val pendingSync: PendingSyncDto? = null,
) {

    companion object {

        const val TABLE_NAME: String = "LinkdingBookmarks"
    }
}

@Factory
internal class BookmarkLocalMapper(
    private val dateFormatter: DateFormatter,
) : TwoWayMapper<BookmarkLocal, Post> {

    override fun map(param: BookmarkLocal): Post = with(param) {
        Post(
            url = url,
            title = title,
            description = description,
            id = id,
            time = dateModified,
            formattedTime = dateFormatter.tzFormatToDisplayFormat(dateModified),
            private = shared == false,
            readLater = unread == true,
            tags = tagNames?.ifBlank { null }?.split(" ")?.sorted()?.map(::Tag),
            notes = notes,
            websiteTitle = websiteTitle,
            websiteDescription = websiteDescription,
            isArchived = isArchived,
            pendingSync = when (pendingSync) {
                PendingSyncDto.ADD -> PendingSync.ADD
                PendingSyncDto.UPDATE -> PendingSync.UPDATE
                PendingSyncDto.DELETE -> PendingSync.DELETE
                null -> null
            },
        )
    }

    override fun mapReverse(param: Post): BookmarkLocal = with(param) {
        BookmarkLocal(
            id = id,
            url = url,
            title = title,
            description = description,
            notes = notes,
            websiteTitle = websiteTitle,
            websiteDescription = websiteDescription,
            isArchived = isArchived,
            unread = readLater,
            shared = private != true,
            tagNames = tags?.joinToString(separator = " ") { it.name },
            dateModified = time,
            pendingSync = when (pendingSync) {
                PendingSync.ADD -> PendingSyncDto.ADD
                PendingSync.UPDATE -> PendingSyncDto.UPDATE
                PendingSync.DELETE -> PendingSyncDto.DELETE
                null -> null
            },
        )
    }
}
