package com.fibelatti.pinboard.core.network

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.net.SocketTimeoutException

class ApiException : Throwable()

class InvalidRequestException : Throwable()

/**
 * Proceeds the [Request] of the given block inside a try catch that catches [SocketTimeoutException].
 *
 * @param chain the [Interceptor.Chain] to proceed
 * @param block a block that returns the [Request] to proceed
 *
 * @return the original [Response] of the request if no [SocketTimeoutException] happened, a fake
 * response otherwise
 */
@Suppress("MagicNumber")
inline fun catchingSocketTimeoutException(
    chain: Interceptor.Chain,
    block: () -> Request
): Response {
    return try {
        chain.proceed(block())
    } catch (exception: SocketTimeoutException) {
        Response.Builder()
            .request(chain.request())
            .code(408)
            .message(exception.message ?: "")
            .body("{}".toResponseBody("text/plain; charset=utf-8".toMediaTypeOrNull()))
            .protocol(Protocol.HTTP_1_1)
            .build()
    }
}
