package com.fibelatti.bookmarking.features.tags.domain

import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.core.functional.Result
import kotlinx.coroutines.flow.Flow

public interface TagsRepository {

    public fun getAllTags(): Flow<Result<List<Tag>>>

    public suspend fun renameTag(oldName: String, newName: String): Result<List<Tag>>
}
