package com.fibelatti.pinboard.features.posts.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
class GenericResponseDto(@SerializedName("result_code") val resultCode: String)
