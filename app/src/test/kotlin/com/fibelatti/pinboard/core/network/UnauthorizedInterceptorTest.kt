package com.fibelatti.pinboard.core.network

import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.HttpURLConnection
import java.net.SocketTimeoutException

internal class UnauthorizedInterceptorTest {

    private val mockUserRepository = mockk<UserRepository>(relaxUnitFun = true)

    private val unauthorizedInterceptor = UnauthorizedInterceptor(mockUserRepository)

    private val mockChain = mockk<Interceptor.Chain>()
    private val mockRequest = mockk<Request>()
    private val mockResponse = mockk<Response>()

    @BeforeEach
    fun setup() {
        every { mockChain.request() } returns mockRequest
        every { mockChain.proceed(mockRequest) } returns mockResponse
    }

    @Test
    fun `WHEN proceed code is HTTP_UNAUTHORIZED THEN forceLogout is called`() {
        // GIVEN
        every { mockResponse.code } returns HttpURLConnection.HTTP_UNAUTHORIZED

        // WHEN
        unauthorizedInterceptor.intercept(mockChain)

        // THEN
        verify { mockUserRepository.forceLogout() }
    }

    @Test
    fun `WHEN proceed code is HTTP_INTERNAL_ERROR THEN forceLogout is called`() {
        // GIVEN
        every { mockResponse.code } returns HttpURLConnection.HTTP_INTERNAL_ERROR

        // WHEN
        unauthorizedInterceptor.intercept(mockChain)

        // THEN
        verify { mockUserRepository.forceLogout() }
    }

    @Test
    fun `WHEN proceed code is not HTTP_UNAUTHORIZED THEN forceLogout is not called`() {
        // GIVEN
        every { mockResponse.code } returns HttpURLConnection.HTTP_OK

        // WHEN
        unauthorizedInterceptor.intercept(mockChain)

        // THEN
        verify(exactly = 0) { mockUserRepository.forceLogout() }
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
