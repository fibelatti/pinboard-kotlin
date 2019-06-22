package com.fibelatti.pinboard.features.tags.presentation

import com.fibelatti.core.archcomponents.BaseViewModel
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.onFailure
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.SetSearchTags
import com.fibelatti.pinboard.features.tags.domain.usecase.GetAllTags
import kotlinx.coroutines.launch
import javax.inject.Inject

class TagsViewModel @Inject constructor(
    private val getAllTags: GetAllTags,
    private val appStateRepository: AppStateRepository
) : BaseViewModel() {

    fun getAll() {
        launch {
            getAllTags()
                .mapCatching { appStateRepository.runAction(SetSearchTags(it)) }
                .onFailure(::handleError)
        }
    }
}
