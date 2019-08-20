package com.fibelatti.pinboard.features.share

import com.fibelatti.core.archcomponents.BaseViewModel
import com.fibelatti.core.archcomponents.LiveEvent
import com.fibelatti.core.archcomponents.MutableLiveEvent
import com.fibelatti.core.archcomponents.postEvent
import com.fibelatti.core.functional.map
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.ExtractUrl
import com.fibelatti.pinboard.features.posts.domain.usecase.ParseUrl
import com.fibelatti.pinboard.features.user.domain.UserRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShareReceiverViewModel @Inject constructor(
    private val extractUrl: ExtractUrl,
    private val parseUrl: ParseUrl,
    private val addPost: AddPost,
    private val userRepository: UserRepository,
    private val resourceProvider: ResourceProvider
) : BaseViewModel() {

    val saved: LiveEvent<String> get() = _saved
    private val _saved = MutableLiveEvent<String>()
    val failed: LiveEvent<String> get() = _failed
    private val _failed = MutableLiveEvent<String>()

    fun saveUrl(url: String) {
        launch {
            extractUrl(url)
                .map { extractedUrl -> parseUrl(extractedUrl) }
                .map { (finalUrl, title, description) ->
                    addPost(
                        AddPost.Params(
                            url = finalUrl,
                            title = title,
                            description = description,
                            private = userRepository.getDefaultPrivate(),
                            readLater = userRepository.getDefaultReadLater()
                        )
                    )
                }
                .onSuccess {
                    _saved.postEvent(resourceProvider.getString(R.string.posts_saved_feedback))
                }
                .onFailure {
                    _failed.postEvent(resourceProvider.getString(R.string.generic_msg_error))
                }
        }
    }
}
