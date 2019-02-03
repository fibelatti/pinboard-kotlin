package com.fibelatti.pinboard.features.tags.domain.usecase

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCase
import com.fibelatti.pinboard.features.tags.domain.TagsRepository
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import javax.inject.Inject

class GetAllTags @Inject constructor(
    private val tagsRepository: TagsRepository
) : UseCase<List<Tag>>() {

    override suspend fun run(): Result<List<Tag>> = tagsRepository.getAllTags()
}
