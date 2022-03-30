package com.fibelatti.pinboard.features.share

import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.functional.map
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.EditPostFromShare
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.ExtractUrl
import com.fibelatti.pinboard.features.posts.domain.usecase.GetUrlPreview
import com.fibelatti.pinboard.features.posts.domain.usecase.UrlPreview
import com.fibelatti.pinboard.features.user.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShareReceiverViewModel @Inject constructor(
    private val extractUrl: ExtractUrl,
    private val getUrlPreview: GetUrlPreview,
    private val addPost: AddPost,
    private val userRepository: UserRepository,
    private val appStateRepository: AppStateRepository,
    private val resourceProvider: ResourceProvider,
) : BaseViewModel() {

    val saved: Flow<String> get() = _saved.filterNotNull()
    private val _saved = MutableStateFlow<String?>(null)
    val edit: Flow<String> get() = _edit.filterNotNull()
    private val _edit = MutableStateFlow<String?>(null)
    val failed: Flow<Throwable> get() = _failed.filterNotNull()
    private val _failed = MutableStateFlow<Throwable?>(null)

    fun saveUrl(url: String) {
        launch {
            val urlPreview = extractUrl(url)
                .map { extractedUrl -> getUrlPreview(extractedUrl) }
                .onFailure { _failed.value = it }
                .getOrNull() ?: return@launch

            if (userRepository.editAfterSharing is EditAfterSharing.BeforeSaving) {
                editBookmark(urlPreview = urlPreview)
            } else {
                addBookmark(urlPreview = urlPreview)
            }
        }
    }

    private suspend fun editBookmark(urlPreview: UrlPreview) {
        val (finalUrl: String, title: String, description: String?) = urlPreview
        val newPost = Post(
            url = finalUrl,
            title = title,
            description = description ?: "",
            private = userRepository.defaultPrivate ?: false,
            readLater = userRepository.defaultReadLater ?: false,
            tags = userRepository.defaultTags,
        )

        _edit.value = ""
        appStateRepository.runAction(EditPostFromShare(newPost))
    }

    private suspend fun addBookmark(urlPreview: UrlPreview) {
        val (finalUrl: String, title: String, description: String?) = urlPreview

        addPost(
            AddPost.Params(
                url = finalUrl,
                title = title,
                description = description,
                private = userRepository.defaultPrivate,
                readLater = userRepository.defaultReadLater,
                tags = userRepository.defaultTags,
                replace = false,
            )
        ).onSuccess {
            if (userRepository.editAfterSharing is EditAfterSharing.AfterSaving) {
                _edit.value = resourceProvider.getString(R.string.posts_saved_feedback)
                appStateRepository.runAction(EditPostFromShare(it))
            } else {
                _saved.value = resourceProvider.getString(R.string.posts_saved_feedback)
            }
        }.onFailure { _failed.value = it }
    }
}
