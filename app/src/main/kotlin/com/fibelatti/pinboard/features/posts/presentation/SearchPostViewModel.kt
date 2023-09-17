package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.SearchParameters
import com.fibelatti.pinboard.features.filters.domain.SavedFiltersRepository
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchPostViewModel @Inject constructor(
    private val postsRepository: PostsRepository,
    private val savedFiltersRepository: SavedFiltersRepository,
) : BaseViewModel() {

    private val _queryResultSize = MutableStateFlow(0)
    val queryResultSize: StateFlow<Int> = _queryResultSize.asStateFlow()

    fun searchParametersChanged(searchParameters: SearchParameters) {
        launch {
            _queryResultSize.value = postsRepository.getQueryResultSize(
                searchTerm = searchParameters.term,
                tags = searchParameters.tags,
            )
        }
    }

    fun saveFilter(savedFilter: SavedFilter) {
        launch {
            savedFiltersRepository.saveFilter(savedFilter)
        }
    }
}
