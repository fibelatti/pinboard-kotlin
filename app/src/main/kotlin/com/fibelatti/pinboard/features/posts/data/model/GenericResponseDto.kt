package com.fibelatti.pinboard.features.posts.data.model

import com.fibelatti.pinboard.core.network.ApiResultCodes
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class GenericResponseDto(@SerialName(value = "result_code") val resultCode: String)

/**
 * Returns `true` if the receiver represents a successful operation, false otherwise.
 */
val GenericResponseDto.isDone: Boolean
    get() = resultCode == ApiResultCodes.DONE.code
