package com.fibelatti.pinboard.features.appstate

sealed class ShouldLoad

/**
 * Local content is available, but still syncing with the backend.
 */
object Syncing : ShouldLoad()

/**
 * Content is available and up-to-date.
 */
object Loaded : ShouldLoad()

/**
 * Content changed and has to be reloaded from the start.
 */
object ShouldLoadFirstPage : ShouldLoad()

/**
 * The user is reaching the end of the content and the next page should be loaded.
 *
 * @param offset the position to start the next page
 */
data class ShouldLoadNextPage(val offset: Int) : ShouldLoad()
