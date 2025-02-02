package com.fibelatti.pinboard.core.extension

import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.io.IOException

fun Throwable.isServerException(): Boolean {
    val serverTypes = listOf(
        ResponseException::class,
        IOException::class,
        TimeoutCancellationException::class,
    )

    return this::class in serverTypes || cause?.let { it::class in serverTypes } == true
}
