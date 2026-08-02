package com.fibelatti.pinboard.core.android.composable

import android.content.Context
import android.os.SystemClock
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * A transient message to be shown to the user.
 */
data class AppMessage(
    @param:StringRes val messageRes: Int,
    val duration: SnackbarDuration = SnackbarDuration.Short,
)

/**
 * Owns the queue of transient messages shown by the app's single `SnackbarHost`.
 *
 * Constructed by the hosting activity with its `lifecycleScope` rather than by a composable,
 * because [SnackbarHostState.showSnackbar] suspends for as long as the message is on screen and
 * drops it when canceled. Most messages are sent right before navigating away, so a scope tied to
 * the composition that sent them would be canceled before the message was ever seen.
 */
@Stable
class AppMessageQueue(
    private val context: Context,
    scope: CoroutineScope,
    private val elapsedRealtime: () -> Long = SystemClock::uptimeMillis,
) {

    val snackbarHostState: SnackbarHostState = SnackbarHostState()

    /**
     * A channel rather than a shared flow: the consumer spends most of its time suspended inside
     * [SnackbarHostState.showSnackbar], and a shared flow would drop everything emitted meanwhile.
     */
    private val messages: Channel<AppMessage> = Channel(capacity = Channel.BUFFERED)

    private var lastMessage: AppMessage? = null
    private var lastMessageAt: Long = 0

    init {
        scope.launch {
            var supersedable: Job? = null

            for (message in messages) {
                // cancelAndJoin, rather than cancel, because the canceled job holds the host state's
                // mutex until it unwinds. A bare cancel would leave the next message queued behind it.
                supersedable?.cancelAndJoin()
                supersedable = null

                val text: String = context.getString(message.messageRes)

                if (message.duration == SnackbarDuration.Indefinite) {
                    // Launched instead of awaited so the next message can replace it.
                    supersedable = launch {
                        snackbarHostState.showSnackbar(
                            message = text,
                            withDismissAction = true,
                            duration = message.duration,
                        )
                    }
                } else {
                    snackbarHostState.showSnackbar(message = text, duration = message.duration)
                }
            }
        }
    }

    fun show(@StringRes messageRes: Int, duration: SnackbarDuration = SnackbarDuration.Short) {
        val message = AppMessage(messageRes = messageRes, duration = duration)
        val now: Long = elapsedRealtime()

        // Both panels are composed in multi-panel mode, so the effects they share send the same
        // message twice in the same frame. Queueing both would show it twice in a row.
        if (message == lastMessage && now - lastMessageAt < DUPLICATE_WINDOW_MS) return

        lastMessage = message
        lastMessageAt = now

        messages.trySend(message)
    }

    private companion object {

        private const val DUPLICATE_WINDOW_MS: Long = 500
    }
}

/**
 * Provides the [AppMessageQueue] of the hosting activity.
 */
val LocalAppMessages = staticCompositionLocalOf<AppMessageQueue> {
    error("No AppMessageQueue provided")
}
