package com.fibelatti.pinboard.core.network

import com.fibelatti.pinboard.features.user.domain.UserRepository
import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection
import javax.inject.Inject

class UnauthorizedInterceptor @Inject constructor(
    private val userRepository: UserRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response =
        chain.run { proceed(request()) }
            .also { if (it.code() == HttpURLConnection.HTTP_UNAUTHORIZED) userRepository.forceLogout() }
}
