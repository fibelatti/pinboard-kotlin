package com.fibelatti.pinboard.core.network

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.core.annotation.Single
import javax.inject.Inject
import javax.inject.Singleton

@Single
@Singleton
class UnauthorizedPluginProvider @Inject constructor() {

    private val _unauthorized = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val unauthorized: Flow<Unit> = _unauthorized

    val plugin = createClientPlugin("UnauthorizedPlugin") {
        onResponse { response ->
            if (response.status == HttpStatusCode.Unauthorized) {
                _unauthorized.tryEmit(Unit)
            }
        }
    }
}
