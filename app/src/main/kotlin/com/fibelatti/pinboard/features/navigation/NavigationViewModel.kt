package com.fibelatti.pinboard.features.navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fibelatti.core.archcomponents.BaseViewModel
import com.fibelatti.core.archcomponents.LiveEvent
import com.fibelatti.core.archcomponents.MutableLiveEvent
import com.fibelatti.core.archcomponents.postEvent
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.NewestFirst
import com.fibelatti.pinboard.features.posts.domain.usecase.OldestFirst
import com.fibelatti.pinboard.features.posts.domain.usecase.SortType
import javax.inject.Inject

class NavigationViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider
) : BaseViewModel() {

    // region LiveData and backing properties
    val content: LiveData<Content> get() = _content
    private val _content = MutableLiveData<Content>().apply {
        value = Content(
            contentType = ContentType.ALL,
            title = resourceProvider.getString(R.string.posts_title_all),
            sortType = NewestFirst
        )
    }
    val newSort: LiveEvent<SortType> get() = _newSort
    private val _newSort = MutableLiveEvent<SortType>()
    val search: LiveData<Search> get() = _search
    private val _search = MutableLiveData<Search>()
    val post: LiveData<Post> get() = _post
    private val _post = MutableLiveData<Post>()
    // endregion

    fun backNavigation(stackCount: Int) {
        _content.value?.contentType?.takeIf { stackCount == 0 }?.let(::viewContent)
    }

    fun viewContent(contentType: ContentType) {
        updateContent(
            contentType = contentType,
            title = when (contentType) {
                ContentType.ALL -> resourceProvider.getString(R.string.posts_title_all)
                ContentType.RECENT -> resourceProvider.getString(R.string.posts_title_recent)
            }
        )
    }

    fun viewLink(post: Post) {
        _post.postValue(post)
    }

    fun toggleSorting() {
        val newSort = when (_content.value?.sortType) {
            NewestFirst -> OldestFirst
            OldestFirst -> NewestFirst
            else -> null
        }

        newSort?.let {
            updateContent(sortType = newSort)
            _newSort.postEvent(newSort)
        }
    }

    fun search(term: String, tags: List<String>) {
        _search.postValue(Search(term, tags))
    }

    fun clearSearch() {
        _search.postValue(Search())
    }

    private fun updateContent(
        contentType: ContentType? = null,
        title: String? = null,
        sortType: SortType? = null
    ) {
        _content.value?.let {
            it.copy(
                contentType = contentType ?: it.contentType,
                title = title ?: it.title,
                sortType = sortType ?: it.sortType
            )
        }?.also(_content::postValue)
    }

    enum class ContentType {
        ALL,
        RECENT
    }

    data class Content(val contentType: ContentType, val title: String, val sortType: SortType)
    class Search(val term: String = "", val tags: List<String> = emptyList())
}
