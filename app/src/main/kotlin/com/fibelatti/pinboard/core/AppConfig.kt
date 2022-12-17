package com.fibelatti.pinboard.core

object AppConfig {

    const val MAIN_PACKAGE_NAME = "com.fibelatti.pinboard"

    const val API_BASE_URL = "https://api.pinboard.in/v1/"
    const val API_ENCODING = "UTF-8"

    const val API_BASE_URL_LENGTH = 90

    const val API_PAGE_SIZE = 10_000
    const val MALFORMED_OBJECT_THRESHOLD = 1_000

    // Pinboard API requires a minimum of 3 seconds between each request
    const val API_THROTTLE_TIME = 3_000L

    const val DEFAULT_PAGE_SIZE = 100
    const val DEFAULT_RECENT_QUANTITY = 50
    const val DEFAULT_FILTER_MAX_TAGS = 3

    const val PINBOARD_USER_URL = "https://pinboard.in/u:"

    const val PLAY_STORE_BASE_URL = "https://play.google.com/store/apps/details?id="

    object PinboardApiLiterals {

        const val YES = "yes"
        const val NO = "no"
        const val TAG_SEPARATOR_REQUEST = "+"
        const val TAG_SEPARATOR_RESPONSE = " "
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
