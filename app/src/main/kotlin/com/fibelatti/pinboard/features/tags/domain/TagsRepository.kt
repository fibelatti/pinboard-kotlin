package com.fibelatti.pinboard.features.tags.domain

import com.fibelatti.core.functional.Result
import com.fibelatti.pinboard.features.tags.domain.model.Tag

interface TagsRepository {

    suspend fun getAllTags(): Result<List<Tag>>
}
