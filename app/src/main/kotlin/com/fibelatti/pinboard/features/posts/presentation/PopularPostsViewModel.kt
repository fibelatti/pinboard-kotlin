package com.fibelatti.pinboard.features.posts.presentation

import androidx.annotation.StringRes
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.PostSaved
import com.fibelatti.pinboard.features.appstate.SetPopularPosts
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.GetPopularPosts
import com.fibelatti.pinboard.features.user.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PopularPostsViewModel @Inject constructor(
    private val appStateRepository: AppStateRepository,
    private val userRepository: UserRepository,
    private val postsRepository: PostsRepository,
    private val getPopularPosts: GetPopularPosts,
    private val addPost: AddPost,
) : BaseViewModel() {

    private val _screenState = MutableStateFlow(ScreenState())
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    fun getPosts() {
        launch {
            getPopularPosts()
                .onSuccess { appStateRepository.runAction(SetPopularPosts(it)) }
                .onFailure(::handleError)
        }
    }

    fun saveLink(post: Post) {
        launch {
            _screenState.update { currentState -> currentState.copy(isLoading = true) }

            val existingPost = postsRepository.getPost(url = post.url).getOrNull()
            val newPost = post.copy(
                private = userRepository.defaultPrivate ?: false,
                readLater = userRepository.defaultReadLater ?: false,
                tags = userRepository.defaultTags,
            )

            when {
                existingPost != null -> {
                    _screenState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            savedMessage = R.string.posts_existing_feedback,
                        )
                    }

                    appStateRepository.runDelayedAction(PostSaved(existingPost))
                }

                userRepository.editAfterSharing is EditAfterSharing.BeforeSaving -> {
                    _screenState.update { currentState -> currentState.copy(isLoading = false) }

                    appStateRepository.runDelayedAction(PostSaved(newPost))
                }

                else -> addBookmark(post = newPost)
            }
        }
    }

    private suspend fun addBookmark(post: Post) {
        addPost(post)
            .onSuccess {
                _screenState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        savedMessage = R.string.posts_saved_feedback,
                    )
                }

                appStateRepository.runDelayedAction(PostSaved(it))
            }
            .onFailure { error ->
                _screenState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        savedMessage = null,
                    )
                }

                handleError(error)
            }
    }

    fun userNotified() {
        _screenState.update { currentState ->
            currentState.copy(
                isLoading = false,
                savedMessage = null,
            )
        }
    }

    data class ScreenState(
        val isLoading: Boolean = false,
        @StringRes val savedMessage: Int? = null,
    )
}
