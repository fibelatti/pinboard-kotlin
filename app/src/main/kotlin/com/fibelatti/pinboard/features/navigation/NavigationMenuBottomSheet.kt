package com.fibelatti.pinboard.features.navigation

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.core.net.toUri
import com.fibelatti.core.android.extension.shareText
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.extension.ErrorReportEntryPoint
import com.fibelatti.pinboard.features.appstate.Action
import com.fibelatti.pinboard.features.licenses.OssLicensesActivity
import com.fibelatti.ui.components.AppBottomSheet
import com.fibelatti.ui.components.AppSheetState
import com.fibelatti.ui.components.hideBottomSheet
import dagger.hilt.android.EntryPointAccessors

@Composable
fun NavigationMenuBottomSheet(
    sheetState: AppSheetState,
    appMode: AppMode,
    onNavOptionClicked: (Action) -> Unit,
) {
    val activity = LocalActivity.current
    val localUriHandler = LocalUriHandler.current

    AppBottomSheet(
        sheetState = sheetState,
    ) {
        NavigationMenuContent(
            appMode = appMode,
            onNavOptionClicked = { action ->
                onNavOptionClicked(action)
                sheetState.hideBottomSheet()
            },
            onSendFeedbackClicked = {
                activity?.showFeedbackPrompt()
                sheetState.hideBottomSheet()
            },
            onWriteReviewClicked = {
                localUriHandler.openUri(NavigationMenu.APP_URL)
                sheetState.hideBottomSheet()
            },
            onShareClicked = {
                activity?.shareText(
                    title = R.string.share_title,
                    text = activity.getString(R.string.share_text, NavigationMenu.APP_URL),
                )
                sheetState.hideBottomSheet()
            },
            onPrivacyPolicyClicked = {
                localUriHandler.openUri(NavigationMenu.PRIVACY_POLICY_URL)
                sheetState.hideBottomSheet()
            },
            onLicensesClicked = {
                activity?.startActivity(Intent(activity, OssLicensesActivity::class.java))
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
