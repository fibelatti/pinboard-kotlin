package com.fibelatti.pinboard.features.posts.domain.model

/**
 * A model to wrap loaded bookmarks.
 *
 * @param posts the list of bookmarks
 * @param totalCount the total count of bookmarks
 * @param upToDate true if the content is up-to-date with the server, false otherwise
 * @param canPaginate true if there might be more content pages available, false otherwise
 */
data class PostListResult(
    val posts: List<Post>,
    val totalCount: Int,
    val upToDate: Boolean,
    val canPaginate: Boolean,
)
