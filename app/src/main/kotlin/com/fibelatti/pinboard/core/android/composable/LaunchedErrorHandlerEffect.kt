package com.fibelatti.pinboard.core.android.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.core.extension.isServerException
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.core.extension.showErrorReportDialog

@Composable
fun LaunchedErrorHandlerEffect(
    viewModel: BaseViewModel,
    postAction: () -> Unit = {},
) {
    val localView = LocalView.current
    val localContext = LocalContext.current

    val composedAction by rememberUpdatedState {
        viewModel.errorHandled()
        postAction()
    }

    val error by viewModel.error.collectAsStateWithLifecycle()

    LaunchedEffect(error) {
        val current = error ?: return@LaunchedEffect

        if (BuildConfig.DEBUG) {
            current.printStackTrace()
        }

        if (current.isServerException()) {
            localView.showBanner(messageRes = R.string.server_error)
            composedAction()
        } else {
            localContext.showErrorReportDialog(throwable = current, postAction = composedAction)
        }
    }
}
