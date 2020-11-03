package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.extension.empty
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.PostSaved
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.GetSuggestedTags
import com.fibelatti.pinboard.features.posts.domain.usecase.InvalidUrlException
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class EditPostViewModel @Inject constructor(
    private val appStateRepository: AppStateRepository,
    private val getSuggestedTags: GetSuggestedTags,
    private val addPost: AddPost,
    private val resourceProvider: ResourceProvider,
) : BaseViewModel() {

    val loading: Flow<Boolean> get() = _loading.filterNotNull()
    private val _loading = MutableStateFlow<Boolean?>(null)
    val suggestedTags: Flow<List<String>> get() = _suggestedTags.filterNotNull()
    private val _suggestedTags = MutableStateFlow<List<String>?>(null)
    val invalidUrlError: StateFlow<String> get() = _invalidUrlError
    private val _invalidUrlError = MutableStateFlow("")
    val invalidUrlTitleError: StateFlow<String> get() = _invalidUrlTitleError
    private val _invalidUrlTitleError = MutableStateFlow("")
    val saved: Flow<Unit> get() = _saved.filterNotNull()
    private val _saved = MutableStateFlow<Unit?>(null)

    fun searchForTag(tag: String, currentTags: List<Tag>) {
        launch {
            getSuggestedTags(GetSuggestedTags.Params(tag, currentTags))
                .onSuccess { _suggestedTags.value = it }
        }
    }

    fun saveLink(
        url: String,
        title: String,
        description: String,
        private: Boolean,
        readLater: Boolean,
        tags: List<Tag>,
    ) {
        launch {
            validateData(url, title, description, private, readLater, tags) { params ->
                _loading.value = true
                addPost(params)
                    .onSuccess {
                        _saved.value = Unit
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
    }

    private inline fun validateData(
        url: String,
        title: String,
        description: String,
        private: Boolean,
        readLater: Boolean,
        tags: List<Tag>,
        ifValid: (AddPost.Params) -> Unit,
    ) {
        _invalidUrlError.value = String.empty()
        _invalidUrlTitleError.value = String.empty()

        when {
            url.isBlank() -> {
                _invalidUrlError.value = resourceProvider.getString(R.string.validation_error_empty_url)
            }
            title.isBlank() -> {
                _invalidUrlTitleError.value = resourceProvider.getString(R.string.validation_error_empty_title)
            }
            else -> {
                ifValid(
                    AddPost.Params(
                        url = url,
                        title = title,
                        description = description,
                        private = private,
                        readLater = readLater,
                        tags = tags
                    )
                )
            }
        }
    }
}
