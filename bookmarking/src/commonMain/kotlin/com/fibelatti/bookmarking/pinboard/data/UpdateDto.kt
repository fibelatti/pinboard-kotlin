package com.fibelatti.bookmarking.pinboard.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class UpdateDto(
    @SerialName(value = "update_time")
    val updateTime: String,
)
