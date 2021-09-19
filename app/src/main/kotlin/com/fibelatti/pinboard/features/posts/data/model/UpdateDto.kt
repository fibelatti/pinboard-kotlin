package com.fibelatti.pinboard.features.posts.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class UpdateDto(@Json(name = "update_time") val updateTime: String)
