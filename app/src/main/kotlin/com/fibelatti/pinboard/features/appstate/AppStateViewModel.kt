package com.fibelatti.pinboard.features.appstate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.fibelatti.core.archcomponents.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppStateViewModel @Inject constructor(
    private val appStateRepository: AppStateRepository
) : BaseViewModel() {

    val content: LiveData<Content> = appStateRepository.getContent()

    val postListContent: LiveData<PostListContent> = mediatorLiveDataForContentType()
    val postDetailContent: LiveData<PostDetailContent> = mediatorLiveDataForContentType()
    val addPostContent: LiveData<AddPostContent> = mediatorLiveDataForContentType()
    val editPostContent: LiveData<EditPostContent> = mediatorLiveDataForContentType()
    val searchContent: LiveData<SearchContent> = mediatorLiveDataForContentType()
    val tagListContent: LiveData<TagListContent> = mediatorLiveDataForContentType()
    val noteListContent: LiveData<NoteListContent> = mediatorLiveDataForContentType()
    val noteDetailContent: LiveData<NoteDetailContent> = mediatorLiveDataForContentType()
    val userPreferencesContent: LiveData<UserPreferencesContent> = mediatorLiveDataForContentType()

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
