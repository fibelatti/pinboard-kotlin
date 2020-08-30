package com.fibelatti.pinboard.core.extension

import kotlinx.coroutines.TimeoutCancellationException
import java.net.UnknownHostException

fun Throwable.isServerDownException(): Boolean = this is TimeoutCancellationException ||
    this is UnknownHostException
