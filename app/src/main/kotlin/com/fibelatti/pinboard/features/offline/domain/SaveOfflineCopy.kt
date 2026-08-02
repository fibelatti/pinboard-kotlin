package com.fibelatti.pinboard.features.offline.domain

import com.fibelatti.core.functional.ResultUseCaseWithParams
import com.fibelatti.core.functional.coMapCatching
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.features.offline.domain.model.OfflineCopy
import com.fibelatti.pinboard.features.posts.domain.model.Post
import javax.inject.Inject

class SaveOfflineCopy @Inject constructor(
    private val offlineCopyBuilder: OfflineCopyBuilder,
    private val offlineCopyRepository: OfflineCopyRepository,
) : ResultUseCaseWithParams<SaveOfflineCopy.Params, OfflineCopy> {

    override suspend operator fun invoke(params: Params): Result<OfflineCopy> {
        val post: Post = params.post

        return offlineCopyBuilder.build(url = post.url, fallbackTitle = post.displayTitle)
            .coMapCatching { output: OfflineCopyBuilder.Output ->
                offlineCopyRepository.save(
                    appMode = params.appMode,
                    bookmarkId = post.id,
                    url = post.url,
                    title = post.displayTitle,
                    html = output.html,
                    truncated = output.truncated,
                ).getOrThrow()
            }
    }

    data class Params(
        val post: Post,
        val appMode: AppMode,
    )
}
