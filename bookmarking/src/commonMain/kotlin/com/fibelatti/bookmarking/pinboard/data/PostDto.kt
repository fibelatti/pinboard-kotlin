package com.fibelatti.bookmarking.pinboard.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fibelatti.bookmarking.core.Config.Pinboard
import com.fibelatti.bookmarking.core.extension.replaceHtmlChars
import com.fibelatti.bookmarking.core.util.DateFormatter
import com.fibelatti.bookmarking.features.posts.data.model.PendingSyncDto
import com.fibelatti.bookmarking.features.posts.domain.model.PendingSync
import com.fibelatti.bookmarking.features.posts.domain.model.Post
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.core.functional.TwoWayMapper
import kotlinx.serialization.Serializable
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import org.koin.core.annotation.Factory

public const val POST_TABLE_NAME: String = "Posts"

@Serializable
@Entity(
    tableName = POST_TABLE_NAME,
    indices = [Index(value = ["shared"]), Index(value = ["toread"])],
)
internal data class PostDto(
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

@Factory
internal class PostDtoMapper(
    private val dateFormatter: DateFormatter,
) : TwoWayMapper<PostDto, Post> {

    override fun map(param: PostDto): Post = with(param) {
        val preparedUrl = listOf(
            ::preparePercentForDecoding,
            ::preparePlusForDecoding,
        ).fold(href) { current, preparation -> preparation(current) }

        Post(
            url = UrlEncoderUtil.decode(preparedUrl),
            title = description.orEmpty(),
            description = extended.orEmpty(),
            id = hash,
            time = time,
            formattedTime = dateFormatter.tzFormatToDisplayFormat(time),
            private = shared == Pinboard.LITERAL_NO,
            readLater = toread == Pinboard.LITERAL_YES,
            tags = if (tags.isBlank()) {
                null
            } else {
                tags.replaceHtmlChars()
                    .split(Pinboard.TAG_SEPARATOR)
                    .sorted()
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

    private fun preparePercentForDecoding(source: String): String = source.replace(
        regex = "%(?![0-9a-fA-F]{2})".toRegex(),
        replacement = "%25",
    )

    private fun preparePlusForDecoding(source: String): String = source.replace(
        oldValue = "+",
        newValue = "%2B",
    )

    override fun mapReverse(param: Post): PostDto = with(param) {
        PostDto(
            href = UrlEncoderUtil.encode(url),
            description = title,
            extended = description,
            hash = id,
            time = time,
            shared = if (private == true) Pinboard.LITERAL_NO else Pinboard.LITERAL_YES,
            toread = if (readLater == true) Pinboard.LITERAL_YES else Pinboard.LITERAL_NO,
            tags = tags?.joinToString(Pinboard.TAG_SEPARATOR) { it.name }.orEmpty(),
            pendingSync = when (pendingSync) {
                PendingSync.ADD -> PendingSyncDto.ADD
                PendingSync.UPDATE -> PendingSyncDto.UPDATE
                PendingSync.DELETE -> PendingSyncDto.DELETE
                null -> null
            },
        )
    }
}
