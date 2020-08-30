package com.fibelatti.pinboard.features.share

import com.fibelatti.core.archcomponents.BaseViewModel
import com.fibelatti.core.archcomponents.LiveEvent
import com.fibelatti.core.archcomponents.MutableLiveEvent
import com.fibelatti.core.archcomponents.postEvent
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.functional.map
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.EditPostFromShare
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.ExtractUrl
import com.fibelatti.pinboard.features.posts.domain.usecase.ParseUrl
import com.fibelatti.pinboard.features.posts.domain.usecase.RichUrl
import com.fibelatti.pinboard.features.user.domain.UserRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShareReceiverViewModel @Inject constructor(
    private val extractUrl: ExtractUrl,
    private val parseUrl: ParseUrl,
    private val addPost: AddPost,
    private val userRepository: UserRepository,
    private val appStateRepository: AppStateRepository,
    private val resourceProvider: ResourceProvider
) : BaseViewModel() {

    val saved: LiveEvent<String> get() = _saved
    private val _saved = MutableLiveEvent<String>()
    val edit: LiveEvent<String> get() = _edit
    private val _edit = MutableLiveEvent<String>()
    val failed: LiveEvent<Throwable> get() = _failed
    private val _failed = MutableLiveEvent<Throwable>()

    fun saveUrl(url: String) {
        launch {
            val richUrl = extractUrl(url)
                .map { extractedUrl -> parseUrl(extractedUrl) }
                .onFailure { _failed.postEvent(it) }
                .getOrNull() ?: return@launch

            if (userRepository.getEditAfterSharing() is EditAfterSharing.BeforeSaving) {
                editBookmark(richUrl = richUrl)
            } else {
                addBookmark(richUrl = richUrl)
            }
        }
    }

    private suspend fun editBookmark(richUrl: RichUrl) {
        val (finalUrl: String, title: String, description: String?) = richUrl
        val newPost = Post(
            url = finalUrl,
            title = title,
            description = description ?: "",
            private = userRepository.getDefaultPrivate() ?: false,
            readLater = userRepository.getDefaultReadLater() ?: false,
        )

        _edit.postEvent("")
        appStateRepository.runAction(EditPostFromShare(newPost))
    }

    private suspend fun addBookmark(richUrl: RichUrl) {
        val (finalUrl: String, title: String, description: String?) = richUrl

        addPost(
            AddPost.Params(
                url = finalUrl,
                title = title,
                description = description,
                private = userRepository.getDefaultPrivate(),
                readLater = userRepository.getDefaultReadLater(),
                replace = false
            )
        ).onSuccess {
            if (userRepository.getEditAfterSharing() is EditAfterSharing.AfterSaving) {
                _edit.postEvent(resourceProvider.getString(R.string.posts_saved_feedback))
                appStateRepository.runAction(EditPostFromShare(it))
            } else {
                _saved.postEvent(resourceProvider.getString(R.string.posts_saved_feedback))
            }
        }.onFailure { _failed.postEvent(it) }
    }
}
