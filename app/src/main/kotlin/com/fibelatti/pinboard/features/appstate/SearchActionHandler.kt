package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.core.AppConfig
import javax.inject.Inject

class SearchActionHandler @Inject constructor() : ActionHandler<SearchAction>() {

    override suspend fun runAction(action: SearchAction, currentContent: Content): Content {
        return when (action) {
            is RefreshSearchTags -> refresh(currentContent)
            is SetTerm -> setSearchTerm(action, currentContent)
            is SetSearchTags -> setSearchTags(action, currentContent)
            is AddSearchTag -> addSearchTag(action, currentContent)
            is RemoveSearchTag -> removeSearchTag(action, currentContent)
            is Search -> search(action, currentContent)
            is ClearSearch -> clearSearch(currentContent)
        }
    }

    private fun refresh(currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<SearchContent>(currentContent) {
            it.copy(shouldLoadTags = true)
        }
    }

    private fun setSearchTerm(action: SetTerm, currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<SearchContent>(currentContent) { searchContent ->
            searchContent.copy(searchParameters = searchContent.searchParameters.copy(term = action.term))
        }
    }

    private fun setSearchTags(action: SetSearchTags, currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<SearchContent>(currentContent) { searchContent ->
            searchContent.copy(
                availableTags = action.tags.filterNot { it in searchContent.searchParameters.tags },
                allTags = action.tags,
                shouldLoadTags = false
            )
        }
    }

    private fun addSearchTag(action: AddSearchTag, currentContent: Content): Content {
        return if (
            currentContent is SearchContent &&
            currentContent.searchParameters.tags.size < AppConfig.DEFAULT_FILTER_MAX_TAGS &&
            currentContent.searchParameters.tags.none { it == action.tag }
        ) {
            val newSearchParameters = currentContent.searchParameters.copy(
                tags = currentContent.searchParameters.tags.plus(action.tag)
            )

            currentContent.copy(
                searchParameters = newSearchParameters,
                availableTags = currentContent.allTags.filterNot { it in newSearchParameters.tags }
            )
        } else {
            currentContent
        }
    }

    private fun removeSearchTag(action: RemoveSearchTag, currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<SearchContent>(currentContent) { searchView ->
            val newSearchParameters = searchView.searchParameters.copy(
                tags = searchView.searchParameters.tags.minus(action.tag)
            )

            searchView.copy(
                searchParameters = newSearchParameters,
                availableTags = searchView.allTags.filterNot { it in newSearchParameters.tags }
            )
        }
    }

    private fun search(action: Search, currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<SearchContent>(currentContent) {
            it.previousContent.copy(
                searchParameters = it.searchParameters.copy(term = action.term),
                shouldLoad = ShouldLoadFirstPage
            )
        }
    }

    private fun clearSearch(currentContent: Content): Content {
        return when (currentContent) {
            is PostListContent -> {
                currentContent.copy(
                    searchParameters = SearchParameters(),
                    shouldLoad = ShouldLoadFirstPage
                )
            }
            is SearchContent -> {
                currentContent.previousContent.copy(
                    searchParameters = SearchParameters(),
                    shouldLoad = ShouldLoadFirstPage
                )
            }
            else -> currentContent
        }
    }
}
