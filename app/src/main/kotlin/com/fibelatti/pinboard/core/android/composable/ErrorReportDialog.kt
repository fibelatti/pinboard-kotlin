@file:Suppress("UNUSED")

package com.fibelatti.pinboard.core.android.composable

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppModeProvider
import com.fibelatti.pinboard.core.extension.isServerException
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.io.PrintWriter
import java.io.StringWriter

@Composable
fun ErrorReport(
    throwable: Throwable,
    modifier: Modifier = Modifier,
    altMessage: String = "",
    postAction: () -> Unit = {},
) {
    if (BuildConfig.DEBUG) {
        throwable.printStackTrace()
    }

    if (throwable.isServerException()) {
        Toast.makeText(LocalContext.current, R.string.server_timeout_error, Toast.LENGTH_LONG).show()
        postAction()
    } else {
        ErrorReportDialog(throwable, modifier, altMessage, postAction)
    }
}

@Composable
fun ErrorReportDialog(
    throwable: Throwable,
    modifier: Modifier = Modifier,
    altMessage: String = "",
    postAction: () -> Unit = {},
) {
    val openDialog = rememberSaveable { mutableStateOf(true) }

    if (!openDialog.value) return

    val dismissDialog = {
        openDialog.value = false
        postAction()
    }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = dismissDialog,
        confirmButton = {
            val chooserTitle = stringResource(R.string.error_send_email)

            Button(
                onClick = {
                    val entryPoint = EntryPointAccessors.fromApplication(
                        context.applicationContext,
                        ErrorReportEntryPoint::class.java,
                    )
                    val appModeProvider = entryPoint.appModeProvider()

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

                    context.startActivity(Intent.createChooser(emailIntent, chooserTitle))

                    dismissDialog()
                },
            ) {
                Text(text = stringResource(id = R.string.error_report))
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = dismissDialog) {
                Text(text = stringResource(id = R.string.error_ignore))
            }
        },
        text = {
            Text(text = altMessage.ifEmpty { stringResource(R.string.error_report_rationale) })
        },
    )
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ErrorReportEntryPoint {

    fun appModeProvider(): AppModeProvider
}

@Composable
@ThemePreviews
private fun ErrorReportDialogPreview() {
    ExtendedTheme {
        ErrorReportDialog(throwable = Exception())
    }
}
