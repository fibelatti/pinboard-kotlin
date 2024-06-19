package com.fibelatti.pinboard.features.notes.presentation

import android.os.Bundle
import android.view.View
import com.fibelatti.core.android.extension.navigateBack
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class NoteListFragment : BaseFragment() {

    private val appStateViewModel: AppStateViewModel by activityViewModel()
    private val mainViewModel: MainViewModel by activityViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setThemedContent {
            NoteListScreen(
                appStateViewModel = appStateViewModel,
                mainViewModel = mainViewModel,
                onBackPressed = { navigateBack() },
                onError = ::handleError,
            )
        }
    }

    companion object {

        @JvmStatic
        val TAG: String = "NoteListFragment"
    }
}
