package com.fibelatti.pinboard.core.network

import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.features.user.domain.UserRepository
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import java.net.HttpURLConnection
import java.net.SocketTimeoutException

internal class UnauthorizedInterceptorTest {

    private val mockUserRepository = mock<UserRepository>()

    private val unauthorizedInterceptor = UnauthorizedInterceptor(mockUserRepository)

    private val mockChain = mock<Interceptor.Chain>()
    private val mockRequest = mock<Request>()
    private val mockResponse = mock<Response>()

    @BeforeEach
    fun setup() {
        given(mockChain.request())
            .willReturn(mockRequest)
        given(mockChain.proceed(mockRequest))
            .willReturn(mockResponse)
    }

    @Test
    fun `WHEN proceed code is HTTP_UNAUTHORIZED THEN forceLogout is called`() {
        // GIVEN
        given(mockResponse.code())
            .willReturn(HttpURLConnection.HTTP_UNAUTHORIZED)

        // WHEN
        unauthorizedInterceptor.intercept(mockChain)

        // THEN
        verify(mockUserRepository).forceLogout()
    }

    @Test
    fun `WHEN proceed code is not HTTP_UNAUTHORIZED THEN forceLogout is not called`() {
        // GIVEN
        given(mockResponse.code())
            .willReturn(HttpURLConnection.HTTP_OK)

        // WHEN
        unauthorizedInterceptor.intercept(mockChain)

        // THEN
        verify(mockUserRepository, never()).forceLogout()
    }

    @Test
    fun `WHEN a SocketTimeoutException happens THEN response code 408 is returned`() {
        // GIVEN
        given(mockChain.proceed(mockRequest))
            .willThrow(SocketTimeoutException())

        // WHEN
        val result = unauthorizedInterceptor.intercept(mockChain)

        // THEN
        result.code() shouldBe 408
    }
}
