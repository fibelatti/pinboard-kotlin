package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.PostDeleted
import com.fibelatti.pinboard.features.appstate.PostSaved
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.DeletePost
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val appStateRepository: AppStateRepository,
    private val deletePost: DeletePost,
    private val addPost: AddPost,
) : BaseViewModel() {

    private val _screenState = MutableStateFlow(ScreenState())
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    fun deletePost(post: Post) {
        launch {
            _screenState.update { currentState ->
                currentState.copy(isLoading = true)
            }

            deletePost(params = post)
                .onSuccess {
                    _screenState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            deleted = Success(value = true),
                        )
                    }
                    appStateRepository.runDelayedAction(PostDeleted)
                }
                .onFailure {
                    _screenState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            deleted = Failure(it),
                        )
                    }
                }
        }
    }

    fun toggleReadLater(post: Post) {
        launch {
            editPost(newPost = post.copy(readLater = !(post.readLater ?: false)))
        }
    }

    fun addTags(post: Post, tags: List<Tag>) {
        launch {
            if (post.tags?.containsAll(tags) == true) return@launch

            editPost(newPost = post.copy(tags = post.tags.orEmpty().plus(tags).distinct()))
        }
    }

    private suspend fun editPost(newPost: Post) {
        _screenState.update { currentState -> currentState.copy(isLoading = true) }
        addPost(params = newPost)
            .onSuccess { addedPost ->
                _screenState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        updated = Success(value = true),
                    )
                }
                appStateRepository.runDelayedAction(PostSaved(addedPost))
            }
            .onFailure { throwable ->
                _screenState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        updated = Failure(throwable),
                    )
                }
            }
    }

    fun userNotified() {
        _screenState.update { currentState ->
            currentState.copy(
                deleted = Success(value = false),
                updated = Success(value = false),
            )
        }
    }

    data class ScreenState(
        val isLoading: Boolean = false,
        val deleted: Result<Boolean> = Success(value = false),
        val updated: Result<Boolean> = Success(value = false),
    )
}
