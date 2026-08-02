package com.fibelatti.pinboard.features.main.reducer

import com.fibelatti.pinboard.core.android.icons.AppIcons
import com.fibelatti.pinboard.core.android.icons.Browser
import com.fibelatti.pinboard.features.appstate.AppState
import com.fibelatti.pinboard.features.appstate.OfflineCopyDetailContent
import com.fibelatti.pinboard.features.main.MainState
import javax.inject.Inject

class OfflineCopyDetailsReducer @Inject constructor() : MainStateReducer {

    override fun invoke(mainState: MainState, appState: AppState): MainState {
        val content: OfflineCopyDetailContent = appState.content as? OfflineCopyDetailContent ?: return mainState

        return if (appState.sidePanelVisible) {
            mainState.copy(
                bottomAppBar = MainState.BottomAppBarComponent.Gone,
                floatingActionButton = MainState.FabComponent.Gone,
                sidePanelAppBar = MainState.SidePanelAppBarComponent.Visible(
                    contentType = OfflineCopyDetailContent::class,
                    menuItems = listOf(
                        MainState.MenuItemComponent.RemoveOfflineCopy,
                        MainState.MenuItemComponent.OpenInBrowser,
                        MainState.MenuItemComponent.CloseSidePanel,
                    ),
                    data = content.offlineCopy,
                ),
            )
        } else {
            MainState(
                navigation = MainState.NavigationComponent.Visible(),
                bottomAppBar = MainState.BottomAppBarComponent.Visible(
                    contentType = OfflineCopyDetailContent::class,
                    menuItems = listOf(MainState.MenuItemComponent.RemoveOfflineCopy),
                    navigationIcon = null,
                    data = content.offlineCopy,
                ),
                floatingActionButton = MainState.FabComponent.Visible(
                    contentType = OfflineCopyDetailContent::class,
                    icon = AppIcons.Browser,
                    data = content.offlineCopy,
                ),
            )
        }
    }
}
