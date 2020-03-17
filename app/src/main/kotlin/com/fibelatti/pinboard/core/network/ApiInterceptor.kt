package com.fibelatti.pinboard.core.network

import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class ApiInterceptor @Inject constructor(
    private val userSharedPreferences: UserSharedPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url().newBuilder()
            .addQueryParameter("format", "json")
            .addEncodedQueryParameter("auth_token", userSharedPreferences.getAuthToken())
            .build()

        return catchingSocketTimeoutException(chain) {
            request.newBuilder().url(url).build()
        }
    }
}
