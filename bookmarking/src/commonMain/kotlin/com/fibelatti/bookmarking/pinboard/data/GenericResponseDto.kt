package com.fibelatti.bookmarking.pinboard.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public class GenericResponseDto(
    @SerialName(value = "result_code")
    public val resultCode: String,
)
