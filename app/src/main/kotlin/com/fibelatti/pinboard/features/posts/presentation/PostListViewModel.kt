package com.fibelatti.pinboard.features.posts.presentation

import androidx.annotation.VisibleForTesting
import com.fibelatti.core.archcomponents.BaseViewModel
import com.fibelatti.core.extension.exhaustive
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.onFailure
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.PostList
import com.fibelatti.pinboard.features.appstate.Private
import com.fibelatti.pinboard.features.appstate.Public
import com.fibelatti.pinboard.features.appstate.Recent
import com.fibelatti.pinboard.features.appstate.SetPosts
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.appstate.Unread
import com.fibelatti.pinboard.features.appstate.Untagged
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

    fun loadContent(content: PostList) {
        val offset = 0

        when (content.category) {
            is All -> {
                getAll(content.sortType, content.searchParameters.term, content.searchParameters.tags, offset)
            }
            is Recent -> {
                getRecent(content.sortType, content.searchParameters.term, content.searchParameters.tags)
            }
            is Public -> {
                getPublic(content.sortType, content.searchParameters.term, content.searchParameters.tags, offset)
            }
            is Private -> {
                getPrivate(content.sortType, content.searchParameters.term, content.searchParameters.tags, offset)
            }
            is Unread -> {
                getUnread(content.sortType, content.searchParameters.term, content.searchParameters.tags, offset)
            }
            is Untagged -> {
                getUntagged(content.sortType, content.searchParameters.term, offset)
            }
        }.exhaustive
    }

    @VisibleForTesting
    fun getAll(sorting: SortType, searchTerm: String, tags: List<Tag>, offset: Int) {
        launchGetAll(
            GetPostParams(
                sorting,
                searchTerm,
                GetPostParams.Tags.Tagged(tags),
                offset = offset
            )
        )
    }

    @VisibleForTesting
    fun getRecent(sorting: SortType, searchTerm: String, tags: List<Tag>) {
        launch {
            getRecentPosts(GetPostParams(sorting, searchTerm, GetPostParams.Tags.Tagged(tags)))
                .mapCatching { appStateRepository.runAction(SetPosts(it)) }
                .onFailure(::handleError)
        }
    }

    @VisibleForTesting
    fun getPublic(sorting: SortType, searchTerm: String, tags: List<Tag>, offset: Int) {
        launchGetAll(
            GetPostParams(
                sorting,
                searchTerm,
                GetPostParams.Tags.Tagged(tags),
                GetPostParams.Visibility.Public,
                offset = offset
            )
        )
    }

    @VisibleForTesting
    fun getPrivate(sorting: SortType, searchTerm: String, tags: List<Tag>, offset: Int) {
        launchGetAll(
            GetPostParams(
                sorting,
                searchTerm,
                GetPostParams.Tags.Tagged(tags),
                GetPostParams.Visibility.Private,
                offset = offset
            )
        )
    }

    @VisibleForTesting
    fun getUnread(sorting: SortType, searchTerm: String, tags: List<Tag>, offset: Int) {
        launchGetAll(
            GetPostParams(
                sorting,
                searchTerm,
                GetPostParams.Tags.Tagged(tags),
                readLater = true,
                offset = offset
            )
        )
    }

    @VisibleForTesting
    fun getUntagged(sorting: SortType, searchTerm: String, offset: Int) {
        launchGetAll(
            GetPostParams(
                sorting,
                searchTerm,
                GetPostParams.Tags.Untagged,
                offset = offset
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
