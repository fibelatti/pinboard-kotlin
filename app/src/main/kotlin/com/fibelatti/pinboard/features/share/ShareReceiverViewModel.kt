package com.fibelatti.pinboard.features.share

import androidx.annotation.StringRes
import com.fibelatti.core.functional.ScreenState
import com.fibelatti.core.functional.emitError
import com.fibelatti.core.functional.emitLoaded
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.functional.getOrThrow
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.EditPostFromShare
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.ExtractUrl
import com.fibelatti.pinboard.features.posts.domain.usecase.GetUrlPreview
import com.fibelatti.pinboard.features.posts.domain.usecase.UrlPreview
import com.fibelatti.pinboard.features.user.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShareReceiverViewModel @Inject constructor(
    private val extractUrl: ExtractUrl,
    private val getUrlPreview: GetUrlPreview,
    private val addPost: AddPost,
    private val postsRepository: PostsRepository,
    private val userRepository: UserRepository,
    private val appStateRepository: AppStateRepository,
) : BaseViewModel() {

    val screenState: StateFlow<ScreenState<SharingResult>> get() = _screenState.asStateFlow()
    private val _screenState = MutableStateFlow<ScreenState<SharingResult>>(ScreenState.Loading.FromEmpty)

    fun saveUrl(url: String, title: String?) {
        launch {
            extractUrl(url).mapCatching { (extractedUrl, highlightedText) ->
                val urlPreview = async {
                    getUrlPreview(
                        GetUrlPreview.Params(
                            url = extractedUrl,
                            title = title,
                            highlightedText = highlightedText,
                        ),
                    )
                }
                val existingPost = async { postsRepository.getPost(url = extractedUrl) }

                urlPreview.await().getOrThrow() to existingPost.await().getOrNull()
            }.onSuccess { (urlPreview, existingPost) ->
                when {
                    existingPost != null -> handleExistingPost(existingPost)
                    userRepository.editAfterSharing is EditAfterSharing.BeforeSaving -> {
                        editBookmark(urlPreview = urlPreview)
                    }

                    else -> addBookmark(urlPreview = urlPreview)
                }
            }.onFailure(_screenState::emitError)
        }
    }

    private suspend fun handleExistingPost(post: Post) {
        if (userRepository.editAfterSharing is EditAfterSharing.SkipEdit) {
            _screenState.emitLoaded(SharingResult.Saved(message = R.string.posts_existing_feedback))
        } else {
            _screenState.emitLoaded(SharingResult.Edit(message = R.string.posts_existing_feedback))
            appStateRepository.runAction(EditPostFromShare(post))
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

        _screenState.emitLoaded(SharingResult.Edit())
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
            ),
        ).onSuccess {
            if (userRepository.editAfterSharing is EditAfterSharing.AfterSaving) {
                _screenState.emitLoaded(SharingResult.Edit(message = R.string.posts_saved_feedback))
                appStateRepository.runAction(EditPostFromShare(it))
            } else {
                _screenState.emitLoaded(SharingResult.Saved())
            }
        }.onFailure(_screenState::emitError)
    }

    sealed class SharingResult {

        @get:StringRes
        abstract val message: Int?

        data class Saved(
            @StringRes override val message: Int? = R.string.posts_saved_feedback,
        ) : SharingResult()

        data class Edit(
            @StringRes override val message: Int? = null,
        ) : SharingResult()
    }
}
