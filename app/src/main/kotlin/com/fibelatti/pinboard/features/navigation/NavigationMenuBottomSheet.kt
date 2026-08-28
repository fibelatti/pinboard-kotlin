package com.fibelatti.pinboard.features.navigation

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.core.android.extension.shareText
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.extension.ErrorReportEntryPoint
import com.fibelatti.pinboard.features.appstate.AppState
import com.fibelatti.pinboard.features.export.ExportBookmarksEffects
import com.fibelatti.pinboard.features.export.ExportBookmarksViewModel
import com.fibelatti.pinboard.features.licenses.OssLicensesActivity
import com.fibelatti.pinboard.features.main.MainViewModel
import com.fibelatti.ui.components.AppBottomSheet
import com.fibelatti.ui.components.AppSheetState
import dagger.hilt.android.EntryPointAccessors

@Composable
fun NavigationMenuBottomSheet(
    sheetState: AppSheetState,
    modifier: Modifier = Modifier,
    exportBookmarksViewModel: ExportBookmarksViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    AppBottomSheet(
        sheetState = sheetState,
        modifier = modifier,
    ) {
        val localActivity: Activity? = LocalActivity.current
        val localUriHandler: UriHandler = LocalUriHandler.current

        val appState: AppState by mainViewModel.appState.collectAsStateWithLifecycle()

        ExportBookmarksEffects(
            state = exportBookmarksViewModel.state,
            onDestinationSelect = exportBookmarksViewModel::exportFile,
            onMessageDismiss = exportBookmarksViewModel::messageHandled,
        )

        NavigationMenuContent(
            appMode = appState.appMode,
            onNavOptionClick = { action ->
                mainViewModel.runAction(action)
                sheetState.hideBottomSheet()
            },
            onExportClick = { exportBookmarksViewModel.createBackup(appMode = appState.appMode) },
            onSendFeedbackClick = {
                localActivity?.showFeedbackPrompt()
                sheetState.hideBottomSheet()
            },
            onWriteReviewClick = {
                localUriHandler.openUri(NavigationMenu.APP_URL)
                sheetState.hideBottomSheet()
            },
            onShareClick = {
                localActivity?.shareText(
                    title = R.string.share_title,
                    text = localActivity.getString(R.string.share_text, NavigationMenu.APP_URL),
                )
                sheetState.hideBottomSheet()
            },
            onPrivacyPolicyClick = {
                localUriHandler.openUri(NavigationMenu.PRIVACY_POLICY_URL)
                sheetState.hideBottomSheet()
            },
            onLicensesClick = {
                localActivity?.startActivity(Intent(localActivity, OssLicensesActivity::class.java))
                sheetState.hideBottomSheet()
            },
        )
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
