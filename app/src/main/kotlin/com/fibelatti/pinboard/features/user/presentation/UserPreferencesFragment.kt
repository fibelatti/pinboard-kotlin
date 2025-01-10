package com.fibelatti.pinboard.features.user.presentation

import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.fibelatti.core.android.extension.hideKeyboard
import com.fibelatti.core.android.extension.navigateBack
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserPreferencesFragment @Inject constructor() : BaseFragment() {

    private val appStateViewModel: AppStateViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setThemedContent {
            UserPreferencesScreen(
                appStateViewModel = appStateViewModel,
                onDynamicColorChange = ::restartActivity,
                onDisableScreenshotsChange = ::restartActivity,
            )
        }

        mainViewModel.updateState { currentState ->
            currentState.copy(
                title = MainState.TitleComponent.Visible(getString(R.string.user_preferences_title)),
                subtitle = MainState.TitleComponent.Gone,
                navigation = MainState.NavigationComponent.Visible(ACTION_ID),
                bottomAppBar = MainState.BottomAppBarComponent.Gone,
                floatingActionButton = MainState.FabComponent.Gone,
            )
        }
        mainViewModel.navigationClicks(ACTION_ID)
            .onEach { navigateBack() }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    private fun restartActivity() {
        viewLifecycleOwner.lifecycleScope.launch {
            delay(300L) // Wait until the switch is done animating
            ActivityCompat.recreate(requireActivity())
        }
    }

    override fun onDestroyView() {
        requireView().hideKeyboard()
        super.onDestroyView()
    }

    companion object {

        @JvmStatic
        val TAG: String = "UserPreferencesFragment"

        private val ACTION_ID = UUID.randomUUID().toString()
    }
}
