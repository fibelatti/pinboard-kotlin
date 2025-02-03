package com.fibelatti.pinboard.core.android.base

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.ErrorReportEntryPoint
import com.fibelatti.pinboard.core.di.modules.ActivityEntryPoint
import com.fibelatti.pinboard.core.extension.applySecureFlag
import com.fibelatti.pinboard.core.extension.isServerException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.EntryPointAccessors
import java.io.PrintWriter
import java.io.StringWriter

abstract class BaseActivity : AppCompatActivity() {

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        val entryPoint = EntryPointAccessors.fromActivity(this, ActivityEntryPoint::class.java)
        supportFragmentManager.fragmentFactory = entryPoint.getFragmentFactory()

        super.onCreate(savedInstanceState)
    }

    open fun handleError(error: Throwable?, postAction: () -> Unit = {}) {
        error ?: return

        if (BuildConfig.DEBUG) {
            error.printStackTrace()
        }

        if (error.isServerException()) {
            Toast.makeText(this, R.string.server_error, Toast.LENGTH_LONG).show()
            postAction()
        } else {
            sendErrorReport(error, postAction = postAction)
        }
    }
}

fun FragmentActivity.sendErrorReport(
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
        val message = altMessage.ifEmpty { getString(R.string.error_report_rationale) }

        if (title.isNotBlank()) {
            setTitle(title)
        }

        setMessage(message)
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
