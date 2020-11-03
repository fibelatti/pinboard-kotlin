package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.PostDeleted
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.DeletePost
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class PostDetailViewModel @Inject constructor(
    private val appStateRepository: AppStateRepository,
    private val deletePost: DeletePost,
) : BaseViewModel() {

    val loading: Flow<Boolean> get() = _loading.filterNotNull()
    private val _loading = MutableStateFlow<Boolean?>(null)

    val deleted: Flow<Unit> get() = _deleted.filterNotNull()
    private val _deleted = MutableStateFlow<Unit?>(null)

    val deleteError: Flow<Throwable> get() = _deleteError.filterNotNull()
    private val _deleteError = MutableStateFlow<Throwable?>(null)

    fun deletePost(post: Post) {
        launch {
            _loading.value = true
            deletePost(post.url)
                .onSuccess {
                    _deleted.value = Unit
                    appStateRepository.runAction(PostDeleted)
                }
                .onFailure {
                    _loading.value = false
                    _deleteError.value = it
                }
        }
    }
}
