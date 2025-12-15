package com.fibelatti.pinboard.features.posts.presentation

import androidx.annotation.VisibleForTesting
import com.fibelatti.core.functional.onEachFailure
import com.fibelatti.core.functional.onEachSuccess
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.ByDateAddedNewestFirst
import com.fibelatti.pinboard.features.appstate.PostAction
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
import com.fibelatti.pinboard.features.appstate.find
import com.fibelatti.pinboard.features.filters.domain.SavedFiltersRepository
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import com.fibelatti.pinboard.features.posts.domain.usecase.GetAllPosts
import com.fibelatti.pinboard.features.posts.domain.usecase.GetPostParams
import com.fibelatti.pinboard.features.posts.domain.usecase.GetRecentPosts
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

@HiltViewModel
class PostListViewModel @Inject constructor(
    scope: CoroutineScope,
    appStateRepository: AppStateRepository,
    private val savedFiltersRepository: SavedFiltersRepository,
    private val getAllPosts: GetAllPosts,
    private val getRecentPosts: GetRecentPosts,
) : BaseViewModel(scope, appStateRepository) {

    init {
        scope.launch {
            appStateRepository.appState
                .mapNotNull { appState -> appState.content.find<PostListContent>() }
                .collectLatest { content: PostListContent ->
                    val shouldLoadContent: Boolean = content.shouldLoad is ShouldLoadFirstPage ||
                        content.shouldLoad is ShouldForceLoad ||
                        content.shouldLoad is ShouldLoadNextPage

                    if (shouldLoadContent) {
                        loadContent(content)
                    }
                }
        }
    }

    @VisibleForTesting
    fun loadContent(content: PostListContent) {
        val offset: Int = when (content.shouldLoad) {
            is ShouldLoadFirstPage, ShouldForceLoad -> 0
            is ShouldLoadNextPage -> content.shouldLoad.offset
            else -> return
        }

        when (content.category) {
            is All -> {
                getAll(
                    sorting = content.sortType,
                    searchTerm = content.searchParameters.term,
                    tags = GetPostParams.Tags.Tagged(content.searchParameters.tags),
                    offset = offset,
                    forceRefresh = content.shouldLoad is ShouldForceLoad,
                )
            }

            is Recent -> {
                getRecent(
                    sorting = content.sortType,
                )
            }

            is Public -> {
                getAll(
                    sorting = content.sortType,
                    visibility = PostVisibility.Public,
                    offset = offset,
                )
            }

            is Private -> {
                getAll(
                    sorting = content.sortType,
                    visibility = PostVisibility.Private,
                    offset = offset,
                )
            }

            is Unread -> {
                getAll(
                    sorting = content.sortType,
                    readLater = true,
                    offset = offset,
                )
            }

            is Untagged -> {
                getAll(
                    sorting = content.sortType,
                    tags = GetPostParams.Tags.Untagged,
                    offset = offset,
                )
            }
        }
    }

    private fun getRecent(sorting: SortType) {
        getRecentPosts(params = GetPostParams(sorting))
            .onEachSuccess { result: PostListResult ->
                runAction(SetPosts(result))
            }
            .onEachFailure(::handleError)
            .launchIn(scope)
    }

    private fun getAll(
        sorting: SortType = ByDateAddedNewestFirst,
        searchTerm: String = "",
        tags: GetPostParams.Tags = GetPostParams.Tags.None,
        visibility: PostVisibility = PostVisibility.None,
        readLater: Boolean = false,
        offset: Int = 0,
        forceRefresh: Boolean = false,
    ) {
        val params = GetPostParams(
            sorting = sorting,
            searchTerm = searchTerm,
            tags = tags,
            visibility = visibility,
            readLater = readLater,
            offset = offset,
            forceRefresh = forceRefresh,
        )

        getAllPosts(params)
            .onEachSuccess { result: PostListResult ->
                val action: PostAction = if (params.offset == 0) {
                    SetPosts(result)
                } else {
                    SetNextPostPage(result)
                }
                runAction(action)
            }
            .onEachFailure(::handleError)
            .launchIn(scope)
    }

    fun saveFilter(savedFilter: SavedFilter) {
        scope.launch {
            savedFiltersRepository.saveFilter(savedFilter)
        }
    }
}
