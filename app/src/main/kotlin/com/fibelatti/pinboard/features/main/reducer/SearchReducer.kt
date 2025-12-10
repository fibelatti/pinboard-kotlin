package com.fibelatti.pinboard.features.main.reducer

import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.appstate.AppState
import com.fibelatti.pinboard.features.appstate.SearchContent
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import com.fibelatti.pinboard.features.main.MainState
import javax.inject.Inject

class SearchReducer @Inject constructor(
    private val resourceProvider: ResourceProvider,
) : MainStateReducer {

    override fun invoke(mainState: MainState, appState: AppState): MainState {
        val content = appState.content as? SearchContent ?: return mainState
        val isActive = content.searchParameters.isActive()

        val activeSearchLabel = resourceProvider.getQuantityString(
            R.plurals.search_result_size,
            content.queryResultSize,
            content.queryResultSize,
        )

        return MainState(
            title = MainState.TitleComponent.Visible(resourceProvider.getString(R.string.search_title)),
            subtitle = if (isActive) {
                MainState.TitleComponent.Visible(label = activeSearchLabel)
            } else {
                MainState.TitleComponent.Gone
            },
            navigation = MainState.NavigationComponent.Visible(),
            actionButton = if (content.allTags.isNotEmpty()) {
                MainState.ActionButtonComponent.Visible(
                    contentType = SearchContent::class,
                    icon = R.drawable.ic_random,
                    label = resourceProvider.getString(R.string.search_random),
                )
            } else {
                MainState.ActionButtonComponent.Gone
            },
            bottomAppBar = MainState.BottomAppBarComponent.Visible(
                contentType = SearchContent::class,
                menuItems = if (isActive) {
                    listOf(
                        MainState.MenuItemComponent.ClearSearch,
                        MainState.MenuItemComponent.SaveSearch,
                    )
                } else {
                    emptyList()
                },
                data = SavedFilter(
                    searchTerm = content.searchParameters.term,
                    tags = content.searchParameters.tags,
                ),
            ),
            floatingActionButton = MainState.FabComponent.Visible(
                contentType = SearchContent::class,
                icon = R.drawable.ic_search,
            ),
        )
    }
}
