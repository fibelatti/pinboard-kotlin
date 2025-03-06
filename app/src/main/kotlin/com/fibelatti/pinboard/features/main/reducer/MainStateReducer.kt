package com.fibelatti.pinboard.features.main.reducer

import com.fibelatti.pinboard.features.appstate.AppState
import com.fibelatti.pinboard.features.main.MainState

fun interface MainStateReducer : (MainState, AppState) -> MainState
