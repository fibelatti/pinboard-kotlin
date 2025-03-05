package com.fibelatti.pinboard.features.main.reducer

import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.appstate.AppState
import com.fibelatti.pinboard.features.appstate.NoteListContent
import javax.inject.Inject

class NoteListReducer @Inject constructor(
    private val resourceProvider: ResourceProvider,
) : MainStateReducer {

    override fun invoke(mainState: MainState, appState: AppState): MainState {
        val content = appState.content as? NoteListContent ?: return mainState

        return mainState.copy(
            title = MainState.TitleComponent.Visible(resourceProvider.getString(R.string.notes_title)),
            subtitle = when {
                content.shouldLoad -> MainState.TitleComponent.Gone
                content.notes.isEmpty() -> MainState.TitleComponent.Gone
                else -> MainState.TitleComponent.Visible(
                    resourceProvider.getQuantityString(
                        R.plurals.notes_quantity,
                        content.notes.size,
                        content.notes.size,
                    ),
                )
            },
            navigation = MainState.NavigationComponent.Visible(),
            bottomAppBar = MainState.BottomAppBarComponent.Gone,
            floatingActionButton = MainState.FabComponent.Gone,
        )
    }
}
