package com.fibelatti.bookmarking.core

public object Config {

    public const val PLAY_STORE_PACKAGE_NAME: String = "com.fibelatti.pinboard"
    public const val PLAY_STORE_BASE_URL: String = "https://play.google.com/store/apps/details?id="

    public const val API_PAGE_SIZE: Int = 10_000
    public const val LOCAL_PAGE_SIZE: Int = 1_000
    public const val DEFAULT_RECENT_QUANTITY: Int = 50
    public const val DEFAULT_FILTER_MAX_TAGS: Int = 3

    public val LOGIN_FAILED_CODES: List<Int> = listOf(401, 500)

    public object Pinboard {

        public const val API_BASE_URL_LENGTH: Int = 90
        public const val MALFORMED_OBJECT_THRESHOLD: Int = 1_000
        public const val USER_URL: String = "https://pinboard.in/u:"
        public const val LITERAL_YES: String = "yes"
        public const val LITERAL_NO: String = "no"
        public const val TAG_SEPARATOR: String = " "

        internal enum class MaxLength(val value: Int) {

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
}
