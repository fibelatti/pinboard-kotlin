package com.fibelatti.pinboard.core.network

import androidx.annotation.VisibleForTesting
import com.fibelatti.pinboard.features.user.domain.UserRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection
import javax.inject.Inject

class UnauthorizedInterceptor @Inject constructor(
    private val userRepository: UserRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            forceLogout()
        }

        return response
    }

    @VisibleForTesting
    fun forceLogout() {
        GlobalScope.launch { userRepository.forceLogout() }
    }
}
