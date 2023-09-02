package com.fibelatti.pinboard.core.android.base

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.di.modules.ActivityEntryPoint
import com.fibelatti.pinboard.core.extension.applySecureFlag
import com.fibelatti.pinboard.core.extension.isServerException
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.EntryPointAccessors
import java.io.PrintWriter
import java.io.StringWriter
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity() {

    @Inject
    lateinit var userRepository: UserRepository

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
            Toast.makeText(this, R.string.server_timeout_error, Toast.LENGTH_LONG).show()
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
    MaterialAlertDialogBuilder(this).apply {
        val message = altMessage.ifEmpty { getString(R.string.error_report_rationale) }

        if (title.isNotBlank()) {
            setTitle(title)
        }

        setMessage(message)
        setPositiveButton(R.string.error_report) { dialog, _ ->
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))

            val emailBody = "Hi, can you please look into this report?" +
                "\n\nMy app version is ${BuildConfig.VERSION_NAME}" +
                "\n\n$sw"
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("fibelatti+dev@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Pinkt - Error Report")
                putExtra(Intent.EXTRA_TEXT, emailBody)
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
