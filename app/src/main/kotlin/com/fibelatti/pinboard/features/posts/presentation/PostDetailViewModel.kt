package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.archcomponents.BaseViewModel
import com.fibelatti.core.archcomponents.LiveEvent
import com.fibelatti.core.archcomponents.MutableLiveEvent
import com.fibelatti.core.archcomponents.postEvent
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.DeletePost
import kotlinx.coroutines.launch
import javax.inject.Inject

class PostDetailViewModel @Inject constructor(
    private val deletePost: DeletePost
) : BaseViewModel() {

    val loading: LiveEvent<Boolean> get() = _loading
    private val _loading = MutableLiveEvent<Boolean>()

    val deleted: LiveEvent<Unit> get() = _deleted
    private val _deleted = MutableLiveEvent<Unit>()

    fun deletePost(post: Post) {
        launch {
            _loading.postEvent(true)
            deletePost(post.url)
                .onSuccess {
                    _loading.postEvent(false)
                    _deleted.postEvent(Unit)
                }
                .onFailure(::handleError)
        }
    }
}
