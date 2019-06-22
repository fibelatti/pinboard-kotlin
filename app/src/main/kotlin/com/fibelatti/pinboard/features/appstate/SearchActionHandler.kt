package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.core.AppConfig
import javax.inject.Inject

class SearchActionHandler @Inject constructor() {

    fun runAction(action: SearchAction, currentContent: Content): Content {
        return when (action) {
            is SetSearchTags -> setSearchTags(action, currentContent)
            is AddSearchTag -> addSearchTag(action, currentContent)
            is RemoveSearchTag -> removeSearchTag(action, currentContent)
            is Search -> search(action, currentContent)
            is ClearSearch -> clearSearch(currentContent)
        }
    }

    private fun setSearchTags(action: SetSearchTags, currentContent: Content): Content {
        return if (currentContent is SearchView) {
            currentContent.copy(
                availableTags = action.tags.filterNot { it in currentContent.searchParameters.tags },
                allTags = action.tags,
                shouldLoadTags = false
            )
        } else {
            currentContent
        }
    }

    private fun addSearchTag(action: AddSearchTag, currentContent: Content): Content {
        return if (
            currentContent is SearchView &&
            currentContent.searchParameters.tags.size < AppConfig.API_FILTER_MAX_TAGS &&
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
        return if (currentContent is SearchView) {
            val newSearchParameters = currentContent.searchParameters.copy(
                tags = currentContent.searchParameters.tags.minus(action.tag)
            )

            currentContent.copy(
                searchParameters = newSearchParameters,
                availableTags = currentContent.allTags.filterNot { it in newSearchParameters.tags }
            )
        } else {
            currentContent
        }
    }

    private fun search(action: Search, currentContent: Content): Content {
        return if (currentContent is SearchView) {
            currentContent.previousContent.copy(
                searchParameters = currentContent.searchParameters.copy(term = action.term),
                shouldLoad = true
            )
        } else {
            currentContent
        }
    }

    private fun clearSearch(currentContent: Content): Content {
        return if (currentContent is PostList) {
            currentContent.copy(
                searchParameters = SearchParameters(),
                shouldLoad = true
            )
        } else {
            currentContent
        }
    }
}
