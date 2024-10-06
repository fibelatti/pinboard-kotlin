package com.fibelatti.pinboard.core.extension

import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.io.IOException

fun Throwable.isServerException(): Boolean = this is ResponseException ||
    this is IOException ||
    this is TimeoutCancellationException
