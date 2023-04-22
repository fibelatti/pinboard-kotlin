package com.fibelatti.pinboard.features.notes.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.android.composable.AppTheme
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class NoteListFragment @Inject constructor() : BaseFragment() {

    private val appStateViewModel: AppStateViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val noteListViewModel: NoteListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(inflater.context)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (view as ComposeView).setContent {
            AppTheme {
                NoteListScreen(
                    appStateViewModel = appStateViewModel,
                    noteListViewModel = noteListViewModel,
                )
            }
        }

        setupViewModels()
    }

    private fun setupViewModels() {
        appStateViewModel.noteListContent
            .onEach { content ->
                mainViewModel.updateState { currentState ->
                    currentState.copy(
                        title = MainState.TitleComponent.Visible(getString(R.string.notes_title)),
                        subtitle = when {
                            content.shouldLoad -> MainState.TitleComponent.Gone
                            content.notes.isEmpty() -> MainState.TitleComponent.Gone
                            else -> MainState.TitleComponent.Visible(
                                resources.getQuantityString(
                                    R.plurals.notes_quantity,
                                    content.notes.size,
                                    content.notes.size,
                                ),
                            )
                        },
                        navigation = MainState.NavigationComponent.Visible(ACTION_ID),
                        bottomAppBar = MainState.BottomAppBarComponent.Gone,
                        floatingActionButton = MainState.FabComponent.Gone,
                    )
                }
            }
            .launchInAndFlowWith(viewLifecycleOwner)

        mainViewModel.navigationClicks(ACTION_ID)
            .onEach { navigateBack() }
            .launchInAndFlowWith(viewLifecycleOwner)

        noteListViewModel.error
            .onEach { throwable -> handleError(throwable, noteListViewModel::errorHandled) }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    companion object {

        @JvmStatic
        val TAG: String = "NoteListFragment"

        private val ACTION_ID = UUID.randomUUID().toString()
    }
}
