package com.fibelatti.bookmarking.pinboard.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public class UpdateDto(
    @SerialName(value = "update_time")
    public val updateTime: String,
)
