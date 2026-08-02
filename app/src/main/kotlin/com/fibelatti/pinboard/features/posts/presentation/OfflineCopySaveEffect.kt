package com.fibelatti.pinboard.features.posts.presentation

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalView
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.extension.showBanner
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
    val localView: View = LocalView.current
    val currentHandler: () -> Unit by rememberUpdatedState(handler)

    SideEffect(isSavingOfflineCopy) {
        if (isSavingOfflineCopy) {
            localView.showBanner(R.string.posts_offline_copy_saving)
        }
    }

    SideEffect(offlineCopySaved) {
        when {
            offlineCopySaved.getOrNull() == true -> {
                localView.showBanner(
                    if (truncated) {
                        R.string.posts_offline_copy_saved_truncated
                    } else {
                        R.string.posts_offline_copy_saved
                    },
                )
                currentHandler()
            }

            offlineCopySaved.isFailure -> {
                localView.showBanner(
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
