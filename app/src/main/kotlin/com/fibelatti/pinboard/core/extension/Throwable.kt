package com.fibelatti.pinboard.core.extension

import java.net.UnknownHostException
import kotlinx.coroutines.TimeoutCancellationException
import retrofit2.HttpException
import java.net.ConnectException
import javax.net.ssl.SSLHandshakeException

fun Throwable.isServerException(): Boolean = this is TimeoutCancellationException ||
    this is UnknownHostException ||
    this is HttpException ||
    this is SSLHandshakeException ||
    this is ConnectException
