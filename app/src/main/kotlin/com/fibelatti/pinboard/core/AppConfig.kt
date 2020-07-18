package com.fibelatti.pinboard.core

object AppConfig {
    const val MAIN_PACKAGE_NAME = "com.fibelatti.pinboard"

    const val API_BASE_URL = "https://api.pinboard.in/v1/"
    const val API_ENCODING = "UTF-8"
    const val API_MAX_LENGTH = 255
    const val API_MAX_URI_LENGTH = 2000
    const val API_BASE_URL_LENGTH = 90

    const val API_PAGE_SIZE = 5000

    // Pinboard API requires a minimum of 3 seconds between each request
    const val API_THROTTLE_TIME = 3000L

    const val DEFAULT_PAGE_SIZE = 100
    const val DEFAULT_RECENT_QUANTITY = 50
    const val DEFAULT_FILTER_MAX_TAGS = 3

    const val PLAY_STORE_BASE_URL = "https://play.google.com/store/apps/details?id="

    object PinboardApiLiterals {
        const val YES = "yes"
        const val NO = "no"
        const val TAG_SEPARATOR_REQUEST = "+"
        const val TAG_SEPARATOR_RESPONSE = " "
    }
}
