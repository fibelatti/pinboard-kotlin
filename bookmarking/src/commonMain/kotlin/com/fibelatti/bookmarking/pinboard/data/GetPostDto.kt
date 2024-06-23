package com.fibelatti.bookmarking.pinboard.data

import com.fibelatti.bookmarking.core.network.SkipBadElementsListSerializer
import kotlinx.serialization.Serializable

@Serializable
public data class GetPostDto(
    val date: String,
    val user: String,
    @Serializable(with = SkipBadElementsListSerializer::class)
    val posts: List<PostRemoteDto>,
)
