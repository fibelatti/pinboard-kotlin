package com.fibelatti.pinboard.features.posts.presentation

import androidx.annotation.VisibleForTesting
import com.fibelatti.core.archcomponents.BaseViewModel
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.onFailure
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.SetPosts
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.posts.domain.usecase.GetAllPosts
import com.fibelatti.pinboard.features.posts.domain.usecase.GetPostParams
import com.fibelatti.pinboard.features.posts.domain.usecase.GetRecentPosts
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.coroutines.launch
import javax.inject.Inject

class PostListViewModel @Inject constructor(
    private val getAllPosts: GetAllPosts,
    private val getRecentPosts: GetRecentPosts,
    private val appStateRepository: AppStateRepository
) : BaseViewModel() {

    fun getAll(sorting: SortType, searchTerm: String, tags: List<Tag>) {
        launchGetAll(
            GetPostParams(
                sorting,
                searchTerm,
                GetPostParams.Tags.Tagged(tags)
            )
        )
    }

    fun getRecent(sorting: SortType, searchTerm: String, tags: List<Tag>) {
        launch {
            getRecentPosts(GetPostParams(sorting, searchTerm, GetPostParams.Tags.Tagged(tags)))
                .mapCatching { appStateRepository.runAction(SetPosts(it)) }
                .onFailure(::handleError)
        }
    }

    fun getPublic(sorting: SortType, searchTerm: String, tags: List<Tag>) {
        launchGetAll(
            GetPostParams(
                sorting,
                searchTerm,
                GetPostParams.Tags.Tagged(tags),
                GetPostParams.Visibility.Public
            )
        )
    }

    fun getPrivate(sorting: SortType, searchTerm: String, tags: List<Tag>) {
        launchGetAll(
            GetPostParams(
                sorting,
                searchTerm,
                GetPostParams.Tags.Tagged(tags),
                GetPostParams.Visibility.Private
            )
        )
    }

    fun getUnread(sorting: SortType, searchTerm: String, tags: List<Tag>) {
        launchGetAll(
            GetPostParams(
                sorting,
                searchTerm,
                GetPostParams.Tags.Tagged(tags),
                readLater = true
            )
        )
    }

    fun getUntagged(sorting: SortType, searchTerm: String) {
        launchGetAll(
            GetPostParams(
                sorting,
                searchTerm,
                GetPostParams.Tags.Untagged
            )
        )
    }

    @VisibleForTesting
    fun launchGetAll(params: GetPostParams) {
        launch {
            getAllPosts(params)
                .mapCatching { appStateRepository.runAction(SetPosts(it)) }
                .onFailure(::handleError)
        }
    }
}
