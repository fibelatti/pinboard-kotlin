package com.fibelatti.pinboard.features.user.presentation

import android.os.Bundle
import android.view.View
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.setThemedContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UserPreferencesFragment @Inject constructor() : BaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setThemedContent {
            UserPreferencesScreen()
        }
    }

    companion object {

        @JvmStatic
        val TAG: String = "UserPreferencesFragment"
    }
}
