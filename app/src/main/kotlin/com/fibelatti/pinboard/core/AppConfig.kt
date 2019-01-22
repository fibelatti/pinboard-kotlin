package com.fibelatti.pinboard.core

object AppConfig {
    const val API_BASE_URL = "https://api.pinboard.in/v1/"
    const val API_ENCODING = "UTF-8"
    const val API_FILTER_MAX_TAGS = 3

    const val PLAY_STORE_BASE_URL = "https://play.google.com/store/apps/details?id="
    const val MARKET_BASE_URL = "market://details?id="

    const val LOCALE_NONE = "none"

    enum class AppTheme(val value: String) {
        CLASSIC("classic"), DARK("dark")
    }

    enum class AppLanguage(val value: String) {
        ENGLISH("en"), PORTUGUESE("pt"), SPANISH("es")
    }

    object PinboardApiLiterals {
        const val YES = "yes"
        const val NO = "no"
        const val TAG_SEPARATOR_REQUEST = "+"
        const val TAG_SEPARATOR_RESPONSE = " "
    }
}
