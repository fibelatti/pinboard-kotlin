package com.fibelatti.pinboard.features.tags.domain

import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.core.functional.Result
import kotlinx.coroutines.flow.Flow

interface TagsRepository {

    fun getAllTags(): Flow<Result<List<Tag>>>

    suspend fun renameTag(oldName: String, newName: String): Result<List<Tag>>
}
