package com.fibelatti.pinboard.core.extension

import io.ktor.client.plugins.ResponseException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.TimeoutCancellationException

fun Throwable.isServerException(): Boolean = this is ResponseException ||
    this is IOException ||
    this is TimeoutCancellationException
