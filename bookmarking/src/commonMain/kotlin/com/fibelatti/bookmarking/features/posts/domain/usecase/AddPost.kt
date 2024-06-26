package com.fibelatti.bookmarking.features.posts.domain.usecase

import com.fibelatti.bookmarking.features.posts.domain.PostsRepository
import com.fibelatti.bookmarking.features.posts.domain.model.Post
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.map
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory

@Factory
public class AddPost(
    private val postsRepository: PostsRepository,
    private val validateUrl: ValidateUrl,
) : UseCaseWithParams<Post, Post>() {

    override suspend fun run(params: Post): Result<Post> = validateUrl(params.url).map {
        withContext(NonCancellable) {
            postsRepository.add(post = params)
        }
    }
}
