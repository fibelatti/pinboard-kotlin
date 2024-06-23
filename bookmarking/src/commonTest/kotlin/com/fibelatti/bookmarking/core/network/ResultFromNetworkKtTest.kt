package com.fibelatti.bookmarking.core.network

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.exceptionOrNull
import com.google.common.truth.Truth.assertThat
import io.ktor.client.plugins.ResponseException
import io.ktor.utils.io.errors.IOException
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class ResultFromNetworkKtTest {

    private var httpExceptionBlockExecutionCounter = 0
    private val httpExceptionBlock = {
        httpExceptionBlockExecutionCounter = httpExceptionBlockExecutionCounter.inc()

        throw ResponseException(
            response = mockk {
                every { status } returns mockk {
                    every { value } returns 429
                }
            },
            cachedResponseText = "",
        )
    }

    private var ioExceptionBlockExecutionCounter = 0
    private val ioExceptionBlock = {
        ioExceptionBlockExecutionCounter = ioExceptionBlockExecutionCounter.inc()
        throw IOException()
    }

    @Test
    fun `resultFromNetwork should retry HttpExceptions`() = runTest {
        resultFromNetwork { httpExceptionBlock() }

        assertThat(httpExceptionBlockExecutionCounter).isEqualTo(3)
    }

    @Test
    fun `resultFromNetwork should return a Failure when an HttpException happens`() = runTest {
        val result: Result<Any> = resultFromNetwork { httpExceptionBlock() }

        assertThat(result.exceptionOrNull()).isInstanceOf(ResponseException::class.java)
    }

    @Test
    fun `resultFromNetwork should retry IOExceptions`() = runTest {
        resultFromNetwork { ioExceptionBlock() }

        assertThat(ioExceptionBlockExecutionCounter).isEqualTo(5)
    }

    @Test
    fun `resultFromNetwork should return a Failure when an IOException happens`() = runTest {
        val result: Result<Any> = resultFromNetwork { ioExceptionBlock() }

        assertThat(result.exceptionOrNull()).isInstanceOf(IOException::class.java)
    }
}
