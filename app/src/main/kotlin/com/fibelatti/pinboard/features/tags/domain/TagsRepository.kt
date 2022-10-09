package com.fibelatti.pinboard.features.tags.domain

import com.fibelatti.core.functional.Result
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagsRepository {

    fun getAllTags(): Flow<Result<List<Tag>>>

    suspend fun renameTag(oldName: String, newName: String): Result<List<Tag>>
}
