package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.archcomponents.BaseViewModel
import com.fibelatti.core.archcomponents.LiveEvent
import com.fibelatti.core.archcomponents.MutableLiveEvent
import com.fibelatti.core.archcomponents.postEvent
import com.fibelatti.core.archcomponents.setEvent
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.NewestFirst
import com.fibelatti.pinboard.features.appstate.SetPosts
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.GetAllPosts
import com.fibelatti.pinboard.features.posts.domain.usecase.GetParams
import com.fibelatti.pinboard.features.posts.domain.usecase.GetRecentPosts
import kotlinx.coroutines.launch
import javax.inject.Inject

class PostListViewModel @Inject constructor(
    private val getAllPosts: GetAllPosts,
    private val getRecentPosts: GetRecentPosts,
    private val appStateRepository: AppStateRepository
) : BaseViewModel() {

    val loading: LiveEvent<Boolean> get() = _loading
    private val _loading = MutableLiveEvent<Boolean>().apply { setEvent(true) }

    fun getAll(sorting: SortType, searchTerm: String, tags: List<String>?) {
        launchGetAll(sorting, searchTerm, tags)
    }

    fun getRecent(sorting: SortType, searchTerm: String, tags: List<String>?) {
        launch {
            _loading.postEvent(true)
            getRecentPosts(GetParams(sorting, searchTerm, tags))
                .mapCatching { appStateRepository.runAction(SetPosts(it)) }
                .onSuccess { _loading.postEvent(false) }
                .onFailure(::handleError)
        }
    }

    fun getPublic(sorting: SortType, searchTerm: String, tags: List<String>?) {
        launchGetAll(sorting, searchTerm, tags) { allPosts -> allPosts.filterNot(Post::private) }
    }

    fun getPrivate(sorting: SortType, searchTerm: String, tags: List<String>?) {
        launchGetAll(sorting, searchTerm, tags) { allPosts -> allPosts.filter(Post::private) }
    }

    fun getUnread(sorting: SortType, searchTerm: String, tags: List<String>?) {
        launchGetAll(sorting, searchTerm, tags) { allPosts -> allPosts.filter(Post::readLater) }
    }

    fun getUntagged(sorting: SortType, searchTerm: String) {
        launchGetAll(sorting, searchTerm) { allPosts -> allPosts.filter { it.tags.isEmpty() } }
    }

    private fun launchGetAll(
        sorting: SortType = NewestFirst,
        searchTerm: String,
        tags: List<String>? = null,
        extraFilter: (List<Post>) -> List<Post> = { it }
    ) {
        launch {
            _loading.postEvent(true)
            getAllPosts(GetParams(sorting, searchTerm, tags))
                .mapCatching(extraFilter)
                .mapCatching { appStateRepository.runAction(SetPosts(it)) }
                .onSuccess { _loading.postEvent(false) }
                .onFailure(::handleError)
        }
    }
}
