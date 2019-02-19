package com.fibelatti.pinboard.features.posts.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.fibelatti.core.functional.TwoWayMapper
import com.fibelatti.pinboard.core.AppConfig.API_ENCODING
import com.fibelatti.pinboard.core.AppConfig.PinboardApiLiterals
import com.fibelatti.pinboard.features.posts.domain.model.Post
import java.net.URLDecoder
import java.net.URLEncoder
import javax.inject.Inject

const val POST_TABLE_NAME = "Posts"

@Entity(tableName = POST_TABLE_NAME)
data class PostDto(
    val href: String,
    val description: String,
    val extended: String,
    @PrimaryKey val hash: String,
    val time: String,
    val shared: String,
    val toread: String,
    val tags: String
) {
    @Ignore
    constructor() : this("", "", "", "", "", "", "", "")
}

class PostDtoMapper @Inject constructor() : TwoWayMapper<PostDto, Post> {

    override fun map(param: PostDto): Post = with(param) {
        Post(
            url = URLDecoder.decode(href, API_ENCODING),
            description = description,
            extendedDescription = extended,
            hash = hash,
            time = time,
            private = shared == PinboardApiLiterals.NO,
            readLater = toread == PinboardApiLiterals.YES,
            tags = tags.split(PinboardApiLiterals.TAG_SEPARATOR_RESPONSE).sorted()
        )
    }

    override fun mapReverse(param: Post): PostDto = with(param) {
        PostDto(
            href = URLEncoder.encode(url, API_ENCODING),
            description = description,
            extended = extendedDescription,
            hash = hash,
            time = time,
            shared = if (private) PinboardApiLiterals.NO else PinboardApiLiterals.YES,
            toread = if (readLater) PinboardApiLiterals.YES else PinboardApiLiterals.NO,
            tags = tags.joinToString(PinboardApiLiterals.TAG_SEPARATOR_RESPONSE)
        )
    }
}
