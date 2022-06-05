package com.fibelatti.pinboard.features.posts.presentation

import androidx.annotation.VisibleForTesting
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.PostListContent
import com.fibelatti.pinboard.features.appstate.Private
import com.fibelatti.pinboard.features.appstate.Public
import com.fibelatti.pinboard.features.appstate.Recent
import com.fibelatti.pinboard.features.appstate.SetNextPostPage
import com.fibelatti.pinboard.features.appstate.SetPosts
import com.fibelatti.pinboard.features.appstate.ShouldForceLoad
import com.fibelatti.pinboard.features.appstate.ShouldLoadFirstPage
import com.fibelatti.pinboard.features.appstate.ShouldLoadNextPage
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.appstate.Unread
import com.fibelatti.pinboard.features.appstate.Untagged
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.posts.domain.usecase.GetAllPosts
import com.fibelatti.pinboard.features.posts.domain.usecase.GetPostParams
import com.fibelatti.pinboard.features.posts.domain.usecase.GetRecentPosts
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostListViewModel @Inject constructor(
    private val getAllPosts: GetAllPosts,
    private val getRecentPosts: GetRecentPosts,
    private val appStateRepository: AppStateRepository,
) : BaseViewModel() {

    fun loadContent(content: PostListContent) {
        coroutineContext.cancelChildren()

        val offset = when (content.shouldLoad) {
            is ShouldLoadFirstPage, ShouldForceLoad -> 0
            is ShouldLoadNextPage -> content.shouldLoad.offset
            else -> return
        }

        when (content.category) {
            is All -> {
                getAll(
                    sorting = content.sortType,
                    searchTerm = content.searchParameters.term,
                    tags = content.searchParameters.tags,
                    offset = offset,
                    forceRefresh = content.shouldLoad is ShouldForceLoad,
                )
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
        }
    }

    @VisibleForTesting
    fun getAll(
        sorting: SortType,
        searchTerm: String,
        tags: List<Tag>,
        offset: Int,
        forceRefresh: Boolean,
    ) {
        launchGetAll(
            GetPostParams(
                sorting = sorting,
                searchTerm = searchTerm,
                tags = GetPostParams.Tags.Tagged(tags),
                offset = offset,
                forceRefresh = forceRefresh,
            )
        )
    }

    @VisibleForTesting
    fun getRecent(sorting: SortType, searchTerm: String, tags: List<Tag>) {
        launch {
            val params = GetPostParams(sorting, searchTerm, GetPostParams.Tags.Tagged(tags))
            getRecentPosts(params).collect { result ->
                result.onSuccess { appStateRepository.runAction(SetPosts(it)) }.onFailure(::handleError)
            }
        }
    }

    @VisibleForTesting
    fun getPublic(sorting: SortType, searchTerm: String, tags: List<Tag>, offset: Int) {
        launchGetAll(
            GetPostParams(
                sorting = sorting,
                searchTerm = searchTerm,
                tags = GetPostParams.Tags.Tagged(tags),
                visibility = PostVisibility.Public,
                offset = offset
            )
        )
    }

    @VisibleForTesting
    fun getPrivate(sorting: SortType, searchTerm: String, tags: List<Tag>, offset: Int) {
        launchGetAll(
            GetPostParams(
                sorting = sorting,
                searchTerm = searchTerm,
                tags = GetPostParams.Tags.Tagged(tags),
                visibility = PostVisibility.Private,
                offset = offset,
            )
        )
    }

    @VisibleForTesting
    fun getUnread(sorting: SortType, searchTerm: String, tags: List<Tag>, offset: Int) {
        launchGetAll(
            GetPostParams(
                sorting = sorting,
                searchTerm = searchTerm,
                tags = GetPostParams.Tags.Tagged(tags),
                readLater = true,
                offset = offset,
            )
        )
    }

    @VisibleForTesting
    fun getUntagged(sorting: SortType, searchTerm: String, offset: Int) {
        launchGetAll(
            GetPostParams(
                sorting = sorting,
                searchTerm = searchTerm,
                tags = GetPostParams.Tags.Untagged,
                offset = offset,
            )
        )
    }

    @VisibleForTesting
    fun launchGetAll(params: GetPostParams) {
        launch {
            getAllPosts(params).collect { result ->
                result.onSuccess {
                    val action = if (params.offset == 0) SetPosts(it) else SetNextPostPage(it)
                    appStateRepository.runAction(action)
                }.onFailure(::handleError)
            }
        }
    }
}
