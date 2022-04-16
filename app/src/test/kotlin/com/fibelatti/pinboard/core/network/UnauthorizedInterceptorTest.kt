package com.fibelatti.pinboard.core.network

import com.fibelatti.pinboard.isEmpty
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.HttpURLConnection
import java.net.SocketTimeoutException

internal class UnauthorizedInterceptorTest {

    private val unauthorizedInterceptor = UnauthorizedInterceptor()

    private val mockChain = mockk<Interceptor.Chain>()
    private val mockRequest = mockk<Request>()
    private val mockResponse = mockk<Response>()

    @BeforeEach
    fun setup() {
        every { mockChain.request() } returns mockRequest
        every { mockChain.proceed(mockRequest) } returns mockResponse
    }

    @Test
    fun `WHEN proceed code is HTTP_UNAUTHORIZED THEN unauthorized emits`() = runTest(UnconfinedTestDispatcher()) {
        // GIVEN
        every { mockResponse.code } returns HttpURLConnection.HTTP_UNAUTHORIZED
        val result = async { unauthorizedInterceptor.unauthorized.firstOrNull() }

        // WHEN
        unauthorizedInterceptor.intercept(mockChain)

        // THEN
        assertThat(result.await()).isNotNull()
    }

    @Test
    fun `WHEN proceed code is HTTP_INTERNAL_ERROR THEN unauthorized emits`() = runTest(UnconfinedTestDispatcher()) {
        // GIVEN
        every { mockResponse.code } returns HttpURLConnection.HTTP_INTERNAL_ERROR
        val result = async { unauthorizedInterceptor.unauthorized.firstOrNull() }

        // WHEN
        unauthorizedInterceptor.intercept(mockChain)

        // THEN
        assertThat(result.await()).isNotNull()
    }

    @Test
    fun `WHEN proceed code is not HTTP_UNAUTHORIZED THEN unauthorized does not emit`() =
        runTest(UnconfinedTestDispatcher()) {
            // GIVEN
            every { mockResponse.code } returns HttpURLConnection.HTTP_OK
            val result = async { unauthorizedInterceptor.unauthorized.isEmpty() }

            // WHEN
            unauthorizedInterceptor.intercept(mockChain)

            // THEN
            assertThat(result.await()).isTrue()
        }

    @Test
    fun `WHEN a SocketTimeoutException happens THEN response code 408 is returned`() {
        // GIVEN
        every { mockChain.proceed(mockRequest) } throws SocketTimeoutException()

        // WHEN
        val result = unauthorizedInterceptor.intercept(mockChain)

        // THEN
        assertThat(result.code).isEqualTo(408)
    }
}
