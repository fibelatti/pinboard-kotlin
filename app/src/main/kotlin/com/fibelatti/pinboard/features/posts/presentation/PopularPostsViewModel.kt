package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.PostSaved
import com.fibelatti.pinboard.features.appstate.SetPopularPosts
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.GetPopularPosts
import com.fibelatti.pinboard.features.user.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PopularPostsViewModel @Inject constructor(
    private val appStateRepository: AppStateRepository,
    private val userRepository: UserRepository,
    private val getPopularPosts: GetPopularPosts,
    private val addPost: AddPost,
) : BaseViewModel() {

    val loading: Flow<Boolean> get() = _loading.filterNotNull()
    private val _loading = MutableStateFlow<Boolean?>(null)

    val saved: Flow<Unit> get() = _saved
    private val _saved = MutableSharedFlow<Unit>()

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
                private = userRepository.defaultPrivate ?: false,
                readLater = userRepository.defaultReadLater ?: false,
                tags = userRepository.defaultTags,
            )
            if (userRepository.editAfterSharing is EditAfterSharing.BeforeSaving) {
                appStateRepository.runAction(PostSaved(newPost))
            } else {
                addBookmark(post = newPost)
            }
        }
    }

    private suspend fun addBookmark(post: Post) {
        _loading.value = true
        addPost(AddPost.Params(post))
            .onSuccess {
                _loading.value = false
                _saved.emit(Unit)
                appStateRepository.runAction(PostSaved(it))
            }
            .onFailure { error ->
                _loading.value = false
                handleError(error)
            }
    }
}
