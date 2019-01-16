package com.fibelatti.pinboard.features.tags.data

import retrofit2.Response
import retrofit2.http.GET

class TagsDataSource

interface TagsApi {

    @GET("/tags/get")
    fun getTags(): Response<TagsDto>
}
