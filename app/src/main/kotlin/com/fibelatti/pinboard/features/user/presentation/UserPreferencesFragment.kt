package com.fibelatti.pinboard.features.user.presentation

import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.di.MainVariant
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class UserPreferencesFragment @Inject constructor(
    @MainVariant private val mainVariant: Boolean,
) : BaseFragment() {

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setThemedContent {
            UserPreferencesScreen(
                mainVariant = mainVariant,
                onDynamicColorChange = {
                    viewLifecycleOwner.lifecycleScope.launch {
                        delay(300L) // Wait until the switch is done animating
                        ActivityCompat.recreate(requireActivity())
                    }
                },
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

    override fun onDestroyView() {
        requireView().hideKeyboard()
        super.onDestroyView()
    }

    override fun onDestroy() {
        activity?.supportFragmentManager?.setFragmentResult(TAG, bundleOf())
        super.onDestroy()
    }

    companion object {

        @JvmStatic
        val TAG: String = "UserPreferencesFragment"

        private val ACTION_ID = UUID.randomUUID().toString()
    }
}
