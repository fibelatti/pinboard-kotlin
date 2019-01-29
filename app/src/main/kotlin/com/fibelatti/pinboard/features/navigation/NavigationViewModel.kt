package com.fibelatti.pinboard.features.navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fibelatti.core.archcomponents.BaseViewModel
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.posts.domain.model.Post
import javax.inject.Inject

class NavigationViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider
) : BaseViewModel() {

    // region LiveData and backing properties
    val contentType: LiveData<ContentType> get() = _contentType
    private val _contentType = MutableLiveData<ContentType>().apply {
        value = ContentType.ALL
    }
    val menuType: LiveData<MenuType> get() = _menuType
    private val _menuType = MutableLiveData<MenuType>().apply {
        value = MenuType.MAIN
    }
    val title: LiveData<String> get() = _title
    private val _title = MutableLiveData<String>().apply {
        value = resourceProvider.getString(R.string.posts_title_all)
    }
    val postCount: LiveData<Int> get() = _postCount
    private val _postCount = MutableLiveData<Int>()
    val post: LiveData<Post> get() = _post
    private val _post = MutableLiveData<Post>()
    // endregion

    fun viewContent(contentType: ContentType) {
        _contentType.postValue(contentType)
        updateTitle(contentType)
    }

    fun viewList() {
        _menuType.postValue(MenuType.MAIN)
        _contentType.value?.let(::updateTitle)
        _postCount.value?.let(::setPostCount)
    }

    fun viewLink(post: Post) {
        _menuType.postValue(MenuType.LINK)
        _title.postValue("")
        _post.postValue(post)
    }

    fun setPostCount(count: Int) {
        _postCount.postValue(count)
    }

    private fun updateTitle(contentType: ContentType) {
        _title.postValue(
            when (contentType) {
                ContentType.ALL -> resourceProvider.getString(R.string.posts_title_all)
                ContentType.RECENT -> resourceProvider.getString(R.string.posts_title_recent)
            }
        )
    }

    enum class ContentType {
        ALL,
        RECENT
    }

    enum class MenuType {
        MAIN, LINK
    }
}
