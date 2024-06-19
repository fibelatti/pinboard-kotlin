package com.fibelatti.pinboard.features.tags.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel
import javax.inject.Inject

@KoinViewModel
@HiltViewModel
class TagManagerViewModel @Inject constructor() : BaseViewModel() {

    private val _state: MutableStateFlow<State> = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun initializeTags(tags: List<Tag>) {
        if (_state.value == State()) {
            _state.update { current -> current.copy(tags = tags) }
        }
    }

    fun addTag(value: String, index: Int = 0) {
        val currentTags = _state.value.tags.map { it.name }
        val newTags = value.trim()
            .split(" ")
            .filterNot { it in currentTags }
            .map(::Tag)

        _state.update { current ->
            current.copy(
                tags = current.tags.toMutableList().apply { addAll(index, newTags) },
                currentQuery = "",
            )
        }
    }

    fun removeTag(tag: Tag) {
        _state.update { current -> current.copy(tags = current.tags.minus(tag)) }
    }

    fun setSuggestedTags(tags: List<String>) {
        _state.update { current -> current.copy(suggestedTags = tags) }
    }

    fun setQuery(value: String) {
        _state.update { current -> current.copy(currentQuery = value) }
    }

    @Stable
    data class State(
        val tags: List<Tag> = emptyList(),
        val suggestedTags: List<String> = emptyList(),
        val currentQuery: String = "",
    ) {

        @get:StringRes
        val displayTitle: Int
            get() = if (tags.isEmpty()) R.string.tags_empty_title else R.string.tags_added_title
    }
}
