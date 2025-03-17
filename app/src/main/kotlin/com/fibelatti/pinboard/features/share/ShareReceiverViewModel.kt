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
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.AppModeProvider
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
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
    private val appModeProvider: AppModeProvider,
) : BaseViewModel(scope, appStateRepository) {

    val screenState: StateFlow<ScreenState<SharingResult>> get() = _screenState.asStateFlow()
    private val _screenState = MutableStateFlow<ScreenState<SharingResult>>(ScreenState.Loading.FromEmpty)

    private val saveRequest = MutableStateFlow<SaveRequest?>(null)
    private val selectedService = MutableStateFlow<AppMode?>(null)

    init {
        combine(saveRequest.filterNotNull(), selectedService.filterNotNull(), ::processRequest)
            .take(count = 1)
            .launchIn(scope)
    }

    fun saveUrl(url: String, title: String?, skipEdit: Boolean = false) {
        scope.launch {
            saveRequest.update {
                SaveRequest(
                    url = url,
                    title = title,
                    skipEdit = skipEdit,
                )
            }

            val connectedServices = userRepository.userCredentials.first().getConnectedServices()

            when (connectedServices.size) {
                0 -> _screenState.emitError(MissingAuthTokenException())
                1 -> selectedService.update { connectedServices.first() }
                else -> _screenState.emitLoaded(SharingResult.ChooseService())
            }
        }
    }

    fun selectService(appMode: AppMode) {
        selectedService.update { appMode }
    }

    private suspend fun processRequest(request: SaveRequest, service: AppMode) {
        appModeProvider.setSelection(appMode = service)

        extractUrl(inputUrl = request.url)
            .mapCatching { (extractedUrl, highlightedText) ->
                val preview = getUrlPreview(
                    GetUrlPreview.Params(
                        url = extractedUrl,
                        title = request.title,
                        highlightedText = highlightedText,
                    ),
                ).getOrThrow()

                val existingPost = postsRepository.getPost(id = "", url = preview.url).getOrNull()

                when {
                    existingPost != null && request.skipEdit -> {
                        _screenState.emitLoaded(SharingResult.Saved(message = R.string.posts_existing_feedback))
                    }

                    existingPost != null -> {
                        _screenState.emitLoaded(SharingResult.Edit(message = R.string.posts_existing_feedback))
                        runAction(EditPostFromShare(existingPost))
                    }

                    request.skipEdit || userRepository.editAfterSharing is EditAfterSharing.AfterSaving -> {
                        addBookmark(urlPreview = preview, skipEdit = request.skipEdit)
                    }

                    else -> editBookmark(urlPreview = preview)
                }
            }
            .onFailure(_screenState::emitError)
    }

    private fun editBookmark(urlPreview: UrlPreview) {
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

    private data class SaveRequest(
        val url: String,
        val title: String?,
        val skipEdit: Boolean = false,
    )

    sealed class SharingResult {

        @get:StringRes
        abstract val message: Int?

        data class ChooseService(
            @StringRes override val message: Int? = null,
        ) : SharingResult()

        data class Saved(
            @StringRes override val message: Int? = R.string.posts_saved_feedback,
        ) : SharingResult()

        data class Edit(
            @StringRes override val message: Int? = null,
        ) : SharingResult()
    }
}
