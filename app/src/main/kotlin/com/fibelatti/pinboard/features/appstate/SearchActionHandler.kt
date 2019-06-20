package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.core.AppConfig
import javax.inject.Inject

class SearchActionHandler @Inject constructor() {

    fun runAction(action: SearchAction, currentContent: Content): Content {
        return when (action) {
            is AddSearchTag -> addSearchTag(action, currentContent)
            is RemoveSearchTag -> removeSearchTag(action, currentContent)
            is Search -> search(action, currentContent)
            is ClearSearch -> clearSearch(currentContent)
        }
    }

    private fun addSearchTag(action: AddSearchTag, currentContent: Content): Content {
        return if (
            currentContent is SearchView &&
            !currentContent.searchParameters.tags.contains(action.tag) &&
            currentContent.searchParameters.tags.size < AppConfig.API_FILTER_MAX_TAGS
        ) {
            currentContent.copy(
                searchParameters = currentContent.searchParameters.copy(
                    tags = currentContent.searchParameters.tags.plus(action.tag)
                )
            )
        } else {
            currentContent
        }
    }

    private fun removeSearchTag(action: RemoveSearchTag, currentContent: Content): Content {
        return if (currentContent is SearchView) {
            currentContent.copy(
                searchParameters = currentContent.searchParameters.copy(
                    tags = currentContent.searchParameters.tags.minus(action.tag)
                )
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
