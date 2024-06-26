package com.fibelatti.bookmarking.features.appstate

public sealed class ShouldLoad

/**
 * Local content is available, but still syncing with the backend.
 */
public data object Syncing : ShouldLoad()

/**
 * Content is available and up-to-date.
 */
public data object Loaded : ShouldLoad()

/**
 * Content changed and has to be reloaded from the start.
 */
public data object ShouldLoadFirstPage : ShouldLoad()

/**
 * Forces a refresh with data from the server.
 */
public data object ShouldForceLoad : ShouldLoad()

/**
 * The user is reaching the end of the content and the next page should be loaded.
 *
 * @param offset the position to start the next page
 */
public data class ShouldLoadNextPage(val offset: Int) : ShouldLoad()
