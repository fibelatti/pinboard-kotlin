package com.fibelatti.pinboard.features.linkding.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fibelatti.core.functional.TwoWayMapper
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.linkding.data.BookmarkLocal.Companion.TABLE_NAME
import com.fibelatti.pinboard.features.posts.data.model.PendingSyncDto
import com.fibelatti.pinboard.features.posts.domain.model.PendingSync
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import javax.inject.Inject

@Entity(
    tableName = TABLE_NAME,
    indices = [Index(value = ["shared"]), Index(value = ["unread"])],
)
data class BookmarkLocal(
    @PrimaryKey
    val id: String,
    val url: String,
    val title: String,
    val description: String,
    val notes: String? = null,
    val websiteTitle: String? = null,
    val websiteDescription: String? = null,
    val faviconUrl: String? = null,
    val isArchived: Boolean? = false,
    val unread: Boolean? = false,
    val shared: Boolean? = true,
    val tagNames: String? = null,
    @ColumnInfo(defaultValue = "")
    val dateAdded: String = "",
    val dateModified: String = "",
    val pendingSync: PendingSyncDto? = null,
) {

    companion object {

        const val TABLE_NAME = "LinkdingBookmarks"
    }
}

class BookmarkLocalMapper @Inject constructor(
    private val dateFormatter: DateFormatter,
) : TwoWayMapper<BookmarkLocal, Post> {

    override fun map(param: BookmarkLocal): Post = with(param) {
        // `dateAdded` wasn't originally part of the model; it will be empty immediately after the DB migration
        val dateAdded = this.dateAdded.ifEmpty { dateModified }

        Post(
            url = url,
            title = title,
            description = description,
            id = id,
            dateAdded = dateAdded,
            displayDateAdded = dateFormatter.dataFormatToDisplayFormat(dateAdded),
            dateModified = dateModified,
            displayDateModified = dateFormatter.dataFormatToDisplayFormat(dateModified),
            private = shared == false,
            readLater = unread == true,
            tags = tagNames?.ifBlank { null }?.split(" ")?.map(::Tag),
            notes = notes,
            websiteTitle = websiteTitle,
            websiteDescription = websiteDescription,
            faviconUrl = faviconUrl,
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
            faviconUrl = faviconUrl,
            isArchived = isArchived,
            unread = readLater,
            shared = private != true,
            tagNames = tags?.joinToString(separator = " ") { it.name },
            dateAdded = dateAdded,
            dateModified = dateModified,
            pendingSync = when (pendingSync) {
                PendingSync.ADD -> PendingSyncDto.ADD
                PendingSync.UPDATE -> PendingSyncDto.UPDATE
                PendingSync.DELETE -> PendingSyncDto.DELETE
                null -> null
            },
        )
    }
}
