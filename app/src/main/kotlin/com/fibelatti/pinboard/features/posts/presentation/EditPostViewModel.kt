package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.android.ResourceProvider
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.core.di.AppDispatchers
import com.fibelatti.pinboard.core.di.Scope
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.PostSaved
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.InvalidUrlException
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditPostViewModel @Inject constructor(
    private val appStateRepository: AppStateRepository,
    private val postsRepository: PostsRepository,
    private val addPost: AddPost,
    private val resourceProvider: ResourceProvider,
    @Scope(AppDispatchers.DEFAULT) scope: CoroutineScope,
    sharingStarted: SharingStarted,
) : BaseViewModel() {

    val loading: Flow<Boolean> get() = _loading.filterNotNull()
    private val _loading = MutableStateFlow<Boolean?>(null)
    val suggestedTags: Flow<List<String>> get() = _suggestedTags.filterNotNull()
    private val _suggestedTags = MutableStateFlow<List<String>?>(null)
    val invalidUrlError: StateFlow<String> get() = _invalidUrlError
    private val _invalidUrlError = MutableStateFlow("")
    val invalidUrlTitleError: StateFlow<String> get() = _invalidUrlTitleError
    private val _invalidUrlTitleError = MutableStateFlow("")
    val saved: Flow<Unit> get() = _saved
    private val _saved = MutableSharedFlow<Unit>()

    private var searchJob: Job? = null

    // Source of all changes to the screen state, takes null to enable it being initialized with initializePost
    private val interactions: MutableSharedFlow<(Post?) -> Post> = MutableSharedFlow()
    private val _postState: StateFlow<IndexedValue<Post>?> = interactions
        .scan(initial = null) { post: Post?, interaction -> interaction(post) }
        .filterNotNull() // interactions never return null but the scan return type is inferred by the initial value
        .withIndex() // add an index to easily figure out if the value has changed
        .stateIn(
            scope = scope,
            started = sharingStarted,
            initialValue = null,
        )
    val postState: Flow<Post> get() = _postState.filterNotNull().map { it.value }

    fun initializePost(post: Post) {
        launch {
            if (_postState.value == null) {
                interactions.emit { post }
            }
        }
    }

    fun updatePost(body: (Post) -> Post) {
        launch {
            interactions.emit { current -> body(requireNotNull(current)) }
        }
    }

    fun searchForTag(tag: String, currentTags: List<Tag>) {
        if (searchJob?.isActive == true) searchJob?.cancel()

        searchJob = launch {
            postsRepository.searchExistingPostTag(
                tag = tag,
                currentTags = currentTags,
            ).onSuccess { _suggestedTags.value = it }
        }
    }

    fun saveLink() {
        launch {
            val params = validateData() ?: return@launch

            _loading.value = true

            addPost(params)
                .onSuccess {
                    _saved.emit(Unit)
                    appStateRepository.runAction(PostSaved(it))
                }
                .onFailure { error ->
                    _loading.value = false
                    when (error) {
                        is InvalidUrlException -> {
                            _invalidUrlError.value = resourceProvider.getString(
                                R.string.validation_error_invalid_url
                            )
                        }
                        else -> handleError(error)
                    }
                }
        }
    }

    fun hasPendingChanges(): Boolean = _postState.value.let { it?.index != null && it.index != 0 }

    private fun validateData(): AddPost.Params? {
        _invalidUrlError.value = ""
        _invalidUrlTitleError.value = ""

        val post = _postState.value?.value ?: return null

        when {
            post.url.isBlank() -> {
                _invalidUrlError.value = resourceProvider.getString(R.string.validation_error_empty_url)
            }
            post.title.isBlank() -> {
                _invalidUrlTitleError.value = resourceProvider.getString(R.string.validation_error_empty_title)
            }
            else -> return AddPost.Params(
                url = post.url,
                title = post.title,
                description = post.description,
                private = post.private,
                readLater = post.readLater,
                tags = post.tags,
                hash = post.hash,
            )
        }

        return null
    }
}
