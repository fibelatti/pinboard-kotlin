package com.fibelatti.pinboard.features.notes.presentation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NoteDetailsFragment @Inject constructor() : BaseFragment() {

    private val appStateViewModel: AppStateViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setThemedContent {
            NoteDetailsScreen(
                appStateViewModel = appStateViewModel,
                mainViewModel = mainViewModel,
                onBackPressed = { navigateBack() },
                onError = ::handleError,
            )
        }
    }

    companion object {

        @JvmStatic
        val TAG: String = "NoteDetailsFragment"
    }
}
