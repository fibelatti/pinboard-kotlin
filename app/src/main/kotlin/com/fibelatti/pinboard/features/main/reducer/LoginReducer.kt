package com.fibelatti.pinboard.features.main.reducer

import com.fibelatti.pinboard.features.appstate.AppState
import com.fibelatti.pinboard.features.main.MainState
import javax.inject.Inject

class LoginReducer @Inject constructor() : MainStateReducer {

    override fun invoke(mainState: MainState, appState: AppState): MainState {
        return MainState(
            navigation = MainState.NavigationComponent.Visible(),
        )
    }
}
