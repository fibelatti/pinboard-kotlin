package com.fibelatti.pinboard.features.appstate

import androidx.lifecycle.viewModelScope
import com.fibelatti.bookmarking.core.AppMode
import com.fibelatti.bookmarking.core.AppModeProvider
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.core.network.UnauthorizedPluginProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class AppStateViewModel(
    private val appStateRepository: AppStateRepository,
    appModeProvider: AppModeProvider,
    unauthorizedPluginProvider: UnauthorizedPluginProvider,
) : BaseViewModel() {

    val appMode: StateFlow<AppMode> = appModeProvider.appMode

    val content: StateFlow<Content> = appStateRepository.content

    val postListContent: Flow<PostListContent> get() = filteredContent()
    val postDetailContent: Flow<PostDetailContent> get() = filteredContent()
    val addPostContent: Flow<AddPostContent> get() = filteredContent()
    val editPostContent: Flow<EditPostContent> get() = filteredContent()
    val searchContent: Flow<SearchContent> get() = filteredContent()
    val tagListContent: Flow<TagListContent> get() = filteredContent()
    val noteListContent: Flow<NoteListContent> get() = filteredContent()
    val noteDetailContent: Flow<NoteDetailContent> get() = filteredContent()
    val popularPostsContent: Flow<PopularPostsContent> get() = filteredContent()
    val popularPostDetailContent: Flow<PopularPostDetailContent> get() = filteredContent()
    val userPreferencesContent: Flow<UserPreferencesContent> get() = filteredContent()

    init {
        unauthorizedPluginProvider.unauthorized
            .onEach { appStateRepository.runAction(UserUnauthorized) }
            .launchIn(viewModelScope)
    }

    fun reset() {
        launch(Dispatchers.Main.immediate) {
            withContext(NonCancellable) {
                appStateRepository.reset()
            }
        }
    }

    fun runAction(action: Action) {
        launch { appStateRepository.runAction(action) }
    }

    private inline fun <reified T> filteredContent() = content.mapNotNull { it as? T }
}
