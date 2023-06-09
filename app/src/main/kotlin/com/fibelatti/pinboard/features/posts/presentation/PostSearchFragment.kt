package com.fibelatti.pinboard.features.posts.presentation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.ClearSearch
import com.fibelatti.pinboard.features.appstate.Search
import com.fibelatti.pinboard.features.tags.presentation.TagsViewModel
import com.fibelatti.ui.foundation.toStableList
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class PostSearchFragment @Inject constructor() : BaseFragment() {

    private val appStateViewModel: AppStateViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val tagsViewModel: TagsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setThemedContent {
            SearchBookmarksScreen(
                appStateViewModel = appStateViewModel,
                tagsViewModel = tagsViewModel,
            )
        }

        mainViewModel.updateState { currentState ->
            currentState.copy(
                title = MainState.TitleComponent.Visible(getString(R.string.search_title)),
                subtitle = MainState.TitleComponent.Gone,
                navigation = MainState.NavigationComponent.Visible(ACTION_ID),
                bottomAppBar = MainState.BottomAppBarComponent.Visible(
                    id = ACTION_ID,
                    menuItems = listOf(MainState.MenuItemComponent.ClearSearch).toStableList(),
                    navigationIcon = null,
                ),
                floatingActionButton = MainState.FabComponent.Visible(ACTION_ID, R.drawable.ic_search),
            )
        }

        setupViewModels()
    }

    override fun onDestroyView() {
        requireView().hideKeyboard()
        super.onDestroyView()
    }

    private fun setupViewModels() {
        mainViewModel.navigationClicks(ACTION_ID)
            .onEach { navigateBack() }
            .launchInAndFlowWith(viewLifecycleOwner)

        mainViewModel.menuItemClicks(ACTION_ID)
            .onEach { (menuItem, _) ->
                if (menuItem is MainState.MenuItemComponent.ClearSearch) {
                    appStateViewModel.runAction(ClearSearch)
                }
            }
            .launchInAndFlowWith(viewLifecycleOwner)

        mainViewModel.fabClicks(ACTION_ID)
            .onEach { appStateViewModel.runAction(Search) }
            .launchInAndFlowWith(viewLifecycleOwner)

        tagsViewModel.error
            .onEach { throwable -> handleError(throwable, tagsViewModel::errorHandled) }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    companion object {

        @JvmStatic
        val TAG: String = "PostSearchFragment"

        private val ACTION_ID = UUID.randomUUID().toString()
    }
}
