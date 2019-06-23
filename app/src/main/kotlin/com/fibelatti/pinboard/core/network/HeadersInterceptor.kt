package com.fibelatti.pinboard.core.network

import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class HeadersInterceptor @Inject constructor(
    private val userSharedPreferences: UserSharedPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response =
        chain.run {
            val request = request()
            val url = request.url().newBuilder()
                .apply {
                    addQueryParameter("format", "json")
                    addEncodedQueryParameter("auth_token", userSharedPreferences.getAuthToken())
                }
                .build()

            proceed(request.newBuilder().url(url).build())
        }
}
