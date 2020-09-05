package com.fibelatti.pinboard.core.network

import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.SocketTimeoutException

internal class ApiInterceptorTest {

    private val mockUserSharedPreferences = mockk<UserSharedPreferences>()

    private val apiInterceptor = ApiInterceptor(mockUserSharedPreferences)

    private val mockChain = mockk<Interceptor.Chain>()
    private val mockRequest = mockk<Request>()
    private val mockRequestBuilder = mockk<Request.Builder>()
    private val mockUrl = mockk<HttpUrl>()
    private val mockUrlBuilder = mockk<HttpUrl.Builder>()
    private val mockResponse = mockk<Response>()

    private val expectedToken = "some-token"

    @BeforeEach
    fun setup() {
        every { mockUserSharedPreferences.getAuthToken() } returns expectedToken
        every { mockChain.request() } returns mockRequest
        every { mockChain.proceed(mockRequest) } returns mockResponse
        every { mockRequest.url } returns mockUrl
        every { mockUrl.newBuilder() } returns mockUrlBuilder
        every { mockUrlBuilder.addQueryParameter(any(), any()) } returns mockUrlBuilder
        every { mockUrlBuilder.addEncodedQueryParameter(any(), any()) } returns mockUrlBuilder
        every { mockUrlBuilder.build() } returns mockUrl
        every { mockRequest.newBuilder() } returns mockRequestBuilder
        every { mockRequestBuilder.url(any<HttpUrl>()) } returns mockRequestBuilder
        every { mockRequestBuilder.build() } returns mockRequest
    }

    @Test
    fun `WHEN intercept is called THEN format query parameter is added`() {
        // WHEN
        apiInterceptor.intercept(mockChain)

        // THEN
        verify { mockUrlBuilder.addQueryParameter("format", "json") }
    }

    @Test
    fun `WHEN intercept is called THEN auth_token encoded query parameter is added`() {
        // WHEN
        apiInterceptor.intercept(mockChain)

        // THEN
        verify { mockUrlBuilder.addEncodedQueryParameter("auth_token", expectedToken) }
    }

    @Test
    fun `WHEN a SocketTimeoutException happens THEN response code 408 is returned`() {
        // GIVEN
        every { mockChain.proceed(mockRequest) } throws SocketTimeoutException()

        // WHEN
        val result = apiInterceptor.intercept(mockChain)

        // THEN
        assertThat(result.code).isEqualTo(408)
    }

    @Test
    fun `WHEN the request proceeds normally THEN response is returned`() {
        // WHEN
        val result = apiInterceptor.intercept(mockChain)

        // THEN
        assertThat(result).isEqualTo(mockResponse)
    }
}
