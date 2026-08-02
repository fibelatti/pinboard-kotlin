package com.fibelatti.pinboard.core.android.composable

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

/**
 * The timeout that dismisses a snackbar lives in the `SnackbarHost` composable rather than in
 * `SnackbarHostState`, so these tests dismiss messages explicitly to move the queue along.
 */
internal class AppMessageQueueTest {

    private val context: Context = mockk {
        every { getString(any()) } answers { "message-${firstArg<Int>()}" }
    }

    private var now: Long = 0

    private fun TestScope.createQueue(): AppMessageQueue = AppMessageQueue(
        context = context,
        scope = backgroundScope,
        elapsedRealtime = { now },
    )

    private val AppMessageQueue.currentMessage: String?
        get() = snackbarHostState.currentSnackbarData?.visuals?.message

    @Test
    fun `WHEN the same message is sent twice within the window THEN it is shown only once`() = runTest {
        val queue = createQueue()

        queue.show(messageRes = 1)
        queue.show(messageRes = 1)
        runCurrent()

        assertThat(queue.currentMessage).isEqualTo("message-1")

        queue.snackbarHostState.currentSnackbarData?.dismiss()
        runCurrent()

        assertThat(queue.currentMessage).isNull()
    }

    @Test
    fun `WHEN the same message is sent again after the window THEN it is shown twice`() = runTest {
        val queue = createQueue()

        queue.show(messageRes = 1)
        now += 501
        queue.show(messageRes = 1)
        runCurrent()

        assertThat(queue.currentMessage).isEqualTo("message-1")

        queue.snackbarHostState.currentSnackbarData?.dismiss()
        runCurrent()

        assertThat(queue.currentMessage).isEqualTo("message-1")
    }

    @Test
    fun `WHEN different messages are sent within the window THEN both are shown in order`() = runTest {
        val queue = createQueue()

        queue.show(messageRes = 1)
        queue.show(messageRes = 2)
        runCurrent()

        assertThat(queue.currentMessage).isEqualTo("message-1")

        queue.snackbarHostState.currentSnackbarData?.dismiss()
        runCurrent()

        assertThat(queue.currentMessage).isEqualTo("message-2")
    }

    @Test
    fun `WHEN an indefinite message is followed by another THEN it is replaced instead of queued`() = runTest {
        val queue = createQueue()

        queue.show(messageRes = 1, duration = SnackbarDuration.Indefinite)
        runCurrent()

        assertThat(queue.currentMessage).isEqualTo("message-1")

        // Nothing dismisses the indefinite message — the next one must take over on its own.
        queue.show(messageRes = 2)
        runCurrent()

        assertThat(queue.currentMessage).isEqualTo("message-2")
    }
}
