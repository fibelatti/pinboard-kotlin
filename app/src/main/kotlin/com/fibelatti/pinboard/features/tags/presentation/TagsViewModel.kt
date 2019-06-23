package com.fibelatti.pinboard.features.tags.presentation

import com.fibelatti.core.archcomponents.BaseViewModel
import com.fibelatti.core.extension.exhaustive
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.onFailure
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.SetSearchTags
import com.fibelatti.pinboard.features.appstate.SetTags
import com.fibelatti.pinboard.features.tags.domain.usecase.GetAllTags
import kotlinx.coroutines.launch
import javax.inject.Inject

class TagsViewModel @Inject constructor(
    private val getAllTags: GetAllTags,
    private val appStateRepository: AppStateRepository
) : BaseViewModel() {

    fun getAll(source: Source) {
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

    enum class Source {
        MENU,
        SEARCH
    }
}
