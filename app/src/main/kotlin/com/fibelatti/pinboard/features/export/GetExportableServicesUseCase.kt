package com.fibelatti.pinboard.features.export

import com.fibelatti.core.functional.UseCase
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.features.linkding.data.BookmarksDao
import com.fibelatti.pinboard.features.posts.data.PostsDao
import javax.inject.Inject

/**
 * Reports which services still have bookmarks cached locally.
 *
 * Availability is derived from the tables themselves instead of the stored credentials since
 * logging out clears the tokens but keeps the cached bookmarks.
 */
class GetExportableServicesUseCase @Inject constructor(
    private val postsDao: PostsDao,
    private val bookmarksDao: BookmarksDao,
) : UseCase<Set<AppMode>> {

    override suspend fun invoke(): Set<AppMode> = buildSet {
        if (postsDao.getPostCount() > 0) add(AppMode.PINBOARD)
        if (bookmarksDao.getBookmarkCount() > 0) add(AppMode.LINKDING)
    }
}
