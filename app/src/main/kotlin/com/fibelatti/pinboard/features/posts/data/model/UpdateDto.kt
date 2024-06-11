package com.fibelatti.pinboard.features.posts.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class UpdateDto(@SerialName(value = "update_time") val updateTime: String)
