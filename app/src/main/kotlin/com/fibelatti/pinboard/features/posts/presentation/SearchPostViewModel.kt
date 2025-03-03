package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.SearchContent
import com.fibelatti.pinboard.features.filters.domain.SavedFiltersRepository
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SearchPostViewModel @Inject constructor(
    scope: CoroutineScope,
    sharingStarted: SharingStarted,
    appStateRepository: AppStateRepository,
    private val postsRepository: PostsRepository,
    private val savedFiltersRepository: SavedFiltersRepository,
) : BaseViewModel(scope, appStateRepository) {

    val searchContent: Flow<SearchContent> get() = filteredContent<SearchContent>()

    val queryResultSize: StateFlow<Int> = searchContent
        .mapLatest { content ->
            if (content.searchParameters.isActive()) {
                postsRepository.getQueryResultSize(
                    searchTerm = content.searchParameters.term,
                    tags = content.searchParameters.tags,
                )
            } else {
                0
            }
        }
        .stateIn(scope = scope, started = sharingStarted, initialValue = 0)

    fun saveFilter(savedFilter: SavedFilter) {
        scope.launch {
            savedFiltersRepository.saveFilter(savedFilter)
        }
    }
}
