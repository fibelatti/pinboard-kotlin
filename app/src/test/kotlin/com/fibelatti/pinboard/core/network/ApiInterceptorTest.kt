package com.fibelatti.pinboard.core.network

import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.safeAny
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import java.net.SocketTimeoutException

internal class ApiInterceptorTest {

    private val mockUserSharedPreferences = mock<UserSharedPreferences>()

    private val apiInterceptor = ApiInterceptor(mockUserSharedPreferences)

    private val mockChain = mock<Interceptor.Chain>()
    private val mockRequest = mock<Request>()
    private val mockRequestBuilder = mock<Request.Builder>()
    private val mockUrl = mock<HttpUrl>()
    private val mockUrlBuilder = mock<HttpUrl.Builder>()
    private val mockResponse = mock<Response>()

    private val expectedToken = "some-token"

    @BeforeEach
    fun setup() {
        given(mockUserSharedPreferences.getAuthToken())
            .willReturn(expectedToken)

        given(mockChain.request())
            .willReturn(mockRequest)
        given(mockChain.proceed(mockRequest))
            .willReturn(mockResponse)
        given(mockRequest.url)
            .willReturn(mockUrl)
        given(mockUrl.newBuilder())
            .willReturn(mockUrlBuilder)
        given(mockUrlBuilder.addQueryParameter(anyString(), anyString()))
            .willReturn(mockUrlBuilder)
        given(mockUrlBuilder.addEncodedQueryParameter(anyString(), anyString()))
            .willReturn(mockUrlBuilder)
        given(mockUrlBuilder.build())
            .willReturn(mockUrl)
        given(mockRequest.newBuilder())
            .willReturn(mockRequestBuilder)
        given(mockRequestBuilder.url(safeAny<HttpUrl>()))
            .willReturn(mockRequestBuilder)
        given(mockRequestBuilder.build())
            .willReturn(mockRequest)
    }

    @Test
    fun `WHEN intercept is called THEN format query parameter is added`() {
        // WHEN
        apiInterceptor.intercept(mockChain)

        // THEN
        verify(mockUrlBuilder).addQueryParameter("format", "json")
    }

    @Test
    fun `WHEN intercept is called THEN auth_token encoded query parameter is added`() {
        // WHEN
        apiInterceptor.intercept(mockChain)

        // THEN
        verify(mockUrlBuilder).addEncodedQueryParameter("auth_token", expectedToken)
    }

    @Test
    fun `WHEN a SocketTimeoutException happens THEN response code 408 is returned`() {
        // GIVEN
        given(mockChain.proceed(mockRequest))
            .willThrow(SocketTimeoutException())

        // WHEN
        val result = apiInterceptor.intercept(mockChain)

        // THEN
        result.code shouldBe 408
    }

    @Test
    fun `WHEN the request proceeds normally THEN response is returned`() {
        // WHEN
        val result = apiInterceptor.intercept(mockChain)

        // THEN
        result shouldBe mockResponse
    }
}
