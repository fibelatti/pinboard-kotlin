package com.fibelatti.pinboard.features.posts.presentation

import android.os.Bundle
import android.view.View
import com.fibelatti.bookmarking.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.features.MainViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class PostSearchFragment : BaseFragment() {

    private val appStateViewModel: AppStateViewModel by activityViewModel()
    private val mainViewModel: MainViewModel by activityViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setThemedContent {
            SearchBookmarksScreen(
                appStateViewModel = appStateViewModel,
                mainViewModel = mainViewModel,
                onError = ::handleError,
            )
        }
    }

    companion object {

        @JvmStatic
        val TAG: String = "PostSearchFragment"
    }
}
