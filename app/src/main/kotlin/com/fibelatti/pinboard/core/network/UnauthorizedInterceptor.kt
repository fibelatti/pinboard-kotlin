package com.fibelatti.pinboard.core.network

import com.fibelatti.pinboard.features.user.domain.UserRepository
import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection
import javax.inject.Inject

class UnauthorizedInterceptor @Inject constructor(
    private val userRepository: UserRepository
) : Interceptor {

    private val loginFailedCodes = listOf(
        HttpURLConnection.HTTP_UNAUTHORIZED,
        HttpURLConnection.HTTP_INTERNAL_ERROR
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = catchingSocketTimeoutException(chain, chain::request)

        if (response.code in loginFailedCodes) {
            userRepository.forceLogout()
        }

        return response
    }
}
