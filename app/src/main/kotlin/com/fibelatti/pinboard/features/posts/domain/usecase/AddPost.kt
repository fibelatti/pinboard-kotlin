package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.map
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AddPost @Inject constructor(
    private val postsRepository: PostsRepository,
    private val validateUrl: ValidateUrl
) : UseCaseWithParams<Post, AddPost.Params>() {

    override suspend fun run(params: Params): Result<Post> =
        validateUrl(params.url).map {
            withContext(NonCancellable) {
                postsRepository.add(
                    url = params.url,
                    title = params.title,
                    description = params.description,
                    private = params.private,
                    readLater = params.readLater,
                    tags = params.tags,
                    replace = params.replace
                )
            }
        }

    data class Params(
        val url: String,
        val title: String,
        val description: String? = null,
        val private: Boolean? = null,
        val readLater: Boolean? = null,
        val tags: List<Tag>? = null,
        val replace: Boolean = true
    ) {

        constructor(post: Post) : this(
            url = post.url,
            title = post.title,
            description = post.description,
            private = post.private,
            readLater = post.readLater,
            tags = post.tags
        )
    }
}
