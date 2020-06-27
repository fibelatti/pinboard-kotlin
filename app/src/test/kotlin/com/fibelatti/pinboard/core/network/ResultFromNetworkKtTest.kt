package com.fibelatti.pinboard.core.network

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.core.test.extension.shouldBeAnInstanceOf
import com.fibelatti.pinboard.features.posts.data.model.GenericResponseDto
import kotlinx.coroutines.runBlocking
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
                "{}".toResponseBody("application/json".toMediaTypeOrNull())
            )
        )
    }

    private var ioExceptionBlockExecutionCounter = 0
    private val ioExceptionBlock = {
        ioExceptionBlockExecutionCounter = ioExceptionBlockExecutionCounter.inc()
        throw IOException()
    }

    @Test
    fun `resultFromNetwork should retry HttpExceptions`() {
        runBlocking { resultFromNetwork { httpExceptionBlock() } }

        httpExceptionBlockExecutionCounter shouldBe 3
    }

    @Test
    fun `resultFromNetwork should return a Failure when an HttpException happens`() {
        val result = runBlocking { resultFromNetwork { httpExceptionBlock() } }

        result.shouldBeAnInstanceOf<Failure>()
    }

    @Test
    fun `resultFromNetwork should retry IOExceptions`() {
        runBlocking { resultFromNetwork { ioExceptionBlock() } }

        ioExceptionBlockExecutionCounter shouldBe 5
    }

    @Test
    fun `resultFromNetwork should return a Failure when an IOException happens`() {
        val result = runBlocking { resultFromNetwork { ioExceptionBlock() } }

        result.shouldBeAnInstanceOf<Failure>()
    }
}
