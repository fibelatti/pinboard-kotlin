package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.pinboard.features.appstate.NewestFirst
import com.fibelatti.pinboard.features.appstate.OldestFirst
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.posts.domain.model.Post
import javax.inject.Inject

class Sort @Inject constructor() : UseCaseWithParams<List<Post>, Sort.Params>() {

    override suspend fun run(params: Params): Result<List<Post>> =
        Success(
            when (params.sorting) {
                NewestFirst -> params.posts.sortedByDescending { it.time }
                OldestFirst -> params.posts.sortedBy { it.time }
            }
        )

    data class Params(val posts: List<Post>, val sorting: SortType)
}
