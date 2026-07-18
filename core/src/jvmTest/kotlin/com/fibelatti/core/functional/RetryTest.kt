package com.fibelatti.core.functional

import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.andThenJust
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class RetryTest {

    private val mockFn = spyk(MockFunctions())

    @Test
    fun `GIVEN the block threw IOException WHEN a retry is successful before maxDelay THEN the result is returned`() =
        runTest {
            // GIVEN
            every { mockFn.default() }.throws(IOException())
                .andThenThrows(IOException())
                .andThenThrows(IOException())
                .andThenThrows(IOException())
                .andThenJust(Runs)

            // WHEN
            val result = retry { mockFn.default() }

            // THEN
            assertThat(result).isEqualTo(Unit)
        }

    @Test
    fun `GIVEN the block throws IOException WHEN no retry is successful before maxDelay THEN IOException is returned`() =
        runTest {
            // GIVEN
            every { mockFn.default() }.throws(IOException())
                .andThenThrows(IOException())
                .andThenThrows(IOException())
                .andThenThrows(IOException())
                .andThenThrows(IOException())
                .andThenThrows(IOException())
                .andThenJust(Runs)

            // THEN
            assertThrows<Exception> {
                retry { mockFn.default() }
            }
        }

    @Test
    fun `GIVEN the block threw a subtype of a retryable type WHEN a retry is successful THEN the result is returned`() =
        runTest {
            // GIVEN
            every { mockFn.default() }.throws(SocketTimeoutException())
                .andThenThrows(UnknownHostException())
                .andThenJust(Runs)

            // WHEN
            val result = retry { mockFn.default() }

            // THEN
            assertThat(result).isEqualTo(Unit)
            verify(exactly = 3) { mockFn.default() }
        }

    @Test
    fun `GIVEN the block throws a non retryable type WHEN retry block is called then exception is thrown`() =
        runTest {
            every { mockFn.default() } throws IllegalStateException()

            assertThrows<IllegalStateException> {
                retry { mockFn.default() }
            }

            verify(exactly = 1) { mockFn.default() }
        }

    @Test
    fun `GIVEN the block throws a CancellationException WHEN retry block is called then exception is thrown`() =
        runTest {
            every { mockFn.default() } throws CancellationException()

            assertThrows<CancellationException> {
                retry { mockFn.default() }
            }

            verify(exactly = 1) { mockFn.default() }
        }

    class MockFunctions {

        fun default() = Unit
    }
}
