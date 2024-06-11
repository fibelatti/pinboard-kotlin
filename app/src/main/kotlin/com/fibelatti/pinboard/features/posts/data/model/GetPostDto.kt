package com.fibelatti.pinboard.features.posts.data.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class GetPostDto(
    val date: String,
    val user: String,
    @Contextual
    val posts: List<PostRemoteDto>,
)
