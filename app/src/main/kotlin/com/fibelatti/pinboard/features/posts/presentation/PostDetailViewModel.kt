package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.core.di.AppDispatchers
import com.fibelatti.pinboard.core.di.Scope
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.OfflineCopySaved
import com.fibelatti.pinboard.features.appstate.PostDeleted
import com.fibelatti.pinboard.features.appstate.PostDetailContent
import com.fibelatti.pinboard.features.appstate.PostSaved
import com.fibelatti.pinboard.features.offline.domain.OfflineCopyRepository
import com.fibelatti.pinboard.features.offline.domain.SaveOfflineCopy
import com.fibelatti.pinboard.features.offline.domain.model.OfflineCopy
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.ArchivePost
import com.fibelatti.pinboard.features.posts.domain.usecase.DeletePost
import com.fibelatti.pinboard.features.posts.domain.usecase.UnarchivePost
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    @Scope(AppDispatchers.DEFAULT) dispatcher: CoroutineDispatcher,
    appStateRepository: AppStateRepository,
    private val deletePost: DeletePost,
    private val addPost: AddPost,
    private val archivePost: ArchivePost,
    private val unarchivePost: UnarchivePost,
    private val saveOfflineCopy: SaveOfflineCopy,
    private val offlineCopyRepository: OfflineCopyRepository,
) : BaseViewModel(dispatcher, appStateRepository) {

    private val _screenState = MutableStateFlow(ScreenState())
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    /**
     * The bookmarks whose captures are currently running.
     *
     * A set rather than a single id because captures can be started for several bookmarks in a row
     * from the list, and they are slow enough to overlap.
     *
     * Deliberately not part of [ScreenState]: a capture outlives the content changes it causes, and
     * [ScreenState] is rebuilt from scratch on every one of them. Keeping it here lets the guard
     * hold while still reporting progress against the bookmark actually being shown.
     */
    private val savingOfflineCopyFor: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())

    init {
        filteredContent<PostDetailContent>()
            .onEach { content ->
                _screenState.update {
                    ScreenState(
                        offlineCopy = content.offlineCopy,
                        offlineCopyFile = content.offlineCopy?.let(offlineCopyRepository::fileFor),
                        // Scoped to this bookmark: moving to another one while a capture runs must
                        // not make that one look like it is being saved.
                        isSavingOfflineCopy = content.post.id in savingOfflineCopyFor.value,
                    )
                }
            }
            .launchIn(scope)
    }

    /**
     * Captures [post] for offline reading.
     *
     * Does nothing while a capture of the same bookmark is already running: the work is slow enough
     * for a second tap to land, and two captures of one bookmark would race over the same file and
     * the same row. Captures of different bookmarks are independent and run concurrently.
     */
    fun saveOfflineCopy(post: Post) {
        val alreadySaving: Set<String> = savingOfflineCopyFor.getAndUpdate { inFlight -> inFlight + post.id }
        if (post.id in alreadySaving) return

        _screenState.update { currentState -> currentState.copy(isSavingOfflineCopy = true) }

        scope.launch {
            // A capture fetches the page and every image it keeps, so leaving the bookmark mid-way
            // is expected. Finishing anyway costs nothing and spares the user a second wait.
            withContext(NonCancellable) {
                val result: Result<OfflineCopy> = saveOfflineCopy(
                    params = SaveOfflineCopy.Params(post = post, appMode = appState.value.appMode),
                )

                val stillSaving: Set<String> = savingOfflineCopyFor.updateAndGet { inFlight ->
                    inFlight - post.id
                }

                result
                    .onSuccess { offlineCopy ->
                        _screenState.update { currentState ->
                            currentState.copy(
                                isSavingOfflineCopy = stillSaving.includesShownBookmark(),
                                offlineCopySaved = Result.success(value = true),
                            )
                        }
                        runAction(OfflineCopySaved(offlineCopy))
                    }
                    .onFailure { throwable ->
                        _screenState.update { currentState ->
                            currentState.copy(
                                isSavingOfflineCopy = stillSaving.includesShownBookmark(),
                                offlineCopySaved = Result.failure(throwable),
                            )
                        }
                    }
            }
        }
    }

    /**
     * Whether this set of in-flight captures covers what the user is looking at, so that one capture
     * finishing does not clear the progress reported for another that is still running.
     */
    private fun Set<String>.includesShownBookmark(): Boolean {
        val shownBookmarkId: String? = (appState.value.content as? PostDetailContent)?.post?.id

        // On the bookmark list there is no single bookmark on screen, so any running capture counts:
        // that is where the feedback for one started from the quick actions has to appear.
        return shownBookmarkId?.let { it in this } ?: isNotEmpty()
    }

    fun toggleViewingOfflineCopy() {
        _screenState.update { currentState ->
            currentState.copy(viewingOfflineCopy = !currentState.viewingOfflineCopy)
        }
    }

    fun deletePost(post: Post) {
        scope.launch {
            _screenState.update { currentState ->
                currentState.copy(isLoading = true)
            }

            deletePost(params = post)
                .onSuccess {
                    offlineCopyRepository.delete(appMode = appState.value.appMode, bookmarkId = post.id)

                    _screenState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            deleted = Result.success(value = true),
                            offlineCopy = null,
                            offlineCopyFile = null,
                        )
                    }
                    runDelayedAction(PostDeleted)
                }
                .onFailure {
                    _screenState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            deleted = Result.failure(it),
                        )
                    }
                }
        }
    }

    fun toggleReadLater(post: Post) {
        scope.launch {
            editPost(newPost = post.copy(readLater = !(post.readLater ?: false)))
        }
    }

    fun addTags(post: Post, tags: List<Tag>) {
        scope.launch {
            if (post.tags?.containsAll(tags) == true) return@launch

            editPost(newPost = post.copy(tags = post.tags.orEmpty().plus(tags).distinct()))
        }
    }

    fun toggleArchived(post: Post) {
        scope.launch {
            _screenState.update { currentState -> currentState.copy(isLoading = true) }

            val archived: Boolean = post.isArchived == true
            val result: Result<Post> = if (archived) unarchivePost(params = post) else archivePost(params = post)
            result
                .onSuccess { updatedPost ->
                    _screenState.update { currentState ->
                        currentState.copy(isLoading = false, updated = Result.success(value = true))
                    }
                    runDelayedAction(PostSaved(updatedPost))
                }
                .onFailure { throwable ->
                    _screenState.update { currentState ->
                        currentState.copy(isLoading = false, updated = Result.failure(throwable))
                    }
                }
        }
    }

    private suspend fun editPost(newPost: Post) {
        _screenState.update { currentState -> currentState.copy(isLoading = true) }
        addPost(params = newPost)
            .onSuccess { addedPost ->
                _screenState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        updated = Result.success(value = true),
                    )
                }
                runDelayedAction(PostSaved(addedPost))
            }
            .onFailure { throwable ->
                _screenState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        updated = Result.failure(throwable),
                    )
                }
            }
    }

    fun userNotified() {
        _screenState.update { currentState ->
            currentState.copy(
                deleted = Result.success(value = false),
                updated = Result.success(value = false),
                offlineCopySaved = Result.success(value = false),
            )
        }
    }

    data class ScreenState(
        val isLoading: Boolean = false,
        val deleted: Result<Boolean> = Result.success(value = false),
        val updated: Result<Boolean> = Result.success(value = false),
        val offlineCopy: OfflineCopy? = null,
        val offlineCopyFile: File? = null,
        val isSavingOfflineCopy: Boolean = false,
        val offlineCopySaved: Result<Boolean> = Result.success(value = false),
        /**
         * Set when the user asks to see the offline copy of a bookmark that could also be loaded
         * live. When there is no connection the copy is shown regardless.
         */
        val viewingOfflineCopy: Boolean = false,
    )
}
