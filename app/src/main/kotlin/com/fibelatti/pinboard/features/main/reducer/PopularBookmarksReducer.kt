package com.fibelatti.pinboard.features.main.reducer

import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.appstate.AppState
import com.fibelatti.pinboard.features.appstate.PopularPostsContent
import com.fibelatti.pinboard.features.main.MainState
import javax.inject.Inject

class PopularBookmarksReducer @Inject constructor(
    private val resourceProvider: ResourceProvider,
) : MainStateReducer {

    override fun invoke(mainState: MainState, appState: AppState): MainState {
        if (appState.content !is PopularPostsContent) return mainState

        return MainState(
            title = MainState.TitleComponent.Visible(resourceProvider.getString(R.string.popular_title)),
            navigation = MainState.NavigationComponent.Visible(),
        )
    }
}
