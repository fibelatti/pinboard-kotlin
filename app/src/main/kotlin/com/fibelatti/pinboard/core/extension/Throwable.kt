package com.fibelatti.pinboard.core.extension

import com.fibelatti.pinboard.core.network.MissingAuthTokenException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentConverterException
import io.ktor.serialization.ContentConvertException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.io.IOException

fun Throwable.isServerException(): Boolean {
    val serverTypes = listOf(
        MissingAuthTokenException::class,
        IOException::class,
        TimeoutCancellationException::class,
        ServerResponseException::class,
        RedirectResponseException::class,
        ContentConverterException::class,
        ContentConvertException::class,
    )

    return serverTypes.any { type -> type.isInstance(this) || cause?.let(type::isInstance) == true }
}
