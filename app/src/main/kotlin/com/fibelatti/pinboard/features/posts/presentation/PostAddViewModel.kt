package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.archcomponents.BaseViewModel
import com.fibelatti.core.archcomponents.LiveEvent
import com.fibelatti.core.archcomponents.MutableLiveEvent
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.GetSuggestedTagsForUrl
import javax.inject.Inject

class PostAddViewModel @Inject constructor(
    private val addPost: AddPost,
    private val suggestedTagsForUrl: GetSuggestedTagsForUrl
) : BaseViewModel() {

    val loading: LiveEvent<Boolean> get() = _loading
    private val _loading = MutableLiveEvent<Boolean>()
}
