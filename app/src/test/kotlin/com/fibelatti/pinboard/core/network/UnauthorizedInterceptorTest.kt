package com.fibelatti.pinboard.core.network

import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.safeAny
import com.fibelatti.core.test.extension.verifySuspend
import com.fibelatti.pinboard.features.user.domain.UserRepository
import okhttp3.Interceptor
import okhttp3.Response
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.never
import java.net.HttpURLConnection

internal class UnauthorizedInterceptorTest {

    private val mockUserRepository = mock<UserRepository>()

    private val unauthorizedInterceptor = UnauthorizedInterceptor(mockUserRepository)

    private val mockChain = mock<Interceptor.Chain>()
    private val mockResponse = mock<Response>()

    @BeforeEach
    fun setup() {
        given(mockChain.proceed(safeAny()))
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
        verifySuspend(mockUserRepository) { forceLogout() }
    }

    @Test
    fun `WHEN proceed code is not HTTP_UNAUTHORIZED THEN forceLogout is not called`() {
        // GIVEN
        given(mockResponse.code())
            .willReturn(HttpURLConnection.HTTP_OK)

        // WHEN
        unauthorizedInterceptor.intercept(mockChain)

        // THEN
        verifySuspend(mockUserRepository, never()) { forceLogout() }
    }
}
