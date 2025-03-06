package com.fibelatti.pinboard.features.main.reducer

import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.appstate.AppState
import com.fibelatti.pinboard.features.main.MainState
import javax.inject.Inject

class UserPreferencesReducer @Inject constructor(
    private val resourceProvider: ResourceProvider,
) : MainStateReducer {

    override fun invoke(mainState: MainState, appState: AppState): MainState {
        return mainState.copy(
            title = MainState.TitleComponent.Visible(resourceProvider.getString(R.string.user_preferences_title)),
            subtitle = MainState.TitleComponent.Gone,
            navigation = MainState.NavigationComponent.Visible(),
            bottomAppBar = MainState.BottomAppBarComponent.Gone,
            floatingActionButton = MainState.FabComponent.Gone,
        )
    }
}
