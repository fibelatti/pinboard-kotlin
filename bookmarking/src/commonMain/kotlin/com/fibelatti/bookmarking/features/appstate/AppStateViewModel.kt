package com.fibelatti.bookmarking.features.appstate

import androidx.lifecycle.viewModelScope
import com.fibelatti.bookmarking.core.AppMode
import com.fibelatti.bookmarking.core.AppModeProvider
import com.fibelatti.bookmarking.core.base.BaseViewModel
import com.fibelatti.bookmarking.core.network.UnauthorizedPluginProvider
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
public class AppStateViewModel(
    private val appStateRepository: AppStateRepository,
    appModeProvider: AppModeProvider,
    unauthorizedPluginProvider: UnauthorizedPluginProvider,
) : BaseViewModel() {

    public val appMode: StateFlow<AppMode> = appModeProvider.appMode

    public val content: StateFlow<Content> = appStateRepository.content

    public val postListContent: Flow<PostListContent> get() = filteredContent()
    public val postDetailContent: Flow<PostDetailContent> get() = filteredContent()
    public val addPostContent: Flow<AddPostContent> get() = filteredContent()
    public val editPostContent: Flow<EditPostContent> get() = filteredContent()
    public val searchContent: Flow<SearchContent> get() = filteredContent()
    public val tagListContent: Flow<TagListContent> get() = filteredContent()
    public val noteListContent: Flow<NoteListContent> get() = filteredContent()
    public val noteDetailContent: Flow<NoteDetailContent> get() = filteredContent()
    public val popularPostsContent: Flow<PopularPostsContent> get() = filteredContent()
    public val popularPostDetailContent: Flow<PopularPostDetailContent> get() = filteredContent()
    public val userPreferencesContent: Flow<UserPreferencesContent> get() = filteredContent()

    init {
        unauthorizedPluginProvider.unauthorized
            .onEach { appStateRepository.runAction(UserUnauthorized) }
            .launchIn(viewModelScope)
    }

    public fun reset() {
        launch(Dispatchers.Main.immediate) {
            withContext(NonCancellable) {
                appStateRepository.reset()
            }
        }
    }

    public fun runAction(action: Action) {
        launch { appStateRepository.runAction(action) }
    }

    private inline fun <reified T> filteredContent() = content.mapNotNull { it as? T }
}
