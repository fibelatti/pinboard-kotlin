package com.fibelatti.bookmarking.features.posts.presentation

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.viewModelScope
import com.fibelatti.bookmarking.core.base.BaseViewModel
import com.fibelatti.bookmarking.features.appstate.All
import com.fibelatti.bookmarking.features.appstate.AppStateRepository
import com.fibelatti.bookmarking.features.appstate.PostListContent
import com.fibelatti.bookmarking.features.appstate.Private
import com.fibelatti.bookmarking.features.appstate.Public
import com.fibelatti.bookmarking.features.appstate.Recent
import com.fibelatti.bookmarking.features.appstate.SetNextPostPage
import com.fibelatti.bookmarking.features.appstate.SetPosts
import com.fibelatti.bookmarking.features.appstate.ShouldForceLoad
import com.fibelatti.bookmarking.features.appstate.ShouldLoadFirstPage
import com.fibelatti.bookmarking.features.appstate.ShouldLoadNextPage
import com.fibelatti.bookmarking.features.appstate.SortType
import com.fibelatti.bookmarking.features.appstate.Unread
import com.fibelatti.bookmarking.features.appstate.Untagged
import com.fibelatti.bookmarking.features.filters.domain.SavedFiltersRepository
import com.fibelatti.bookmarking.features.filters.domain.model.SavedFilter
import com.fibelatti.bookmarking.features.posts.domain.PostVisibility
import com.fibelatti.bookmarking.features.posts.domain.usecase.GetAllPosts
import com.fibelatti.bookmarking.features.posts.domain.usecase.GetPostParams
import com.fibelatti.bookmarking.features.posts.domain.usecase.GetRecentPosts
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
public class PostListViewModel(
    private val getAllPosts: GetAllPosts,
    private val getRecentPosts: GetRecentPosts,
    private val appStateRepository: AppStateRepository,
    private val savedFiltersRepository: SavedFiltersRepository,
) : BaseViewModel() {

    public fun loadContent(content: PostListContent) {
        coroutineContext.cancelChildren()

        val offset = when (val shouldLoad = content.shouldLoad) {
            is ShouldLoadFirstPage, ShouldForceLoad -> 0
            is ShouldLoadNextPage -> shouldLoad.offset
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
    internal fun getAll(
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
    internal fun getRecent(sorting: SortType, searchTerm: String, tags: List<Tag>) {
        val params = GetPostParams(sorting, searchTerm, GetPostParams.Tags.Tagged(tags))
        getRecentPosts(params)
            .onEach { result ->
                result.onSuccess { appStateRepository.runAction(SetPosts(it)) }
                    .onFailure(::handleError)
            }
            .launchIn(viewModelScope)
    }

    @VisibleForTesting
    internal fun getPublic(sorting: SortType, searchTerm: String, tags: List<Tag>, offset: Int) {
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
    internal fun getPrivate(sorting: SortType, searchTerm: String, tags: List<Tag>, offset: Int) {
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
    internal fun getUnread(sorting: SortType, searchTerm: String, tags: List<Tag>, offset: Int) {
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
    internal fun getUntagged(sorting: SortType, searchTerm: String, offset: Int) {
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
    internal fun launchGetAll(params: GetPostParams) {
        getAllPosts(params)
            .onEach { result ->
                result.onSuccess { postListResult ->
                    val action = if (params.offset == 0) SetPosts(postListResult) else SetNextPostPage(postListResult)
                    appStateRepository.runAction(action)
                }.onFailure(::handleError)
            }
            .launchIn(viewModelScope)
    }

    public fun saveFilter(savedFilter: SavedFilter) {
        launch {
            savedFiltersRepository.saveFilter(savedFilter)
        }
    }
}
