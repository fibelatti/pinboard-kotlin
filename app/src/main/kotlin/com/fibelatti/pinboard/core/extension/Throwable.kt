package com.fibelatti.pinboard.core.extension

import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.TimeoutCancellationException
import java.net.ConnectException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

fun Throwable.isServerException(): Boolean = this is TimeoutCancellationException ||
    this is UnknownHostException ||
    this is ResponseException ||
    this is SSLHandshakeException ||
    this is ConnectException
