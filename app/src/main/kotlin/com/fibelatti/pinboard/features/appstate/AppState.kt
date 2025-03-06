package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.core.AppMode

data class AppState(
    val appMode: AppMode,
    val content: Content,
    val multiPanelAvailable: Boolean,
) {

    val sidePanelVisible: Boolean
        get() = content is SidePanelContent && multiPanelAvailable
}
