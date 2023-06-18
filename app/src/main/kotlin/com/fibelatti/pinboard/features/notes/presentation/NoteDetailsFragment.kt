package com.fibelatti.pinboard.features.notes.presentation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.ui.foundation.stableListOf
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class NoteDetailsFragment @Inject constructor() : BaseFragment() {

    private val appStateViewModel: AppStateViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val noteDetailsViewModel: NoteDetailsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setThemedContent {
            NoteDetailsScreen(
                appStateViewModel = appStateViewModel,
                noteDetailsViewModel = noteDetailsViewModel,
            )
        }

        mainViewModel.updateState { currentState ->
            if (currentState.multiPanelEnabled) {
                currentState.copy(
                    sidePanelAppBar = MainState.SidePanelAppBarComponent.Visible(
                        id = ACTION_ID,
                        menuItems = stableListOf(MainState.MenuItemComponent.CloseSidePanel),
                    ),
                )
            } else {
                currentState.copy(
                    title = MainState.TitleComponent.Gone,
                    subtitle = MainState.TitleComponent.Gone,
                    navigation = MainState.NavigationComponent.Visible(ACTION_ID),
                    bottomAppBar = MainState.BottomAppBarComponent.Gone,
                    floatingActionButton = MainState.FabComponent.Gone,
                )
            }
        }

        setupViewModels()
    }

    private fun setupViewModels() {
        mainViewModel.navigationClicks(ACTION_ID)
            .onEach { navigateBack() }
            .launchInAndFlowWith(viewLifecycleOwner)
        mainViewModel.menuItemClicks(ACTION_ID)
            .onEach { (menuItem, _) ->
                if (menuItem is MainState.MenuItemComponent.CloseSidePanel) {
                    navigateBack()
                }
            }
            .launchInAndFlowWith(viewLifecycleOwner)

        noteDetailsViewModel.error
            .onEach { throwable -> handleError(throwable, noteDetailsViewModel::errorHandled) }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    companion object {

        @JvmStatic
        val TAG: String = "NoteDetailsFragment"

        private val ACTION_ID = UUID.randomUUID().toString()
    }
}
