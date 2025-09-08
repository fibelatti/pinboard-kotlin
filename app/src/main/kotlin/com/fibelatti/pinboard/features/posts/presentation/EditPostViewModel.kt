package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.AddPostContent
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.EditPostContent
import com.fibelatti.pinboard.features.appstate.PostSaved
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.InvalidUrlException
import com.fibelatti.pinboard.features.tags.domain.TagManagerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.plugins.ClientRequestException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class EditPostViewModel @Inject constructor(
    scope: CoroutineScope,
    dispatchers: CoroutineDispatcher,
    sharingStarted: SharingStarted,
    appStateRepository: AppStateRepository,
    private val tagManagerRepository: TagManagerRepository,
    private val addPost: AddPost,
    private val resourceProvider: ResourceProvider,
) : BaseViewModel(scope, appStateRepository), TagManagerRepository by tagManagerRepository {

    // Initial `post` state when the screen is first opened
    private val initialPostState: StateFlow<Post?> = appStateRepository.appState
        .map { appState ->
            when (appState.content) {
                is AddPostContent -> {
                    Post(
                        url = "",
                        title = "",
                        description = "",
                        private = appState.content.defaultPrivate,
                        readLater = appState.content.defaultReadLater,
                        tags = appState.content.defaultTags.ifEmpty { null },
                    )
                }

                is EditPostContent -> appState.content.post

                else -> null
            }
        }
        .flowOn(dispatchers)
        .stateIn(scope = scope, started = sharingStarted, initialValue = null)

    // Source of all changes to the screen state, only takes null from `initialPostState`
    private val interactions: MutableSharedFlow<(Post?) -> Post> = MutableSharedFlow()

    private val _postState: StateFlow<Post?> = initialPostState
        .flatMapLatest { post -> interactions.scan(initial = post) { current, interaction -> interaction(current) } }
        .flowOn(dispatchers) // to avoid sync issues with compose TextField state
        .stateIn(scope = scope, started = sharingStarted, initialValue = null)
    val postState: Flow<Post> get() = _postState.filterNotNull()

    private val _screenState = MutableStateFlow(ScreenState())
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    init {
        merge(filteredContent<AddPostContent>(), filteredContent<EditPostContent>())
            .onEach { content ->
                _screenState.update { ScreenState(isNewBookmark = content is AddPostContent) }
            }
            .launchIn(scope)

        scope.launch {
            tagManagerState.collectLatest { tagState ->
                updatePost { post -> post.copy(tags = tagState.tags.ifEmpty { null }) }
            }
        }
    }

    fun updatePost(body: (Post) -> Post) {
        _postState.value ?: return
        scope.launch {
            interactions.emit { current -> body(requireNotNull(current)) }
        }
    }

    fun saveLink() {
        scope.launch {
            val params = validateData() ?: return@launch

            _screenState.update { currentState ->
                currentState.copy(isLoading = true)
            }

            addPost(params)
                .onSuccess {
                    _screenState.update { currentState ->
                        currentState.copy(saved = true)
                    }
                    runDelayedAction(PostSaved(it))
                }
                .onFailure { error ->
                    _screenState.update { currentState ->
                        currentState.copy(isLoading = false)
                    }
                    when (error) {
                        is InvalidUrlException, is ClientRequestException -> {
                            _screenState.update { currentState ->
                                currentState.copy(
                                    invalidUrlError = resourceProvider.getString(R.string.validation_error_invalid_url),
                                )
                            }
                        }

                        else -> handleError(error)
                    }
                }
        }
    }

    fun userNotified() {
        _screenState.update { currentState ->
            currentState.copy(saved = false)
        }
    }

    fun hasPendingChanges(): Boolean = _postState.value != initialPostState.value

    private fun validateData(): Post? {
        _screenState.update { currentState ->
            currentState.copy(
                invalidUrlError = "",
                invalidTitleError = "",
            )
        }

        val post = _postState.value ?: return null

        when {
            post.url.isBlank() -> {
                _screenState.update { currentState ->
                    currentState.copy(
                        invalidUrlError = resourceProvider.getString(R.string.validation_error_empty_url),
                    )
                }
            }

            post.displayTitle.isBlank() -> {
                _screenState.update { currentState ->
                    currentState.copy(
                        invalidTitleError = resourceProvider.getString(R.string.validation_error_empty_title),
                    )
                }
            }

            else -> return post
        }

        return null
    }

    data class ScreenState(
        val isNewBookmark: Boolean = true,
        val isLoading: Boolean = false,
        val invalidUrlError: String = "",
        val invalidTitleError: String = "",
        val saved: Boolean = false,
    )
}
