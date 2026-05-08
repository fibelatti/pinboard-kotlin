package com.fibelatti.pinboard.core.android.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.extension.isServerException
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.core.extension.showErrorReportDialog

@Composable
fun ErrorHandlerEffect(
    error: Throwable?,
    handler: () -> Unit,
    postAction: () -> Unit = {},
) {
    val localView = LocalView.current
    val localContext = LocalContext.current

    val composedAction by rememberUpdatedState {
        handler()
        postAction()
    }

    SideEffect(error) {
        val current = error ?: return@SideEffect

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
