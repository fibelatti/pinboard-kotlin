package com.fibelatti.pinboard.features.navigation

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.fibelatti.core.android.extension.shareText
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.ComposeBottomSheetDialog
import com.fibelatti.pinboard.core.extension.ErrorReportEntryPoint
import com.fibelatti.pinboard.features.licenses.OssLicensesActivity
import dagger.hilt.android.EntryPointAccessors

object NavigationMenu {

    private const val APP_URL = "https://play.google.com/store/apps/details?id=com.fibelatti.pinboard"
    private const val PRIVACY_POLICY_URL = "https://fibelatti.com/privacy-policy/pinkt"

    fun show(activity: AppCompatActivity) {
        ComposeBottomSheetDialog(activity) {
            NavigationMenuScreen(
                onSendFeedbackClicked = {
                    sendFeedback(activity)
                },
                onWriteReviewClicked = {
                    activity.startActivity(Intent(Intent.ACTION_VIEW, APP_URL.toUri()))
                },
                onShareClicked = {
                    activity.shareText(
                        title = R.string.share_title,
                        text = context.getString(R.string.share_text, APP_URL),
                    )
                },
                onPrivacyPolicyClicked = {
                    activity.startActivity(Intent(Intent.ACTION_VIEW, PRIVACY_POLICY_URL.toUri()))
                },
                onLicensesClicked = {
                    activity.startActivity(Intent(activity, OssLicensesActivity::class.java))
                },
                onOptionSelected = ::dismiss,
            )
        }.show()
    }

    private fun sendFeedback(activity: AppCompatActivity) {
        val entryPoint = EntryPointAccessors.fromApplication(
            activity.applicationContext,
            ErrorReportEntryPoint::class.java,
        )
        val appModeProvider = entryPoint.appModeProvider()

        val emailBody = StringBuilder().apply {
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
            putExtra(Intent.EXTRA_TEXT, emailBody.toString())
        }

        activity.startActivity(Intent.createChooser(emailIntent, activity.getString(R.string.error_send_email)))
    }
}
