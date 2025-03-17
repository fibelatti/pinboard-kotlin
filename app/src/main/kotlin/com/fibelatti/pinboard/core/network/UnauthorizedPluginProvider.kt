package com.fibelatti.pinboard.core.network

import com.fibelatti.pinboard.core.AppMode
import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Singleton
class UnauthorizedPluginProvider @Inject constructor() {

    private val enabledAppModes = mutableSetOf<AppMode>()

    private val _unauthorized = MutableSharedFlow<AppMode>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val unauthorized: Flow<AppMode> = _unauthorized.asSharedFlow()

    val plugin: ClientPlugin<Unit> = createClientPlugin("UnauthorizedPlugin") {
        onResponse { response ->
            val appMode = if (response.request.url.host == "api.pinboard.in") {
                AppMode.PINBOARD
            } else {
                AppMode.LINKDING
            }

            // An unrelated endpoint is used to validate the provided token.
            // If the user provides an invalid token the server will respond with `Unauthorized`,
            // but we don't want to handle it that way until after they've successfully logged in.
            if (appMode in enabledAppModes && response.status == HttpStatusCode.Unauthorized) {
                _unauthorized.emit(appMode)
            }
        }
    }

    fun enable(appMode: AppMode) {
        enabledAppModes.add(appMode)
    }

    fun disable(appMode: AppMode) {
        enabledAppModes.remove(appMode)
    }
}
