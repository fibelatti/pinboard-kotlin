package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.core.AppMode

data class AppState(
    val appMode: AppMode,
    val content: Content,
    val multiPanelAvailable: Boolean,
    val useSplitNav: Boolean,
) {

    val sidePanelVisible: Boolean
        get() = content is SidePanelContent && multiPanelAvailable && useSplitNav

    fun prettyPrint(): String = "AppState(" +
        "appMode=$appMode, " +
        "content=${content.prettyPrint()}, " +
        "multiPanelAvailable=$multiPanelAvailable, " +
        "useSplitNav=$useSplitNav" +
        ")"
}
