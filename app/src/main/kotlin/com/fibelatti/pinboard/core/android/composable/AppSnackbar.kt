package com.fibelatti.pinboard.core.android.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.icons.AppIcons
import com.fibelatti.pinboard.core.android.icons.Close
import com.fibelatti.ui.preview.PreviewAccessibility
import com.fibelatti.ui.preview.PreviewThemesAndColors
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun AppSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
) {
    Snackbar(
        modifier = modifier,
        dismissAction = if (snackbarData.visuals.withDismissAction) {
            {
                IconButton(onClick = snackbarData::dismiss) {
                    Icon(
                        imageVector = AppIcons.Close,
                        contentDescription = stringResource(id = R.string.cd_dismiss_message),
                    )
                }
            }
        } else {
            null
        },
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(text = AnnotatedString.fromHtml(snackbarData.visuals.message))
    }
}

private class PreviewSnackbarData(
    message: String,
    withDismissAction: Boolean = false,
) : SnackbarData {

    override val visuals: SnackbarVisuals = object : SnackbarVisuals {
        override val message: String = message
        override val actionLabel: String? = null
        override val withDismissAction: Boolean = withDismissAction
        override val duration: SnackbarDuration = SnackbarDuration.Short
    }

    override fun performAction() = Unit

    override fun dismiss() = Unit
}

@Composable
@PreviewThemesAndColors
@PreviewAccessibility
private fun AppSnackbarPreview() {
    ExtendedTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            AppSnackbar(snackbarData = PreviewSnackbarData(message = "Sample message"))
        }
    }
}

@Composable
@PreviewThemesAndColors
@PreviewAccessibility
private fun AppSnackbarWithDismissActionPreview() {
    ExtendedTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            AppSnackbar(
                snackbarData = PreviewSnackbarData(
                    message = "<b>Sample message.</b><br />With <i>markup</i>.",
                    withDismissAction = true,
                ),
            )
        }
    }
}
