package com.fibelatti.pinboard.core.extension

import com.fibelatti.pinboard.core.network.MissingAuthTokenException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentConverterException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.io.IOException

fun Throwable.isServerException(): Boolean {
    val serverTypes = listOf(
        MissingAuthTokenException::class,
        IOException::class,
        TimeoutCancellationException::class,
        ResponseException::class,
        ContentConverterException::class,
    )

    return this::class in serverTypes || cause?.let { it::class in serverTypes } == true
}
