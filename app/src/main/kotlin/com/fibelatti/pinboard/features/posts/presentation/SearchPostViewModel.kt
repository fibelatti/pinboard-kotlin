package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.SearchContent
import com.fibelatti.pinboard.features.appstate.SetResultSize
import com.fibelatti.pinboard.features.filters.domain.SavedFiltersRepository
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class SearchPostViewModel @Inject constructor(
    scope: CoroutineScope,
    appStateRepository: AppStateRepository,
    private val postsRepository: PostsRepository,
    private val savedFiltersRepository: SavedFiltersRepository,
) : BaseViewModel(scope, appStateRepository) {

    val searchContent: Flow<SearchContent> get() = filteredContent<SearchContent>()

    init {
        scope.launch {
            searchContent.collectLatest { content ->
                val resultSize = if (content.searchParameters.isActive()) {
                    postsRepository.getQueryResultSize(
                        searchTerm = content.searchParameters.term,
                        tags = content.searchParameters.tags,
                        matchAll = content.searchParameters.matchAll,
                        exactMatch = content.searchParameters.exactMatch,
                    )
                } else {
                    0
                }

                runAction(SetResultSize(resultSize = resultSize))
            }
        }
    }

    fun saveFilter(savedFilter: SavedFilter) {
        scope.launch {
            savedFiltersRepository.saveFilter(savedFilter)
        }
    }
}
