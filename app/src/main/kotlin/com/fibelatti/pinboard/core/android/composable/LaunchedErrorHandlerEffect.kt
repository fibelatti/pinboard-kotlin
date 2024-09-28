package com.fibelatti.pinboard.core.android.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.core.extension.isServerException
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.core.extension.showErrorReportDialog
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach

@Composable
fun LaunchedErrorHandlerEffect(
    viewModel: BaseViewModel,
    postAction: () -> Unit = {},
) {
    val localView = LocalView.current
    val localContext = LocalContext.current
    val localLifecycleOwner = LocalLifecycleOwner.current

    val composedAction by rememberUpdatedState {
        viewModel.errorHandled()
        postAction()
    }

    LaunchedEffect(viewModel) {
        viewModel.error
            .filterNotNull()
            .onEach { throwable ->
                if (BuildConfig.DEBUG) {
                    throwable.printStackTrace()
                }

                if (throwable.isServerException()) {
                    localView.showBanner(message = localContext.getString(R.string.server_error))
                    composedAction()
                } else {
                    localContext.showErrorReportDialog(throwable = throwable, postAction = composedAction)
                }
            }
            .launchInAndFlowWith(localLifecycleOwner)
    }
}
