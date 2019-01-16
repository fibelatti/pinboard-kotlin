package com.fibelatti.pinboard.features.posts.data.model

import com.squareup.moshi.Json

data class GenericResponseDto(@Json(name = "result_code") val resultCode: String)

enum class ApiResultCodes(val code: String) {
    DONE("done"),
    SOMETHING_WENT_WRONG("something went wrong"),
    NOT_FOUND("item not found"),
    MISSING_URL("missing url")
}
