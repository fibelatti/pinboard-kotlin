package com.fibelatti.pinboard.features.appstate

sealed class ShouldLoad

/**
 * Local content is available, but still syncing with the backend.
 */
data object Syncing : ShouldLoad()

/**
 * Content is available and up-to-date.
 */
data object Loaded : ShouldLoad()

/**
 * Content changed and has to be reloaded from the start.
 */
data object ShouldLoadFirstPage : ShouldLoad()

/**
 * Forces a refresh with data from the server.
 */
data object ShouldForceLoad : ShouldLoad()

/**
 * The user is reaching the end of the content and the next page should be loaded.
 *
 * @param offset the position to start the next page
 */
data class ShouldLoadNextPage(val offset: Int) : ShouldLoad()
