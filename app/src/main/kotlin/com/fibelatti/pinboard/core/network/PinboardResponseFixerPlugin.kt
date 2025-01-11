package com.fibelatti.pinboard.core.network

import com.fibelatti.pinboard.features.posts.data.model.GenericResponseDto
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.utils.io.readUTF8Line

/**
 * Following a refactor on December 2024 the API started returning an invalid JSON response (with
 * prepended log messages). This plugin sanitizes the response to ensure that the app continues to
 * work despite that.
 */
val PinboardResponseFixerPlugin = createClientPlugin("PinboardResponseFixer") {
    val regex = "\"result_code\":\"(.*)\"".toRegex()

    transformResponseBody { _, content, requestedType ->
        if (requestedType.type != GenericResponseDto::class) {
            return@transformResponseBody null
        }

        content.readUTF8Line()
            ?.let(regex::find)
            ?.groupValues?.get(1)
            ?.let(::GenericResponseDto)
    }
}
