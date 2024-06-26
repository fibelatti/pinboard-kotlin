package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.bookmarking.features.appstate.AppStateRepository
import com.fibelatti.bookmarking.features.appstate.PostSaved
import com.fibelatti.bookmarking.features.posts.domain.PostsRepository
import com.fibelatti.bookmarking.features.posts.domain.model.Post
import com.fibelatti.bookmarking.features.posts.domain.usecase.AddPost
import com.fibelatti.bookmarking.features.posts.domain.usecase.InvalidUrlException
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class EditPostViewModel(
    private val appStateRepository: AppStateRepository,
    private val postsRepository: PostsRepository,
    private val addPost: AddPost,
    private val resourceProvider: ResourceProvider,
    scope: CoroutineScope,
    sharingStarted: SharingStarted,
) : BaseViewModel() {

    private var searchJob: Job? = null

    // Source of all changes to the screen state, takes null to enable it being initialized with initializePost
    private val interactions: Channel<(Post?) -> Post> = Channel()
    private val _postState: StateFlow<IndexedValue<Post>?> = interactions.receiveAsFlow()
        .scan(initial = null) { post: Post?, interaction -> interaction(post) }
        .filterNotNull() // interactions never return null but the scan return type is inferred by the initial value
        .distinctUntilChanged() // ignore interactions that result in no changes
        .withIndex() // add an index to easily figure out if the value has changed
        .flowOn(Dispatchers.Main.immediate) // to avoid sync issues with compose TextField state
        .stateIn(
            scope = scope,
            started = sharingStarted,
            initialValue = null,
        )
    val postState: Flow<Post> get() = _postState.filterNotNull().map { it.value }

    private val _screenState = MutableStateFlow(ScreenState())
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    fun initializePost(post: Post) {
        if (_postState.value != null) return
        interactions.trySend { post }
    }

    fun updatePost(body: (Post) -> Post) {
        _postState.value ?: return
        interactions.trySend { current -> body(requireNotNull(current)) }
    }

    fun searchForTag(tag: String, currentTags: List<Tag>) {
        if (searchJob?.isActive == true) searchJob?.cancel()

        searchJob = launch {
            postsRepository.searchExistingPostTag(
                tag = tag,
                currentTags = currentTags,
            ).onSuccess { suggestedTags ->
                _screenState.update { currentState ->
                    currentState.copy(suggestedTags = suggestedTags)
                }
            }
        }
    }

    fun saveLink() {
        launch {
            val params = validateData() ?: return@launch

            _screenState.update { currentState ->
                currentState.copy(isLoading = true)
            }

            addPost(params)
                .onSuccess {
                    _screenState.update { currentState ->
                        currentState.copy(saved = true)
                    }
                    appStateRepository.runDelayedAction(PostSaved(it))
                }
                .onFailure { error ->
                    _screenState.update { currentState ->
                        currentState.copy(isLoading = false)
                    }
                    when (error) {
                        is InvalidUrlException -> {
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

    fun hasPendingChanges(): Boolean = _postState.value.let { it?.index != null && it.index != 0 }

    private fun validateData(): Post? {
        _screenState.update { currentState ->
            currentState.copy(
                invalidUrlError = "",
                invalidTitleError = "",
            )
        }

        val post = _postState.value?.value ?: return null

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
        val isLoading: Boolean = false,
        val suggestedTags: List<String> = emptyList(),
        val invalidUrlError: String = "",
        val invalidTitleError: String = "",
        val saved: Boolean = false,
    )
}
