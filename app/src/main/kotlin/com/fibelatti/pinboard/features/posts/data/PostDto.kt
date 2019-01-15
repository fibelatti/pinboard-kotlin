package com.fibelatti.pinboard.features.posts.data

import com.fibelatti.core.functional.TwoWayMapper
import com.fibelatti.pinboard.core.AppConfig.PinboardApiLiterals
import com.fibelatti.pinboard.features.posts.domain.Post
import javax.inject.Inject

data class PostDto(
    val href: String,
    val description: String,
    val extended: String,
    val time: String,
    val shared: String,
    val toread: String,
    val tags: String
)

class PostDtoMapper @Inject constructor() : TwoWayMapper<PostDto, Post> {

    override fun map(param: PostDto): Post = with(param) {
        Post(
            url = href,
            description = description,
            extendedDescription = extended,
            time = time,
            public = shared == PinboardApiLiterals.YES,
            unread = toread == PinboardApiLiterals.YES,
            tags = tags.split(PinboardApiLiterals.TAG_SEPARATOR_RESPONSE)
        )
    }

    override fun mapReverse(param: Post): PostDto = with(param) {
        PostDto(
            href = url,
            description = description,
            extended = extendedDescription,
            time = time,
            shared = if (public) PinboardApiLiterals.YES else PinboardApiLiterals.NO,
            toread = if (unread) PinboardApiLiterals.YES else PinboardApiLiterals.NO,
            tags = tags.joinToString(PinboardApiLiterals.TAG_SEPARATOR_RESPONSE)
        )
    }
}
