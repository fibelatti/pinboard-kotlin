package com.fibelatti.pinboard.features.posts.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fibelatti.core.functional.TwoWayMapper
import com.fibelatti.pinboard.core.AppConfig.API_ENCODING
import com.fibelatti.pinboard.core.AppConfig.PinboardApiLiterals
import com.fibelatti.pinboard.core.extension.replaceHtmlChars
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.posts.domain.model.PendingSync
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.squareup.moshi.JsonClass
import java.net.URLDecoder
import java.net.URLEncoder
import javax.inject.Inject

const val POST_TABLE_NAME = "Posts"

@JsonClass(generateAdapter = true)
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
        val preparedUrl = listOf(
            ::preparePercentForDecoding,
            ::preparePlusForDecoding,
        ).fold(href) { current, preparation -> preparation(current) }

        Post(
            url = URLDecoder.decode(preparedUrl, API_ENCODING),
            title = description ?: "No title",
            description = extended.orEmpty(),
            id = hash,
            time = time,
            formattedTime = dateFormatter.tzFormatToDisplayFormat(time) ?: time,
            private = shared == PinboardApiLiterals.NO,
            readLater = toread == PinboardApiLiterals.YES,
            tags = if (tags.isBlank()) {
                null
            } else {
                tags.replaceHtmlChars()
                    .split(PinboardApiLiterals.TAG_SEPARATOR_RESPONSE)
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
            href = URLEncoder.encode(url, API_ENCODING),
            description = title,
            extended = description,
            hash = id,
            time = time,
            shared = if (private) PinboardApiLiterals.NO else PinboardApiLiterals.YES,
            toread = if (readLater) PinboardApiLiterals.YES else PinboardApiLiterals.NO,
            tags = tags?.joinToString(PinboardApiLiterals.TAG_SEPARATOR_RESPONSE) { it.name }.orEmpty(),
            pendingSync = when (pendingSync) {
                PendingSync.ADD -> PendingSyncDto.ADD
                PendingSync.UPDATE -> PendingSyncDto.UPDATE
                PendingSync.DELETE -> PendingSyncDto.DELETE
                null -> null
            },
        )
    }
}
