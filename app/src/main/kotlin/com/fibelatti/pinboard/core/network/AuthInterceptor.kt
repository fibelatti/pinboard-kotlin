package com.fibelatti.pinboard.core.network

import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    userSharedPreferences: UserSharedPreferences
) : Interceptor {

    private val parameters: MutableMap<String, String> = hashMapOf(
        "auth_token" to userSharedPreferences.getAuthToken(),
        "format" to "json"
    )

    override fun intercept(chain: Interceptor.Chain): Response =
        chain.run {
            val request = request()
            val url = request.url().newBuilder()
                .apply { parameters.forEach { (name, value) -> addQueryParameter(name, value) } }
                .build()

            proceed(request.newBuilder().url(url).build())
        }
}
