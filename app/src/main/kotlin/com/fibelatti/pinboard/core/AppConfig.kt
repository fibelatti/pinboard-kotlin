package com.fibelatti.pinboard.core

object AppConfig {

    const val API_ENCODING = "UTF-8"

    const val API_BASE_URL_LENGTH = 90

    const val API_PAGE_SIZE = 10_000

    const val DEFAULT_PAGE_SIZE = 1_000
    const val DEFAULT_RECENT_QUANTITY = 50
    const val DEFAULT_FILTER_MAX_TAGS = 3

    const val PINBOARD_USER_URL = "https://pinboard.in/u:"

    val LOGIN_FAILED_CODES: List<Int> = listOf(401, 500)

    object PinboardApiLiterals {

        const val YES = "yes"
        const val NO = "no"
        const val TAG_SEPARATOR = " "
    }

    enum class PinboardApiMaxLength(val value: Int) {
        TEXT_TYPE(value = 255),

        /**
         * This is the upper limit to prevent data loss when adding bookmarks with long descriptions.
         */
        URI(value = 3_000),

        /**
         * The REST API abuses GET, this is a safe limit to avoid 414 but it can result in data loss.
         */
        SAFE_URI(value = 2_000),
    }
}
