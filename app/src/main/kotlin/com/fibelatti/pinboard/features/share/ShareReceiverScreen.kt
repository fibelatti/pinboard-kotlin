package com.fibelatti.pinboard.features.share

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.core.functional.ScreenState
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.extension.isServerException
import com.fibelatti.pinboard.core.extension.showErrorReportDialog
import com.fibelatti.pinboard.features.posts.domain.usecase.InvalidUrlException
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import io.ktor.client.plugins.ResponseException

@Composable
fun ShareReceiverScreen(
    onEdit: () -> Unit,
    onSaved: () -> Unit,
    errorDialogAction: () -> Unit,
    modifier: Modifier = Modifier,
    shareReceiverViewModel: ShareReceiverViewModel = hiltViewModel(),
) {
    val state by shareReceiverViewModel.screenState.collectAsStateWithLifecycle()

    val icon = when (state) {
        is ScreenState.Error -> painterResource(id = R.drawable.ic_url_saved_error)
        is ScreenState.Loaded -> painterResource(id = R.drawable.ic_url_saved)
        else -> painterResource(id = R.drawable.ic_url_saving)
    }

    val currentState = state
    if (currentState is ScreenState.Loaded) {
        LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.LongPress)

        currentState.data.message?.let {
            Toast.makeText(LocalContext.current, it, Toast.LENGTH_SHORT).show()
        }
        when (currentState.data) {
            is ShareReceiverViewModel.SharingResult.Edit -> onEdit()
            is ShareReceiverViewModel.SharingResult.Saved -> onSaved()
        }
    } else if (currentState is ScreenState.Error) {
        ShareReceiverErrorDialog(
            throwable = currentState.throwable,
            action = errorDialogAction,
        )
    }

    ShareReceiverScreen(
        icon = icon,
        modifier = modifier,
    )
}

@Composable
fun ShareReceiverScreen(
    icon: Painter,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = icon,
            modifier = Modifier
                .size(50.dp)
                .align(Alignment.Center),
            transitionSpec = { fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut() },
            label = "ShareReceiver_Icon",
        ) {
            Icon(
                painter = it,
                contentDescription = stringResource(id = R.string.cd_share_receiver_image),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        CircularProgressIndicator(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.Center),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
@ThemePreviews
private fun ShareReceiverScreenPreview() {
    ExtendedTheme {
        ShareReceiverScreen(icon = painterResource(id = R.drawable.ic_url_saving))
    }
}

@Composable
fun ShareReceiverErrorDialog(
    throwable: Throwable,
    action: () -> Unit,
) {
    val openDialog = rememberSaveable { mutableStateOf(true) }

    if (!openDialog.value) return

    val errorMessage = when {
        throwable is InvalidUrlException -> R.string.validation_error_invalid_url_rationale
        throwable.isServerException() -> R.string.server_error
        throwable is ResponseException && throwable.response.status.value in AppConfig.LOGIN_FAILED_CODES -> {
            R.string.auth_logged_out_feedback
        }

        else -> {
            val localContext = LocalContext.current
            LaunchedEffect(throwable) {
                localContext.showErrorReportDialog(
                    throwable = throwable,
                    postAction = action,
                )
            }

            return
        }
    }

    AlertDialog(
        onDismissRequest = action,
        confirmButton = {
            Button(onClick = action) {
                Text(text = stringResource(id = R.string.hint_ok))
            }
        },
        text = {
            Text(text = stringResource(id = errorMessage))
        },
    )
}

@Composable
@ThemePreviews
private fun ShareReceiverErrorDialogPreview() {
    ExtendedTheme {
        ShareReceiverErrorDialog(
            throwable = InvalidUrlException(),
            action = {},
        )
    }
}
