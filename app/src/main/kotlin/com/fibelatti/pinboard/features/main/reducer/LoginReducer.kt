package com.fibelatti.pinboard.features.main.reducer

import com.fibelatti.pinboard.features.appstate.AppState
import com.fibelatti.pinboard.features.main.MainState
import javax.inject.Inject

class LoginReducer @Inject constructor() : MainStateReducer {

    override fun invoke(mainState: MainState, appState: AppState): MainState {
        return mainState.copy(
            title = MainState.TitleComponent.Gone,
            subtitle = MainState.TitleComponent.Gone,
            navigation = MainState.NavigationComponent.Visible(),
            bottomAppBar = MainState.BottomAppBarComponent.Gone,
            floatingActionButton = MainState.FabComponent.Gone,
        )
    }
}
