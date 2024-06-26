package com.fibelatti.bookmarking.features.posts.presentation

import com.fibelatti.bookmarking.core.base.BaseViewModel
import com.fibelatti.bookmarking.features.appstate.SearchParameters
import com.fibelatti.bookmarking.features.filters.domain.SavedFiltersRepository
import com.fibelatti.bookmarking.features.filters.domain.model.SavedFilter
import com.fibelatti.bookmarking.features.posts.domain.PostsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
public class SearchPostViewModel(
    private val postsRepository: PostsRepository,
    private val savedFiltersRepository: SavedFiltersRepository,
) : BaseViewModel() {

    private val _queryResultSize = MutableStateFlow(0)
    public val queryResultSize: StateFlow<Int> = _queryResultSize.asStateFlow()

    public fun searchParametersChanged(searchParameters: SearchParameters) {
        launch {
            _queryResultSize.value = postsRepository.getQueryResultSize(
                searchTerm = searchParameters.term,
                tags = searchParameters.tags,
            )
        }
    }

    public fun saveFilter(savedFilter: SavedFilter) {
        launch {
            savedFiltersRepository.saveFilter(savedFilter)
        }
    }
}
