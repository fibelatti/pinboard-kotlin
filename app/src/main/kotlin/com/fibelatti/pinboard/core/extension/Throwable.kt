package com.fibelatti.pinboard.core.extension

import java.net.UnknownHostException
import kotlinx.coroutines.TimeoutCancellationException
import retrofit2.HttpException

fun Throwable.isServerException(): Boolean = this is TimeoutCancellationException ||
    this is UnknownHostException ||
    this is HttpException
