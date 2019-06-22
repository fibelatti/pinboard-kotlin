package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.archcomponents.BaseViewModel
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.onFailure
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.NewestFirst
import com.fibelatti.pinboard.features.appstate.SetPosts
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.GetAllPosts
import com.fibelatti.pinboard.features.posts.domain.usecase.GetParams
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
        launchGetAll(sorting, searchTerm, tags)
    }

    fun getRecent(sorting: SortType, searchTerm: String, tags: List<Tag>) {
        launch {
            getRecentPosts(GetParams(sorting, searchTerm, tags))
                .mapCatching { appStateRepository.runAction(SetPosts(it)) }
                .onFailure(::handleError)
        }
    }

    fun getPublic(sorting: SortType, searchTerm: String, tags: List<Tag>) {
        launchGetAll(sorting, searchTerm, tags) { allPosts -> allPosts.filterNot(Post::private) }
    }

    fun getPrivate(sorting: SortType, searchTerm: String, tags: List<Tag>) {
        launchGetAll(sorting, searchTerm, tags) { allPosts -> allPosts.filter(Post::private) }
    }

    fun getUnread(sorting: SortType, searchTerm: String, tags: List<Tag>) {
        launchGetAll(sorting, searchTerm, tags) { allPosts -> allPosts.filter(Post::readLater) }
    }

    fun getUntagged(sorting: SortType, searchTerm: String) {
        launchGetAll(sorting, searchTerm, tags = emptyList()) { allPosts -> allPosts.filter { it.tags.isEmpty() } }
    }

    private fun launchGetAll(
        sorting: SortType = NewestFirst,
        searchTerm: String,
        tags: List<Tag>,
        extraFilter: (List<Post>) -> List<Post> = { it }
    ) {
        launch {
            getAllPosts(GetParams(sorting, searchTerm, tags))
                .mapCatching(extraFilter)
                .mapCatching { appStateRepository.runAction(SetPosts(it)) }
                .onFailure(::handleError)
        }
    }
}
