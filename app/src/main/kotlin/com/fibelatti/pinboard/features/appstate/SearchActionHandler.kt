package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.GetPreferredSortType
import javax.inject.Inject

class SearchActionHandler @Inject constructor(
    private val getPreferredSortType: GetPreferredSortType,
) : ActionHandler<SearchAction>() {

    override suspend fun runAction(action: SearchAction, currentContent: Content): Content {
        return when (action) {
            is RefreshSearchTags -> refresh(currentContent)
            is SetAdvancedSearchParameters -> setAdvancedSearchParameters(action, currentContent)
            is SetTerm -> setSearchTerm(action, currentContent)
            is SetSearchTags -> setSearchTags(action, currentContent)
            is AddSearchTag -> addSearchTag(action, currentContent)
            is RemoveSearchTag -> removeSearchTag(action, currentContent)
            is SetResultSize -> setResultSize(action, currentContent)
            is ViewRandomSearch -> viewRandomSearch(currentContent)
            is Search -> search(currentContent)
            is ClearSearch -> clearSearch(currentContent)
            is ViewSavedFilter -> viewSavedFilter(action, currentContent)
        }
    }

    private fun refresh(currentContent: Content): Content {
        return currentContent.reduce<SearchContent> { searchContent ->
            searchContent.copy(shouldLoadTags = true)
        }
    }

    private fun setAdvancedSearchParameters(
        action: SetAdvancedSearchParameters,
        currentContent: Content,
    ): Content {
        return currentContent.reduce<SearchContent> { searchContent ->
            searchContent.copy(
                searchParameters = searchContent.searchParameters.copy(
                    matchAll = action.matchAll,
                    exactMatch = action.exactMatch,
                ),
            )
        }
    }

    private fun setSearchTerm(action: SetTerm, currentContent: Content): Content {
        return currentContent.reduce<SearchContent> { searchContent ->
            searchContent.copy(searchParameters = searchContent.searchParameters.copy(term = action.term))
        }
    }

    private fun setSearchTags(action: SetSearchTags, currentContent: Content): Content {
        return currentContent.reduce<SearchContent> { searchContent ->
            searchContent.copy(
                availableTags = action.tags.filterNot { it in searchContent.searchParameters.tags },
                allTags = action.tags,
                shouldLoadTags = false,
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
                tags = currentContent.searchParameters.tags.plus(action.tag),
            )

            currentContent.copy(
                searchParameters = newSearchParameters,
                availableTags = currentContent.allTags.filterNot { it in newSearchParameters.tags },
            )
        } else {
            currentContent
        }
    }

    private fun removeSearchTag(action: RemoveSearchTag, currentContent: Content): Content {
        return currentContent.reduce<SearchContent> { searchContent ->
            val newSearchParameters = searchContent.searchParameters.copy(
                tags = searchContent.searchParameters.tags.minus(action.tag),
            )

            searchContent.copy(
                searchParameters = newSearchParameters,
                availableTags = searchContent.allTags.filterNot { it in newSearchParameters.tags },
            )
        }
    }

    private fun setResultSize(action: SetResultSize, currentContent: Content): Content {
        return currentContent.reduce<SearchContent> { searchContent ->
            searchContent.copy(queryResultSize = action.resultSize)
        }
    }

    private fun viewRandomSearch(currentContent: Content): Content {
        return currentContent.reduce<SearchContent> { searchContent ->
            val tags: List<Tag> = if (searchContent.allTags.isNotEmpty()) {
                listOf(searchContent.allTags.random())
            } else {
                emptyList()
            }

            searchContent.previousContent.copy(
                category = All,
                sortType = getPreferredSortType(),
                searchParameters = SearchParameters(tags = tags),
                shouldLoad = ShouldLoadFirstPage,
            )
        }
    }

    private fun search(currentContent: Content): Content {
        return currentContent.reduce<SearchContent> { searchContent ->
            searchContent.previousContent.copy(
                category = All,
                sortType = getPreferredSortType(),
                searchParameters = searchContent.searchParameters,
                shouldLoad = ShouldLoadFirstPage,
            )
        }
    }

    private fun clearSearch(currentContent: Content): Content {
        val body = { postListContent: PostListContent ->
            postListContent.copy(
                searchParameters = SearchParameters(),
                shouldLoad = ShouldLoadFirstPage,
            )
        }

        return currentContent
            .reduce(body)
            .reduce<PostDetailContent> { postDetailContent ->
                postDetailContent.copy(
                    previousContent = body(postDetailContent.previousContent),
                )
            }.reduce<SearchContent> { searchContent ->
                body(searchContent.previousContent)
            }
    }

    private fun viewSavedFilter(action: ViewSavedFilter, currentContent: Content): Content {
        return currentContent.reduce<SavedFiltersContent> { savedFiltersContent ->
            savedFiltersContent.previousContent.copy(
                category = All,
                sortType = getPreferredSortType(),
                searchParameters = SearchParameters(
                    term = action.savedFilter.searchTerm,
                    tags = action.savedFilter.tags,
                ),
                shouldLoad = ShouldLoadFirstPage,
            )
        }
    }
}
