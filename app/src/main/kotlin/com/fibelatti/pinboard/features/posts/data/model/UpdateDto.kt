package com.fibelatti.pinboard.features.posts.data.model

import com.squareup.moshi.Json

data class UpdateDto(@Json(name = "update_time") val updateTime: String)
