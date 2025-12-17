package com.fibelatti.pinboard.features.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.core.android.extension.shareText
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.extension.ErrorReportEntryPoint
import com.fibelatti.pinboard.features.appstate.AppState
import com.fibelatti.pinboard.features.licenses.OssLicensesActivity
import com.fibelatti.pinboard.features.main.MainViewModel
import com.fibelatti.ui.components.AppBottomSheet
import com.fibelatti.ui.components.AppSheetState
import com.fibelatti.ui.components.hideBottomSheet
import dagger.hilt.android.EntryPointAccessors

@Composable
fun NavigationMenuBottomSheet(
    sheetState: AppSheetState,
    modifier: Modifier = Modifier,
    navigationMenuViewModel: NavigationMenuViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    AppBottomSheet(
        sheetState = sheetState,
        modifier = modifier,
    ) {
        val localContext: Context = LocalContext.current
        val localActivity: Activity? = LocalActivity.current
        val localUriHandler: UriHandler = LocalUriHandler.current

        val appState: AppState by mainViewModel.appState.collectAsStateWithLifecycle()
        val state: NavigationMenuViewModel.State = navigationMenuViewModel.state

        val saveFileLauncher: ManagedActivityResultLauncher<Intent, ActivityResult> = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result: ActivityResult ->
            navigationMenuViewModel.exportFile(destinationUri = result.data?.data)
        }

        LaunchedEffect(state.preparedFile) {
            if (state.preparedFile != null) {
                val intent: Intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/html"
                    putExtra(Intent.EXTRA_TITLE, state.preparedFile.name)
                }
                saveFileLauncher.launch(intent)
            }
        }

        LaunchedEffect(state.messages) {
            state.messages.firstOrNull()?.let { message ->
                val messageRes = when (message) {
                    NavigationMenuViewModel.State.Message.EXPORT_SUCCESS -> R.string.export_feedback_success
                    NavigationMenuViewModel.State.Message.EXPORT_FAILURE -> R.string.export_feedback_failure
                }

                Toast.makeText(localContext, messageRes, Toast.LENGTH_SHORT).show()
                navigationMenuViewModel.messageHandled(message = message)
            }
        }

        NavigationMenuContent(
            appMode = appState.appMode,
            onNavOptionClicked = { action ->
                mainViewModel.runAction(action)
                sheetState.hideBottomSheet()
            },
            onExportClicked = navigationMenuViewModel::createBackup,
            onSendFeedbackClicked = {
                localActivity?.showFeedbackPrompt()
                sheetState.hideBottomSheet()
            },
            onWriteReviewClicked = {
                localUriHandler.openUri(NavigationMenu.APP_URL)
                sheetState.hideBottomSheet()
            },
            onShareClicked = {
                localActivity?.shareText(
                    title = R.string.share_title,
                    text = localActivity.getString(R.string.share_text, NavigationMenu.APP_URL),
                )
                sheetState.hideBottomSheet()
            },
            onPrivacyPolicyClicked = {
                localUriHandler.openUri(NavigationMenu.PRIVACY_POLICY_URL)
                sheetState.hideBottomSheet()
            },
            onLicensesClicked = {
                localActivity?.startActivity(Intent(localActivity, OssLicensesActivity::class.java))
                sheetState.hideBottomSheet()
            },
        )

        if (state.isProcessing) {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                ),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private object NavigationMenu {

    const val APP_URL = "https://play.google.com/store/apps/details?id=com.fibelatti.pinboard"
    const val PRIVACY_POLICY_URL = "https://fibelatti.com/privacy-policy/pinkt"
}

private fun Activity.showFeedbackPrompt() {
    val entryPoint = EntryPointAccessors.fromApplication(
        applicationContext,
        ErrorReportEntryPoint::class.java,
    )
    val appModeProvider = entryPoint.appModeProvider()

    val emailBody = buildString {
        appendLine("Android Version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
        appendLine("Current Service: ${appModeProvider.appMode.value}")
        appendLine("---")
        appendLine()
    }

    val emailIntent = Intent(Intent.ACTION_SENDTO, "mailto:".toUri()).apply {
        putExtra(Intent.EXTRA_EMAIL, arrayOf("appsupport@fibelatti.com"))
        putExtra(
            Intent.EXTRA_SUBJECT,
            "Pinkt (${BuildConfig.VERSION_NAME}) — Feature request / Bug report",
        )
        putExtra(Intent.EXTRA_TEXT, emailBody)
    }

    startActivity(Intent.createChooser(emailIntent, getString(R.string.error_send_email)))
}
