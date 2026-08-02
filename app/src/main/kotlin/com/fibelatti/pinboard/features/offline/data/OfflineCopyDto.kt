package com.fibelatti.pinboard.features.offline.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.fibelatti.core.functional.Mapper
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.features.offline.data.OfflineCopyDto.Companion.TABLE_NAME
import com.fibelatti.pinboard.features.offline.domain.model.OfflineCopy
import javax.inject.Inject

/**
 * A page saved for offline reading.
 *
 * There is deliberately no foreign key to [com.fibelatti.pinboard.features.posts.data.model.PostDto]
 * or [com.fibelatti.pinboard.features.linkding.data.BookmarkLocal]: a cascading delete would drop
 * this row without deleting the file it points at, orphaning it on disk forever. Deletion is always
 * explicit through the repository so that the row and the file go together.
 *
 * [url] and [title] are denormalized for the same reason — the list of offline copies has to render
 * without joining either backend's bookmark table, since the bookmark may be gone.
 */
@Entity(
    tableName = TABLE_NAME,
    primaryKeys = ["bookmarkId", "appMode"],
)
data class OfflineCopyDto(
    val bookmarkId: String,
    val appMode: String,
    val url: String,
    val title: String,
    val fileName: String,
    val sizeBytes: Long,
    val dateCreated: String,
    @ColumnInfo(defaultValue = "0")
    val truncated: Boolean = false,
) {

    companion object {

        const val TABLE_NAME = "OfflineCopies"
    }
}

class OfflineCopyDtoMapper @Inject constructor() : Mapper<OfflineCopyDto, OfflineCopy> {

    override fun map(param: OfflineCopyDto): OfflineCopy = OfflineCopy(
        bookmarkId = param.bookmarkId,
        appMode = AppMode.entries.firstOrNull { it.name == param.appMode } ?: AppMode.UNSET,
        url = param.url,
        title = param.title,
        fileName = param.fileName,
        sizeBytes = param.sizeBytes,
        dateCreated = param.dateCreated,
        truncated = param.truncated,
    )
}
