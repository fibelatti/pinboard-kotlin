package com.fibelatti.pinboard.features.tags.data

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface TagsApi {

    @GET("tags/get")
    suspend fun getTags(): Map<String, Int>

    @GET("tags/rename")
    suspend fun renameTag(
        @Query("old") oldName: String,
        @Query("new") newName: String,
    ): RenameTagResponseDto
}

@Serializable
class RenameTagResponseDto(val result: String)
