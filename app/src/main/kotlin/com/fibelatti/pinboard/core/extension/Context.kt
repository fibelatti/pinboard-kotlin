package com.fibelatti.pinboard.core.extension

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppModeProvider
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

/**
 * The system permission dialog talks about nearby devices, which gives no hint that the same
 * permission is required to access a server on the same network. This explains why it is about to
 * be shown before requesting it via [onConfirm].
 */
fun Context.showLocalNetworkAccessDialog(onConfirm: () -> Unit) {
    materialAlertDialogBuilder().apply {
        setMessage(R.string.auth_linkding_missing_local_network_permission)
        setPositiveButton(R.string.hint_ok) { dialog, _ ->
            dialog?.dismiss()
            onConfirm()
        }
    }.applySecureFlag().show()
}

/**
 * Shown once the local network permission was denied for good, when the system will no longer prompt
 * for it. The only way to grant it is from the settings app, which [openAppSettings] opens.
 */
fun Context.showLocalNetworkAccessSettingsDialog() {
    materialAlertDialogBuilder().apply {
        setMessage(R.string.auth_linkding_missing_local_network_permission_settings)
        setPositiveButton(R.string.hint_open_settings) { dialog, _ ->
            dialog?.dismiss()
            openAppSettings()
        }
        setNegativeButton(R.string.hint_cancel) { dialog, _ -> dialog?.dismiss() }
    }.applySecureFlag().show()
}

fun Context.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        "package:$packageName".toUri(),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
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

    materialAlertDialogBuilder().apply {
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

            val emailIntent = Intent(Intent.ACTION_SENDTO, "mailto:".toUri()).apply {
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
