package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.archcomponents.BaseViewModel
import com.fibelatti.core.archcomponents.LiveEvent
import com.fibelatti.core.archcomponents.MutableLiveEvent
import com.fibelatti.core.archcomponents.postEvent
import com.fibelatti.core.archcomponents.setEvent
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.core.provider.CoroutineLauncher
import com.fibelatti.pinboard.features.posts.domain.Sorting
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.GetAllPosts
import com.fibelatti.pinboard.features.posts.domain.usecase.GetRecentPosts
import javax.inject.Inject

class PostListViewModel @Inject constructor(
    private val getAllPosts: GetAllPosts,
    private val getRecentPosts: GetRecentPosts,
    coroutineLauncher: CoroutineLauncher
) : BaseViewModel(coroutineLauncher) {

    val posts: LiveEvent<List<Post>> get() = _posts
    private val _posts = MutableLiveEvent<List<Post>>().apply { setEvent(emptyList()) }

    val loading: LiveEvent<Boolean> get() = _loading
    private val _loading = MutableLiveEvent<Boolean>().apply { setEvent(true) }

    fun getAll(sorting: Sorting, tags: List<String>? = null) {
        startInBackground {
            _loading.postEvent(true)
            getAllPosts(GetAllPosts.Params(tags, sorting))
                .onSuccess {
                    _posts.postEvent(it)
                    _loading.postEvent(false)
                }
                .onFailure(::handleError)
        }
    }

    fun getRecent(sorting: Sorting, tags: List<String>? = null) {
        startInBackground {
            _loading.postEvent(true)
            getRecentPosts(GetRecentPosts.Params(tags, sorting))
                .onSuccess {
                    _posts.postEvent(it)
                    _loading.postEvent(false)
                }
                .onFailure(::handleError)
        }
    }
}
