@file:Suppress("UNUSED")

package com.fibelatti.pinboard.core.android.composable

import android.content.Intent
import android.net.Uri
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
import com.fibelatti.bookmarking.core.extension.isServerException
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
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

@Composable
@ThemePreviews
private fun ErrorReportDialogPreview() {
    ExtendedTheme {
        ErrorReportDialog(throwable = Exception())
    }
}
