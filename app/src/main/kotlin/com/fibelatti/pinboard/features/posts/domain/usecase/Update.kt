package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCase
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import javax.inject.Inject

class Update @Inject constructor(
    private val postsRepository: PostsRepository
) : UseCase<String>() {

    override suspend fun run(): Result<String> = postsRepository.update()
}
