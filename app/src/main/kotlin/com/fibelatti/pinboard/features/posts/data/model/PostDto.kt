package com.fibelatti.pinboard.features.posts.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fibelatti.core.functional.TwoWayMapper
import com.fibelatti.pinboard.core.AppConfig.PinboardApiLiterals
import com.fibelatti.pinboard.core.extension.replaceHtmlChars
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.posts.domain.model.PendingSync
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import javax.inject.Inject
import kotlinx.serialization.Serializable

const val POST_TABLE_NAME = "Posts"

@Serializable
@Entity(
    tableName = POST_TABLE_NAME,
    indices = [Index(value = ["shared"]), Index(value = ["toread"])],
)
data class PostDto(
    val href: String,
    val description: String?,
    val extended: String?,
    @PrimaryKey val hash: String,
    val time: String,
    val shared: String,
    val toread: String,
    val tags: String,
    val pendingSync: PendingSyncDto? = null,
)

class PostDtoMapper @Inject constructor(
    private val dateFormatter: DateFormatter,
) : TwoWayMapper<PostDto, Post> {

    override fun map(param: PostDto): Post = with(param) {
        Post(
            url = href,
            title = description.orEmpty(),
            description = extended.orEmpty(),
            id = hash,
            dateAdded = time,
            displayDateAdded = dateFormatter.dataFormatToDisplayFormat(time),
            private = shared == PinboardApiLiterals.NO,
            readLater = toread == PinboardApiLiterals.YES,
            tags = if (tags.isBlank()) {
                null
            } else {
                tags.replaceHtmlChars()
                    .split(PinboardApiLiterals.TAG_SEPARATOR)
                    .map(::Tag)
            },
            pendingSync = when (pendingSync) {
                PendingSyncDto.ADD -> PendingSync.ADD
                PendingSyncDto.UPDATE -> PendingSync.UPDATE
                PendingSyncDto.DELETE -> PendingSync.DELETE
                null -> null
            },
        )
    }

    override fun mapReverse(param: Post): PostDto = with(param) {
        PostDto(
            href = url,
            description = title,
            extended = description,
            hash = id,
            time = dateAdded,
            shared = if (private == true) PinboardApiLiterals.NO else PinboardApiLiterals.YES,
            toread = if (readLater == true) PinboardApiLiterals.YES else PinboardApiLiterals.NO,
            tags = tags?.joinToString(PinboardApiLiterals.TAG_SEPARATOR) { it.name }.orEmpty(),
            pendingSync = when (pendingSync) {
                PendingSync.ADD -> PendingSyncDto.ADD
                PendingSync.UPDATE -> PendingSyncDto.UPDATE
                PendingSync.DELETE -> PendingSyncDto.DELETE
                null -> null
            },
        )
    }
}
