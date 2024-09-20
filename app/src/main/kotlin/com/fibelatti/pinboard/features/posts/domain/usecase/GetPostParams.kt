package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.pinboard.core.AppConfig.DEFAULT_PAGE_SIZE
import com.fibelatti.pinboard.features.appstate.ByDateAddedNewestFirst
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.tags.domain.model.Tag

data class GetPostParams(
    val sorting: SortType = ByDateAddedNewestFirst,
    val searchTerm: String = "",
    val tags: Tags = Tags.None,
    val visibility: PostVisibility = PostVisibility.None,
    val readLater: Boolean = false,
    val limit: Int = DEFAULT_PAGE_SIZE,
    val offset: Int = 0,
    val forceRefresh: Boolean = false,
) {
    sealed class Tags {
        data object Untagged : Tags()
        data class Tagged(val tags: List<Tag>?) : Tags()
        data object None : Tags()
    }
}
