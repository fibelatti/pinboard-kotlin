package com.fibelatti.pinboard.features.tags.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fibelatti.core.archcomponents.BaseViewModel
import com.fibelatti.core.archcomponents.LiveEvent
import com.fibelatti.core.archcomponents.MutableLiveEvent
import com.fibelatti.core.archcomponents.postEvent
import com.fibelatti.core.archcomponents.setEvent
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.domain.usecase.GetAllTags
import kotlinx.coroutines.launch
import javax.inject.Inject

class TagsViewModel @Inject constructor(
    private val getAllTags: GetAllTags
) : BaseViewModel() {

    val tags: LiveData<List<Tag>> get() = _tags
    private val _tags = MutableLiveData<List<Tag>>()

    val loading: LiveEvent<Boolean> get() = _loading
    private val _loading = MutableLiveEvent<Boolean>().apply { setEvent(true) }

    fun getAll() {
        launch {
            _loading.postEvent(true)
            getAllTags()
                .onSuccess {
                    _tags.postValue(it)
                    _loading.postEvent(false)
                }
                .onFailure(::handleError)
        }
    }
}
