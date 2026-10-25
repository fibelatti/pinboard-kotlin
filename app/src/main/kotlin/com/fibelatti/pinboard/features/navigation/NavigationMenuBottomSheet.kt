package com.fibelatti.pinboard.features.navigation

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import com.fibelatti.pinboard.features.appstate.Action
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
        val appState: AppState by mainViewModel.appState.collectAsStateWithLifecycle()

        ExportBookmarksEffects(
            state = exportBookmarksViewModel.state,
            onDestinationSelect = exportBookmarksViewModel::exportFile,
            onMessageDismiss = exportBookmarksViewModel::messageHandled,
        )

        val onNavigationMenuAction: (NavigationMenuAction) -> Unit = rememberNavigationMenuActionHandler(
            onNavigate = mainViewModel::runAction,
            onExportBookmarks = { exportBookmarksViewModel.createBackup(appState.appMode) },
        )

        NavigationMenuContent(
            appMode = appState.appMode,
            onActionClick = { action ->
                onNavigationMenuAction(action)

                // The export flow is hosted by the sheet content, so it must stay open until the export completes
                if (action != NavigationMenuAction.ExportBookmarks) {
                    sheetState.hideBottomSheet()
                }
            },
        )
    }
}

@Composable
fun rememberNavigationMenuActionHandler(
    onNavigate: (Action) -> Unit,
    onExportBookmarks: () -> Unit,
): (NavigationMenuAction) -> Unit {
    val onNavigateState by rememberUpdatedState(onNavigate)
    val onExportBookmarksState by rememberUpdatedState(onExportBookmarks)

    val localActivity: Activity? = LocalActivity.current
    val localUriHandler: UriHandler = LocalUriHandler.current

    return remember(localActivity, localUriHandler) {
        { action: NavigationMenuAction ->
            when (action) {
                is NavigationMenuAction.Navigate -> {
                    onNavigateState(action.action)
                }

                NavigationMenuAction.ExportBookmarks -> {
                    onExportBookmarksState()
                }

                NavigationMenuAction.SendFeedback -> {
                    localActivity?.showFeedbackPrompt()
                }

                NavigationMenuAction.WriteReview -> {
                    localUriHandler.openUri(NavigationMenu.APP_URL)
                }

                NavigationMenuAction.ShareApp -> {
                    localActivity?.shareText(
                        title = R.string.share_title,
                        text = localActivity.getString(R.string.share_text, NavigationMenu.APP_URL),
                    )
                }

                NavigationMenuAction.ViewPrivacyPolicy -> {
                    localUriHandler.openUri(NavigationMenu.PRIVACY_POLICY_URL)
                }

                NavigationMenuAction.ViewLicenses -> {
                    localActivity?.startActivity(Intent(localActivity, OssLicensesActivity::class.java))
                }
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
