package com.fibelatti.pinboard.features.tags.presentation

import com.fibelatti.core.extension.exhaustive
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.onFailure
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.SetSearchTags
import com.fibelatti.pinboard.features.appstate.SetTags
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.domain.model.TagSorting
import com.fibelatti.pinboard.features.tags.domain.usecase.GetAllTags
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class TagsViewModel @Inject constructor(
    private val getAllTags: GetAllTags,
    private val appStateRepository: AppStateRepository,
) : BaseViewModel() {

    val tags: Flow<List<Tag>> get() = _tags.filterNotNull()
    private val _tags = MutableStateFlow<List<Tag>?>(null)

    fun getAll(source: Source) {
        _tags.value = null
        launch {
            getAllTags()
                .mapCatching {
                    when (source) {
                        Source.MENU -> appStateRepository.runAction(SetTags(it))
                        Source.SEARCH -> appStateRepository.runAction(SetSearchTags(it))
                    }.exhaustive
                }
                .onFailure(::handleError)
        }
    }

    fun sortTags(tags: List<Tag>, sorting: TagSorting) {
        _tags.value = when (sorting) {
            TagSorting.AtoZ -> tags.sortedBy(Tag::name)
            TagSorting.MoreFirst -> tags.sortedByDescending(Tag::posts)
            TagSorting.LessFirst -> tags.sortedBy(Tag::posts)
        }
    }

    enum class Source {
        MENU,
        SEARCH
    }
}
