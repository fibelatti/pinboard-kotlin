package com.fibelatti.pinboard.features.posts.domain.model

/**
 * A model to wrap loaded bookmarks.
 *
 * @param totalCount the total count of bookmarks
 * @param posts the list of bookmarks
 * @param upToDate true if the content is up-to-date with the server, false otherwise
 */
data class PostListResult(
    val totalCount: Int,
    val posts: List<Post>,
    val upToDate: Boolean,
)
