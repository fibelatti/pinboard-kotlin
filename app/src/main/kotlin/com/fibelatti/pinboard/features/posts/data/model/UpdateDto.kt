package com.fibelatti.pinboard.features.posts.data.model

import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
class UpdateDto(@Json(name = "update_time") val updateTime: String)
