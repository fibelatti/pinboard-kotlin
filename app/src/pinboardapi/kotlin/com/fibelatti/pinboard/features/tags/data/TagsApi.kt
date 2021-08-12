package com.fibelatti.pinboard.features.tags.data

import retrofit2.http.GET

interface TagsApi {

    @GET("tags/get")
    suspend fun getTags(): Map<String, String>
}
