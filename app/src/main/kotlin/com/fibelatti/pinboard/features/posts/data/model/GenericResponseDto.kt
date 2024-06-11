package com.fibelatti.pinboard.features.posts.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class GenericResponseDto(@SerialName(value = "result_code") val resultCode: String)
