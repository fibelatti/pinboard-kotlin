package com.fibelatti.pinboard.core.extension

import kotlinx.coroutines.TimeoutCancellationException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

fun Throwable.isServerException(): Boolean = this is TimeoutCancellationException ||
    this is UnknownHostException ||
    this is HttpException ||
    this is SSLHandshakeException ||
    this is ConnectException
