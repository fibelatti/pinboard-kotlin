package com.fibelatti.pinboard.features.navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fibelatti.core.archcomponents.BaseViewModel
import com.fibelatti.core.archcomponents.LiveEvent
import com.fibelatti.core.archcomponents.MutableLiveEvent
import com.fibelatti.core.archcomponents.postEvent
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig
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
            contentType = ContentType.All,
            title = resourceProvider.getString(R.string.posts_title_all),
            sortType = NewestFirst,
            search = Search()
        )
    }
    val newSort: LiveEvent<SortType> get() = _newSort
    private val _newSort = MutableLiveEvent<SortType>()
    val search: LiveData<Search> get() = _search
    private val _search = MutableLiveData<Search>().apply {
        value = Search()
    }
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
                is ContentType.All -> resourceProvider.getString(R.string.posts_title_all)
                is ContentType.Recent -> resourceProvider.getString(R.string.posts_title_recent)
                is ContentType.Tags -> resourceProvider.getString(R.string.posts_title_tags)
                is ContentType.Tag -> contentType.tagName
            }
        )
    }

    fun viewLink(post: Post) {
        _post.value = post
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

    fun updateSearchTags(tag: String, shouldRemove: Boolean = false) {
        _search.value?.let {
            _search.value = it.copy(
                tags = it.tags.apply {
                    when {
                        shouldRemove -> remove(tag)
                        !it.tags.contains(tag) && it.tags.size < AppConfig.API_FILTER_MAX_TAGS -> add(tag)
                    }
                }
            )
        }
    }

    fun search(term: String) {
        _search.value?.let {
            val newSearch = it.copy(term = term)
            _search.value = newSearch
            updateContent(search = newSearch)
        }
    }

    fun clearSearch() {
        val newSearch = Search()
        _search.value = newSearch
        updateContent(search = newSearch)
    }

    private fun updateContent(
        contentType: ContentType? = null,
        title: String? = null,
        sortType: SortType? = null,
        search: Search? = null
    ) {
        _content.value?.let {
            _content.value = it.copy(
                contentType = contentType ?: it.contentType,
                title = title ?: it.title,
                sortType = sortType ?: it.sortType,
                search = search ?: it.search
            )
        }
    }

    sealed class ContentType {
        object All : ContentType()
        object Recent : ContentType()
        object Tags : ContentType()
        class Tag(val tagName: String) : ContentType()
    }

    data class Content(
        val contentType: ContentType,
        val title: String,
        val sortType: SortType,
        val search: Search
    )
    data class Search(val term: String = "", val tags: MutableList<String> = mutableListOf())
}
