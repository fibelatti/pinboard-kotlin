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
import com.fibelatti.pinboard.features.tags.domain.model.Tag
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
    val invalidUrlTitleError: LiveData<String> get() = _invalidUrlTitleError
    private val _invalidUrlTitleError = MutableLiveData<String>()
    val saved: LiveEvent<Unit> get() = _saved
    private val _saved = MutableLiveEvent<Unit>()

    fun saveLink(
        url: String,
        title: String,
        description: String,
        private: Boolean,
        readLater: Boolean,
        tags: List<Tag>
    ) {
        launch {
            validateData(url, title, description, private, readLater, tags) { params ->
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
        title: String,
        description: String,
        private: Boolean,
        readLater: Boolean,
        tags: List<Tag>,
        ifValid: (AddPost.Params) -> Unit
    ) {
        when {
            url.isBlank() -> {
                _invalidUrlError.postValue(resourceProvider.getString(R.string.validation_error_empty_url))
            }
            title.isBlank() -> {
                _invalidUrlTitleError.postValue(resourceProvider.getString(R.string.validation_error_empty_title))
            }
            else -> {
                _invalidUrlError.postValue(String.empty())
                _invalidUrlTitleError.postValue(String.empty())

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
