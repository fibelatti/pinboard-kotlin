package com.fibelatti.pinboard.features.posts.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fibelatti.core.archcomponents.BaseViewModel
import com.fibelatti.core.archcomponents.LiveEvent
import com.fibelatti.core.archcomponents.MutableLiveEvent
import com.fibelatti.core.archcomponents.postEvent
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.PostSaved
import com.fibelatti.pinboard.features.appstate.SetPopularPosts
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.GetPopularPosts
import com.fibelatti.pinboard.features.user.domain.UserRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class PopularPostsViewModel @Inject constructor(
    private val appStateRepository: AppStateRepository,
    private val userRepository: UserRepository,
    private val getPopularPosts: GetPopularPosts,
    private val addPost: AddPost
) : BaseViewModel() {

    val loading: LiveData<Boolean> get() = _loading
    private val _loading = MutableLiveData<Boolean>()

    val saved: LiveEvent<Unit> get() = _saved
    private val _saved = MutableLiveEvent<Unit>()

    fun getPosts() {
        launch {
            getPopularPosts()
                .onSuccess { appStateRepository.runAction(SetPopularPosts(it)) }
                .onFailure(::handleError)
        }
    }

    fun saveLink(post: Post) {
        launch {
            val newPost = post.copy(
                private = userRepository.getDefaultPrivate() ?: false,
                readLater = userRepository.getDefaultReadLater() ?: false,
            )
            if (userRepository.getEditAfterSharing() is EditAfterSharing.BeforeSaving) {
                appStateRepository.runAction(PostSaved(newPost))
            } else {
                addBookmark(post = newPost)
            }
        }
    }

    private suspend fun addBookmark(post: Post) {
        _loading.postValue(true)
        addPost(AddPost.Params(post))
            .onSuccess {
                _loading.postValue(false)
                _saved.postEvent(Unit)
                appStateRepository.runAction(PostSaved(it))
            }
            .onFailure { error ->
                _loading.postValue(false)
                handleError(error)
            }
    }
}
