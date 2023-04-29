package com.fibelatti.pinboard.features.user.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.android.base.sendErrorReport
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.core.extension.setThemedContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class AuthFragment @Inject constructor() : BaseFragment() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(inflater.context)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setThemedContent {
            AuthScreen()
        }

        authViewModel.error
            .onEach { throwable -> handleError(throwable, authViewModel::errorHandled) }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    override fun handleError(error: Throwable?, postAction: () -> Unit) {
        error ?: return

        if (BuildConfig.DEBUG) {
            error.printStackTrace()
        }

        activity?.sendErrorReport(
            throwable = error,
            altMessage = getString(R.string.auth_error),
            postAction = postAction,
        )
    }

    companion object {

        @JvmStatic
        val TAG: String = "AuthFragment"
    }
}
