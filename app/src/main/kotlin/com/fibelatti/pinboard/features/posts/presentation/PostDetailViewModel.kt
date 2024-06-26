package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.bookmarking.features.appstate.AppStateRepository
import com.fibelatti.bookmarking.features.appstate.PostDeleted
import com.fibelatti.bookmarking.features.appstate.PostSaved
import com.fibelatti.bookmarking.features.posts.domain.model.Post
import com.fibelatti.bookmarking.features.posts.domain.usecase.AddPost
import com.fibelatti.bookmarking.features.posts.domain.usecase.DeletePost
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class PostDetailViewModel(
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
            _screenState.update { currentState ->
                currentState.copy(isLoading = true)
            }
            addPost(
                params = post.copy(
                    readLater = !(post.readLater ?: false),
                ),
            ).onSuccess {
                _screenState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        updated = Success(value = true),
                    )
                }
                appStateRepository.runDelayedAction(PostSaved(it))
            }.onFailure {
                _screenState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        updated = Failure(it),
                    )
                }
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
