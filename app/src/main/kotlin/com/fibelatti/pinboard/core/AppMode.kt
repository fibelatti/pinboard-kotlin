package com.fibelatti.pinboard.core

enum class AppMode {

    /**
     * The user has not logged in yet so the app mode cannot be determined.
     */
    UNSET,

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
