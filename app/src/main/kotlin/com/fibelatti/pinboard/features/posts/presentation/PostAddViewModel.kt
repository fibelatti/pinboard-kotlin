package com.fibelatti.pinboard.features.posts.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fibelatti.core.archcomponents.BaseViewModel
import com.fibelatti.core.archcomponents.LiveEvent
import com.fibelatti.core.archcomponents.MutableLiveEvent
import com.fibelatti.core.archcomponents.postEvent
import com.fibelatti.core.extension.empty
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.GetSuggestedTagsForUrl
import com.fibelatti.pinboard.features.posts.domain.usecase.InvalidUrlException
import kotlinx.coroutines.launch
import javax.inject.Inject

class PostAddViewModel @Inject constructor(
    private val addPost: AddPost,
    private val suggestedTagsForUrl: GetSuggestedTagsForUrl,
    private val resourceProvider: ResourceProvider
) : BaseViewModel() {

    val loading: LiveEvent<Boolean> get() = _loading
    private val _loading = MutableLiveEvent<Boolean>()
    val post: LiveData<Post> get() = _post
    private val _post = MutableLiveData<Post>()
    val invalidUrlError: LiveData<String> get() = _invalidUrlError
    private val _invalidUrlError = MutableLiveData<String>()
    val invalidDescriptionError: LiveData<String> get() = _invalidDescriptionError
    private val _invalidDescriptionError = MutableLiveData<String>()
    val saved: LiveEvent<Unit> get() = _saved
    private val _saved = MutableLiveEvent<Unit>()

    fun saveLink(
        url: String,
        description: String,
        private: Boolean,
        readLater: Boolean,
        tags: List<String>
    ) {
        launch {
            validateData(url, description, private, readLater, tags) { params ->
                addPost(params)
                    .onSuccess { _saved.postEvent(Unit) }
                    .onFailure {
                        when (it) {
                            is InvalidUrlException -> {
                                _invalidUrlError.postValue(resourceProvider.getString(R.string.validation_error_invalid_url))
                            }
                            else -> {
                                handleError(Throwable(resourceProvider.getString(R.string.generic_msg_error)))
                            }
                        }
                    }
            }
        }
    }

    private inline fun validateData(
        url: String,
        description: String,
        private: Boolean,
        readLater: Boolean,
        tags: List<String>,
        ifValid: (AddPost.Params) -> Unit
    ) {
        when {
            url.isBlank() -> {
                _invalidUrlError.postValue(resourceProvider.getString(R.string.validation_error_empty_url))
            }
            description.isBlank() -> {
                _invalidDescriptionError.postValue(resourceProvider.getString(R.string.validation_error_empty_description))
            }
            else -> {
                _invalidUrlError.postValue(String.empty())
                _invalidDescriptionError.postValue(String.empty())

                ifValid(AddPost.Params(
                    url = url,
                    description = description,
                    private = private,
                    readLater = readLater,
                    tags = tags
                ))
            }
        }
    }
}
