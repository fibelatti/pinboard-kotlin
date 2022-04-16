package com.fibelatti.pinboard.core.network

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnauthorizedInterceptor @Inject constructor() : Interceptor {

    private val loginFailedCodes = listOf(
        HttpURLConnection.HTTP_UNAUTHORIZED,
        HttpURLConnection.HTTP_INTERNAL_ERROR,
    )

    private val _unauthorized = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val unauthorized: Flow<Unit> = _unauthorized

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = catchingSocketTimeoutException(chain, chain::request)

        if (response.code in loginFailedCodes) {
            _unauthorized.tryEmit(Unit)
        }

        return response
    }
}
