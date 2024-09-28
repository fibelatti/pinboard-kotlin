package com.fibelatti.pinboard.core.extension

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.getSystemService
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppModeProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.io.PrintWriter
import java.io.StringWriter

fun Context.copyToClipboard(
    label: String,
    text: String,
) {
    getSystemService<ClipboardManager>()?.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(this, R.string.feedback_copied_to_clipboard, Toast.LENGTH_SHORT).show()
}

fun Context.showErrorReportDialog(
    throwable: Throwable,
    title: String = "",
    altMessage: String = "",
    postAction: () -> Unit = {},
) {
    val entryPoint = EntryPointAccessors.fromApplication(
        applicationContext,
        ErrorReportEntryPoint::class.java,
    )
    val appModeProvider = entryPoint.appModeProvider()

    MaterialAlertDialogBuilder(this).apply {
        if (title.isNotBlank()) {
            setTitle(title)
        }

        setMessage(altMessage.ifEmpty { getString(R.string.error_report_rationale) })

        setPositiveButton(R.string.error_report) { dialog, _ ->
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))

            val emailBody = StringBuilder().apply {
                appendLine("Android Version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
                appendLine("Current Service: ${appModeProvider.appMode.value}")
                appendLine("---")
                appendLine("This error just happened to me:")
                appendLine()
                append(sw.toString().replace(regex = "&?auth_token=[^&]*".toRegex(), replacement = ""))
            }

            val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
                putExtra(Intent.EXTRA_EMAIL, arrayOf("appsupport@fibelatti.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Pinkt (${BuildConfig.VERSION_NAME}) — Error Report")
                putExtra(Intent.EXTRA_TEXT, emailBody.toString())
            }
            startActivity(Intent.createChooser(emailIntent, getString(R.string.error_send_email)))
            dialog?.dismiss()
            postAction()
        }
        setNegativeButton(R.string.error_ignore) { dialog, _ ->
            dialog?.dismiss()
            postAction()
        }
        setOnDismissListener {
            postAction()
        }
    }.applySecureFlag().show()
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ErrorReportEntryPoint {

    fun appModeProvider(): AppModeProvider
}
