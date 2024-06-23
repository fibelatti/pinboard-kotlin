package com.fibelatti.bookmarking.core

public enum class AppMode {

    /**
     * The app uses no external API and all bookmarks are stored only in the local database.
     */
    NO_API,

    /**
     * The app uses the Pinboard API to store and retrieve bookmarks, backed by a local database.
     */
    PINBOARD,

    /**
     * The app uses the Linkding API to store and retrieve bookmarks, backed by a local database.
     */
    LINKDING,
}
