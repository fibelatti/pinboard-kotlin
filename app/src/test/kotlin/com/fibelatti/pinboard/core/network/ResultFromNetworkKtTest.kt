package com.fibelatti.pinboard.core.network

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.pinboard.features.posts.data.model.GenericResponseDto
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

internal class ResultFromNetworkKtTest {

    private var httpExceptionBlockExecutionCounter = 0
    private val httpExceptionBlock = {
        httpExceptionBlockExecutionCounter = httpExceptionBlockExecutionCounter.inc()
        throw HttpException(
            Response.error<GenericResponseDto>(
                429,
                "{}".toResponseBody("application/json".toMediaTypeOrNull()),
            ),
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

        assertThat(result.exceptionOrNull()).isInstanceOf(HttpException::class.java)
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
