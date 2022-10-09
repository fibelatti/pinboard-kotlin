package com.fibelatti.pinboard.features.tags.data

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface TagsApi {

    @GET("tags/get")
    suspend fun getTags(): Map<String, String>

    @GET("tags/rename")
    suspend fun renameTag(
        @Query("old") oldName: String,
        @Query("new") newName: String,
    ): RenameTagResponseDto
}

@JsonClass(generateAdapter = true)
class RenameTagResponseDto(val result: String)
