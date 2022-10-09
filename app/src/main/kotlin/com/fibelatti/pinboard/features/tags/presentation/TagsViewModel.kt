package com.fibelatti.pinboard.features.tags.presentation

import androidx.lifecycle.viewModelScope
import com.fibelatti.core.functional.getOrThrow
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.SetSearchTags
import com.fibelatti.pinboard.features.appstate.SetTags
import com.fibelatti.pinboard.features.tags.domain.TagsRepository
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.domain.model.TagSorting
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagsViewModel @Inject constructor(
    private val tagsRepository: TagsRepository,
    private val appStateRepository: AppStateRepository,
) : BaseViewModel() {

    val tags: Flow<List<Tag>> get() = _tags.filterNotNull()
    private val _tags = MutableStateFlow<List<Tag>?>(null)
    val loading: Flow<Boolean> get() = _loading.filterNotNull()
    private val _loading = MutableStateFlow<Boolean?>(null)

    fun getAll(source: Source) {
        _tags.value = null
        tagsRepository.getAllTags()
            .map { result ->
                val tags = result.getOrThrow()
                return@map when (source) {
                    Source.MENU -> SetTags(tags)
                    Source.SEARCH -> SetSearchTags(tags)
                }
            }
            .onEach(appStateRepository::runAction)
            .catch { cause -> handleError(cause) }
            .launchIn(viewModelScope)
    }

    fun sortTags(tags: List<Tag>, sorting: TagSorting) {
        _tags.value = when (sorting) {
            TagSorting.AtoZ -> tags.sortedBy(Tag::name)
            TagSorting.MoreFirst -> tags.sortedByDescending(Tag::posts)
            TagSorting.LessFirst -> tags.sortedBy(Tag::posts)
        }
    }

    fun renameTag(tag: Tag, newName: String) {
        viewModelScope.launch {
            _loading.value = true
            tagsRepository.renameTag(oldName = tag.name, newName = newName)
                .onSuccess { tags -> appStateRepository.runAction(SetTags(tags, shouldReloadPosts = true)) }
                .onFailure(::handleError)
            _loading.value = false
        }
    }

    enum class Source {
        MENU,
        SEARCH
    }
}
