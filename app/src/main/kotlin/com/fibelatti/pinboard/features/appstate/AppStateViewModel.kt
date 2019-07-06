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

    val postList: LiveData<PostList> = mediatorLiveDataForContentType()
    val postDetail: LiveData<PostDetail> = mediatorLiveDataForContentType()
    val editPostView: LiveData<EditPostView> = mediatorLiveDataForContentType()
    val searchView: LiveData<SearchView> = mediatorLiveDataForContentType()
    val tagList: LiveData<TagList> = mediatorLiveDataForContentType()

    fun runAction(action: Action) {
        launch { appStateRepository.runAction(action) }
    }

    private inline fun <reified T : Content> mediatorLiveDataForContentType(): MediatorLiveData<T> =
        MediatorLiveData<T>().apply {
            addSource(appStateRepository.getContent()) { if (it is T) value = it }
        }
}
