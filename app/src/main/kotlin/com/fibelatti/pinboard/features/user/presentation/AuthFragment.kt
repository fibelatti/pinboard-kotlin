package com.fibelatti.pinboard.features.user.presentation

import android.os.Bundle
import android.view.View
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.android.base.sendErrorReport
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.core.extension.setThemedContent
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel

class AuthFragment : BaseFragment() {

    private val authViewModel: AuthViewModel by viewModel()

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
            title = getString(R.string.auth_error_title),
            altMessage = getString(R.string.auth_error),
            postAction = postAction,
        )
    }

    companion object {

        @JvmStatic
        val TAG: String = "AuthFragment"
    }
}
