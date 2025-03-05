package com.fibelatti.pinboard.features.main.reducer

import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.appstate.AppState

fun interface MainStateReducer : (MainState, AppState) -> MainState
