package com.fibelatti.pinboard.features.posts.presentation

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.viewModelScope
import com.fibelatti.bookmarking.features.appstate.SortType
import com.fibelatti.bookmarking.features.filters.domain.SavedFiltersRepository
import com.fibelatti.bookmarking.features.filters.domain.model.SavedFilter
import com.fibelatti.bookmarking.features.posts.domain.PostVisibility
import com.fibelatti.bookmarking.features.posts.domain.usecase.GetAllPosts
import com.fibelatti.bookmarking.features.posts.domain.usecase.GetPostParams
import com.fibelatti.bookmarking.features.posts.domain.usecase.GetRecentPosts
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
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
import com.fibelatti.pinboard.features.appstate.Unread
import com.fibelatti.pinboard.features.appstate.Untagged
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class PostListViewModel(
    private val getAllPosts: GetAllPosts,
    private val getRecentPosts: GetRecentPosts,
    private val appStateRepository: AppStateRepository,
    private val savedFiltersRepository: SavedFiltersRepository,
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
            ),
        )
    }

    @VisibleForTesting
    fun getRecent(sorting: SortType, searchTerm: String, tags: List<Tag>) {
        val params = GetPostParams(sorting, searchTerm, GetPostParams.Tags.Tagged(tags))
        getRecentPosts(params)
            .onEach { result ->
                result.onSuccess { appStateRepository.runAction(SetPosts(it)) }
                    .onFailure(::handleError)
            }
            .launchIn(viewModelScope)
    }

    @VisibleForTesting
    fun getPublic(sorting: SortType, searchTerm: String, tags: List<Tag>, offset: Int) {
        launchGetAll(
            GetPostParams(
                sorting = sorting,
                searchTerm = searchTerm,
                tags = GetPostParams.Tags.Tagged(tags),
                visibility = PostVisibility.Public,
                offset = offset,
            ),
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
            ),
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
            ),
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
            ),
        )
    }

    @VisibleForTesting
    fun launchGetAll(params: GetPostParams) {
        getAllPosts(params)
            .onEach { result ->
                result.onSuccess { postListResult ->
                    val action = if (params.offset == 0) SetPosts(postListResult) else SetNextPostPage(postListResult)
                    appStateRepository.runAction(action)
                }.onFailure(::handleError)
            }
            .launchIn(viewModelScope)
    }

    fun saveFilter(savedFilter: SavedFilter) {
        launch {
            savedFiltersRepository.saveFilter(savedFilter)
        }
    }
}
