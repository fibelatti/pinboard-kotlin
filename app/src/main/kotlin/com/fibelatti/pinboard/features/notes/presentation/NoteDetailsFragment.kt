package com.fibelatti.pinboard.features.notes.presentation

import android.os.Bundle
import android.view.View
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.setThemedContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NoteDetailsFragment @Inject constructor() : BaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setThemedContent {
            NoteDetailsScreen()
        }
    }

    companion object {

        @JvmStatic
        val TAG: String = "NoteDetailsFragment"
    }
}
