package com.fibelatti.pinboard.features.tags.domain

import com.fibelatti.core.functional.Result

interface TagsRepository {

    suspend fun getAllTags(): Result<Map<String, Int>>
}
