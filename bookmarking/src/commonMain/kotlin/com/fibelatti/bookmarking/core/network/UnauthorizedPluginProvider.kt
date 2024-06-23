package com.fibelatti.bookmarking.core.network

import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.core.annotation.Single

// TODO: Make internal once the migration is completed
@Single
public class UnauthorizedPluginProvider {

    private val _unauthorized = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    public val unauthorized: Flow<Unit> = _unauthorized

    public val plugin: ClientPlugin<Unit> = createClientPlugin("UnauthorizedPlugin") {
        onResponse { response ->
            if (response.status == HttpStatusCode.Unauthorized) {
                _unauthorized.tryEmit(Unit)
            }
        }
    }
}
