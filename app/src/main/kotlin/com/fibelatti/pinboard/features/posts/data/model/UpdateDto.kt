package com.fibelatti.pinboard.features.posts.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
class UpdateDto(@SerializedName("update_time") val updateTime: String)
