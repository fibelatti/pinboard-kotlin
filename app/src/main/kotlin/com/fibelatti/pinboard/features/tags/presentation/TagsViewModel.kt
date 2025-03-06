package com.fibelatti.pinboard.features.tags.presentation

import androidx.compose.runtime.Stable
import com.fibelatti.core.functional.getOrThrow
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.SearchContent
import com.fibelatti.pinboard.features.appstate.SetSearchTags
import com.fibelatti.pinboard.features.appstate.SetTags
import com.fibelatti.pinboard.features.appstate.TagListContent
import com.fibelatti.pinboard.features.tags.domain.TagsRepository
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.domain.model.TagSorting
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TagsViewModel @Inject constructor(
    scope: CoroutineScope,
    appStateRepository: AppStateRepository,
    private val tagsRepository: TagsRepository,
) : BaseViewModel(scope, appStateRepository) {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> get() = _state.asStateFlow()

    init {
        appStateRepository.appState
            .onEach { appState ->
                when (val current = appState.content) {
                    is TagListContent -> {
                        if (current.shouldLoad) {
                            getAll(source = Source.MENU)
                        } else {
                            sortTags(tags = current.tags)
                        }
                    }

                    is SearchContent -> {
                        if (current.shouldLoadTags) {
                            getAll(source = Source.SEARCH)
                        } else {
                            sortTags(tags = current.availableTags)
                        }
                    }

                    else -> Unit
                }
            }
            .launchIn(scope)
    }

    fun getAll(source: Source) {
        tagsRepository.getAllTags()
            .map { result ->
                val tags = result.getOrThrow()
                val action = when (source) {
                    Source.MENU -> SetTags(tags)
                    Source.SEARCH -> SetSearchTags(tags)
                }
                runAction(action)
            }
            .catch { cause -> handleError(cause) }
            .launchIn(scope)
    }

    fun sortTags(
        tags: List<Tag> = _state.value.allTags,
        sorting: TagSorting = _state.value.currentSorting,
        searchQuery: String = _state.value.currentQuery,
    ) {
        _state.update { currentState ->
            val sorted = when (sorting) {
                TagSorting.AtoZ -> tags.sortedBy(Tag::name)
                TagSorting.MoreFirst -> tags.sortedByDescending(Tag::posts)
                TagSorting.LessFirst -> tags.sortedBy(Tag::posts)
            }

            currentState.copy(
                allTags = sorted,
                currentSorting = sorting,
                currentQuery = searchQuery,
                isLoading = false,
            )
        }
    }

    fun searchTags(query: String) {
        _state.update { currentState -> currentState.copy(currentQuery = query) }
    }

    fun renameTag(tag: Tag, newName: String) {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            _state.update { currentState ->
                tagsRepository.renameTag(oldName = tag.name, newName = newName)
                    .onSuccess { tags -> runAction(SetTags(tags, shouldReloadPosts = true)) }
                    .onFailure(::handleError)

                currentState.copy(isLoading = false)
            }
        }
    }

    enum class Source {
        MENU,
        SEARCH,
    }

    @Stable
    data class State(
        val allTags: List<Tag> = emptyList(),
        val currentSorting: TagSorting = TagSorting.AtoZ,
        val currentQuery: String = "",
        val isLoading: Boolean = true,
    ) {

        val filteredTags: List<Tag>
            get() = allTags.run {
                val queryIsBlank = currentQuery.isBlank()
                filter { queryIsBlank || it.name.startsWith(currentQuery, ignoreCase = true) }
            }
    }
}
