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
import com.fibelatti.pinboard.core.network.MissingAuthTokenException
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
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ShareReceiverViewModel @Inject constructor(
    scope: CoroutineScope,
    appStateRepository: AppStateRepository,
    private val extractUrl: ExtractUrl,
    private val getUrlPreview: GetUrlPreview,
    private val addPost: AddPost,
    private val postsRepository: PostsRepository,
    private val userRepository: UserRepository,
) : BaseViewModel(scope, appStateRepository) {

    val screenState: StateFlow<ScreenState<SharingResult>> get() = _screenState.asStateFlow()
    private val _screenState = MutableStateFlow<ScreenState<SharingResult>>(ScreenState.Loading.FromEmpty)

    fun saveUrl(url: String, title: String?, skipEdit: Boolean = false) {
        scope.launch {
            if (!userRepository.hasAuthToken()) {
                _screenState.emitError(MissingAuthTokenException())
                return@launch
            }

            extractUrl(url).mapCatching { (extractedUrl, highlightedText) ->
                val preview = getUrlPreview(
                    GetUrlPreview.Params(
                        url = extractedUrl,
                        title = title,
                        highlightedText = highlightedText,
                    ),
                ).getOrThrow()

                val existingPost = postsRepository.getPost(id = "", url = preview.url).getOrNull()

                when {
                    existingPost != null && skipEdit -> {
                        _screenState.emitLoaded(SharingResult.Saved(message = R.string.posts_existing_feedback))
                    }

                    existingPost != null -> {
                        _screenState.emitLoaded(SharingResult.Edit(message = R.string.posts_existing_feedback))
                        runAction(EditPostFromShare(existingPost))
                    }

                    skipEdit || userRepository.editAfterSharing is EditAfterSharing.AfterSaving -> {
                        addBookmark(urlPreview = preview, skipEdit = skipEdit)
                    }

                    else -> editBookmark(urlPreview = preview)
                }
            }.onFailure(_screenState::emitError)
        }
    }

    private suspend fun editBookmark(urlPreview: UrlPreview) {
        val (finalUrl: String, title: String, description: String?) = urlPreview
        val newPost = Post(
            url = finalUrl,
            title = title,
            description = description
                ?.let { if (userRepository.useBlockquote) "<blockquote>$it</blockquote>" else it }
                .orEmpty(),
            private = userRepository.defaultPrivate ?: false,
            readLater = userRepository.defaultReadLater ?: false,
            tags = userRepository.defaultTags,
        )

        _screenState.emitLoaded(SharingResult.Edit())
        runAction(EditPostFromShare(newPost))
    }

    private suspend fun addBookmark(urlPreview: UrlPreview, skipEdit: Boolean) {
        val (finalUrl: String, title: String, description: String?) = urlPreview

        addPost(
            params = Post(
                url = finalUrl,
                title = title,
                description = description
                    ?.let { if (userRepository.useBlockquote) "<blockquote>$it</blockquote>" else it }
                    .orEmpty(),
                private = userRepository.defaultPrivate,
                readLater = userRepository.defaultReadLater,
                tags = userRepository.defaultTags,
            ),
        ).onSuccess {
            if (skipEdit) {
                _screenState.emitLoaded(SharingResult.Saved())
            } else {
                _screenState.emitLoaded(SharingResult.Edit(message = R.string.posts_saved_feedback))
                runAction(EditPostFromShare(it))
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
