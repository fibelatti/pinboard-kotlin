package com.fibelatti.pinboard.features.posts.data.model

import com.fibelatti.pinboard.core.network.SkipBadElementsListSerializer
import kotlinx.serialization.Serializable

@Serializable
data class GetPostDto(
    val date: String,
    val user: String,
    @Serializable(with = SkipBadElementsListSerializer::class)
    val posts: List<PostRemoteDto>,
)
