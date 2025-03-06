package com.fibelatti.pinboard.features.main.reducer

import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.pinboard.features.appstate.AppState
import com.fibelatti.pinboard.features.appstate.NoteDetailContent
import com.fibelatti.pinboard.features.main.MainState
import javax.inject.Inject

class NoteDetailsReducer @Inject constructor(
    private val resourceProvider: ResourceProvider,
) : MainStateReducer {

    override fun invoke(mainState: MainState, appState: AppState): MainState {
        if (appState.content !is NoteDetailContent) return mainState

        return if (appState.multiPanelAvailable) {
            mainState.copy(
                sidePanelAppBar = MainState.SidePanelAppBarComponent.Visible(
                    contentType = NoteDetailContent::class,
                    menuItems = listOf(MainState.MenuItemComponent.CloseSidePanel),
                ),
            )
        } else {
            mainState.copy(
                title = MainState.TitleComponent.Gone,
                subtitle = MainState.TitleComponent.Gone,
                navigation = MainState.NavigationComponent.Visible(),
                bottomAppBar = MainState.BottomAppBarComponent.Gone,
                floatingActionButton = MainState.FabComponent.Gone,
            )
        }
    }
}
