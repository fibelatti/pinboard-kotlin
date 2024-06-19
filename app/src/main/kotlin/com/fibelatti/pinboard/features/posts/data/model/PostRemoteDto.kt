package com.fibelatti.pinboard.features.posts.data.model

import com.fibelatti.core.functional.Mapper
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Factory
import javax.inject.Inject

@Serializable
data class PostRemoteDto(
    val href: String,
    val description: String?,
    val extended: String?,
    val hash: String,
    val time: String,
    val shared: String,
    val toread: String,
    val tags: String,
)

@Factory
class PostRemoteDtoMapper @Inject constructor() : Mapper<PostRemoteDto, PostDto> {

    override fun map(param: PostRemoteDto): PostDto = with(param) {
        PostDto(
            href = href,
            description = description,
            extended = extended,
            hash = hash,
            time = time,
            shared = shared,
            toread = toread,
            tags = tags,
        )
    }
}
