package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.archcomponents.BaseViewModel
import com.fibelatti.core.archcomponents.LiveEvent
import com.fibelatti.core.archcomponents.MutableLiveEvent
import com.fibelatti.core.archcomponents.postEvent
import com.fibelatti.core.archcomponents.setEvent
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.GetAllPosts
import com.fibelatti.pinboard.features.posts.domain.usecase.GetRecentPosts
import com.fibelatti.pinboard.features.posts.domain.usecase.Sort
import com.fibelatti.pinboard.features.posts.domain.usecase.SortType
import kotlinx.coroutines.launch
import javax.inject.Inject

class PostListViewModel @Inject constructor(
    private val getAllPosts: GetAllPosts,
    private val getRecentPosts: GetRecentPosts,
    private val sort: Sort
) : BaseViewModel() {

    val posts: LiveEvent<List<Post>> get() = _posts
    private val _posts = MutableLiveEvent<List<Post>>()

    val loading: LiveEvent<Boolean> get() = _loading
    private val _loading = MutableLiveEvent<Boolean>().apply { setEvent(true) }

    fun getAll(sorting: SortType, tags: List<String>? = null) {
        launch {
            _loading.postEvent(true)
            getAllPosts(GetAllPosts.Params(tags, sorting))
                .onSuccess {
                    _posts.postEvent(it)
                    _loading.postEvent(false)
                }
                .onFailure(::handleError)
        }
    }

    fun getRecent(sorting: SortType, tags: List<String>? = null) {
        launch {
            _loading.postEvent(true)
            getRecentPosts(GetRecentPosts.Params(tags, sorting))
                .onSuccess {
                    _posts.postEvent(it)
                    _loading.postEvent(false)
                }
                .onFailure(::handleError)
        }
    }

    fun sort(posts: List<Post>, sorting: SortType) {
        launch {
            sort(Sort.Params(posts, sorting)).onSuccess { _posts.postEvent(it) }
        }
    }
}
