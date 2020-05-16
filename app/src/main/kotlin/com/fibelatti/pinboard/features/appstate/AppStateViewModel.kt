package com.fibelatti.pinboard.features.appstate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.fibelatti.core.archcomponents.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppStateViewModel @Inject constructor(
    private val appStateRepository: AppStateRepository
) : BaseViewModel() {

    val content: LiveData<Content>
        get() = appStateRepository.getContent()

    val postListContent: LiveData<PostListContent> get() = mediatorLiveDataForContentType()
    val postDetailContent: LiveData<PostDetailContent> get() = mediatorLiveDataForContentType()
    val addPostContent: LiveData<AddPostContent> get() = mediatorLiveDataForContentType()
    val editPostContent: LiveData<EditPostContent> get() = mediatorLiveDataForContentType()
    val searchContent: LiveData<SearchContent> get() = mediatorLiveDataForContentType()
    val tagListContent: LiveData<TagListContent> get() = mediatorLiveDataForContentType()
    val noteListContent: LiveData<NoteListContent> get() = mediatorLiveDataForContentType()
    val noteDetailContent: LiveData<NoteDetailContent> get() = mediatorLiveDataForContentType()
    val popularPostsContent: LiveData<PopularPostsContent> get() = mediatorLiveDataForContentType()
    val popularPostDetailContent: LiveData<PopularPostDetailContent> get() = mediatorLiveDataForContentType()
    val userPreferencesContent: LiveData<UserPreferencesContent> get() = mediatorLiveDataForContentType()

    fun reset() {
        appStateRepository.reset()
    }

    fun runAction(action: Action) {
        launch { appStateRepository.runAction(action) }
    }

    private inline fun <reified T : Content> mediatorLiveDataForContentType(): MediatorLiveData<T> =
        MediatorLiveData<T>().apply {
            addSource(appStateRepository.getContent()) { if (it is T) value = it }
        }
}
