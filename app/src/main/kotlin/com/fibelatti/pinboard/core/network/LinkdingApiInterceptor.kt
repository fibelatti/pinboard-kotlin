package com.fibelatti.pinboard.core.network

import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class LinkdingApiInterceptor @Inject constructor(
    private val userSharedPreferences: UserSharedPreferences,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response = catchingSocketTimeoutException(chain) {
        chain.request()
            .newBuilder()
            .addHeader("Authorization", "Token ${userSharedPreferences.authToken}")
            .build()
    }
}
