package com.fibelatti.pinboard.features.posts.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class GenericResponseDto(@Json(name = "result_code") val resultCode: String)
