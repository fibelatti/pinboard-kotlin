package com.fibelatti.pinboard.features.main.reducer

import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.appstate.AppState
import com.fibelatti.pinboard.features.appstate.PopularPostsContent
import javax.inject.Inject

class PopularBookmarksReducer @Inject constructor(
    private val resourceProvider: ResourceProvider,
) : MainStateReducer {

    override fun invoke(mainState: MainState, appState: AppState): MainState {
        if (appState.content !is PopularPostsContent) return mainState

        return mainState.copy(
            title = MainState.TitleComponent.Visible(resourceProvider.getString(R.string.popular_title)),
            subtitle = MainState.TitleComponent.Gone,
            navigation = MainState.NavigationComponent.Visible(),
            bottomAppBar = MainState.BottomAppBarComponent.Gone,
            floatingActionButton = MainState.FabComponent.Gone,
        )
    }
}
