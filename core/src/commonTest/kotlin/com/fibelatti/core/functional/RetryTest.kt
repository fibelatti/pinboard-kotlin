package com.fibelatti.core.functional

import com.google.common.truth.Truth.assertThat
import io.ktor.utils.io.errors.IOException
import io.mockk.Runs
import io.mockk.andThenJust
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class RetryTest {

    @Nested
    inner class NetworkRetryTests {

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
                val result = retryIO { mockFn.default() }

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
                    retryIO { mockFn.default() }
                }
            }
    }

    class MockFunctions {

        fun default() = Unit
    }
}
