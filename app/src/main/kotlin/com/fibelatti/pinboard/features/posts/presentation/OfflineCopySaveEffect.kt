package com.fibelatti.pinboard.features.posts.presentation

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.AppMessageQueue
import com.fibelatti.pinboard.core.android.composable.LocalAppMessages
import com.fibelatti.pinboard.features.offline.domain.NoReadableContentException

/**
 * Reports the progress and the outcome of saving an offline copy.
 *
 * Shared by every screen that can start a capture — the bookmark list, through the quick actions,
 * and the bookmark details — so the feedback stays identical wherever the capture was started from.
 */
@Composable
fun OfflineCopySaveEffect(
    isSavingOfflineCopy: Boolean,
    offlineCopySaved: Result<Boolean>,
    truncated: Boolean,
    handler: () -> Unit,
) {
    val localAppMessages: AppMessageQueue = LocalAppMessages.current
    val currentHandler: () -> Unit by rememberUpdatedState(handler)

    SideEffect(isSavingOfflineCopy) {
        if (isSavingOfflineCopy) {
            // Indefinite so that the outcome replaces it instead of waiting for it to time out.
            localAppMessages.show(
                messageRes = R.string.posts_offline_copy_saving,
                duration = SnackbarDuration.Indefinite,
            )
        }
    }

    SideEffect(offlineCopySaved) {
        when {
            offlineCopySaved.getOrNull() == true -> {
                localAppMessages.show(
                    if (truncated) {
                        R.string.posts_offline_copy_saved_truncated
                    } else {
                        R.string.posts_offline_copy_saved
                    },
                )
                currentHandler()
            }

            offlineCopySaved.isFailure -> {
                localAppMessages.show(
                    if (offlineCopySaved.exceptionOrNull() is NoReadableContentException) {
                        R.string.posts_offline_copy_unreadable
                    } else {
                        R.string.posts_offline_copy_failed
                    },
                )
                currentHandler()
            }
        }
    }
}
