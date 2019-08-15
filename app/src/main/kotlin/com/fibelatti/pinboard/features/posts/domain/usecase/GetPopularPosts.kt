package com.fibelatti.pinboard.features.posts.domain.usecase

import androidx.annotation.VisibleForTesting
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCase
import com.fibelatti.core.functional.catching
import com.fibelatti.core.functional.retryIO
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import javax.inject.Inject

@VisibleForTesting
const val PINBOARD_URL_POPULAR = "https://pinboard.in/popular/"
@VisibleForTesting
const val PINBOARD_CLASS_BOOKMARK = "bookmark"
@VisibleForTesting
const val PINBOARD_CLASS_BOOKMARK_TITLE = "bookmark_title"
@VisibleForTesting
const val PINBOARD_CLASS_TAG = "tag"
@VisibleForTesting
const val ATTR_HREF = "href"

class GetPopularPosts @Inject constructor() : UseCase<List<Post>>() {

    override suspend fun run(): Result<List<Post>> {
        return catching {
            val document = getDocument()
            val bookmarks = document.getElementsByClass(PINBOARD_CLASS_BOOKMARK)

            bookmarks.mapNotNull { element ->
                val titleAndUrl = element.getElementsByClass(PINBOARD_CLASS_BOOKMARK_TITLE)
                    .firstOrNull()
                    ?: return@mapNotNull null

                val tags = element.getElementsByClass(PINBOARD_CLASS_TAG)
                    .map { Tag(it.text()) }
                    .takeIf { it.isNotEmpty() }

                createPost(
                    url = titleAndUrl.attr(ATTR_HREF),
                    title = titleAndUrl.text(),
                    tags = tags
                )
            }
        }
    }

    @VisibleForTesting
    suspend fun getDocument(): Document = withContext(Dispatchers.IO) {
        retryIO { Jsoup.connect(PINBOARD_URL_POPULAR).get() }
    }

    @VisibleForTesting
    fun createPost(url: String, title: String, tags: List<Tag>?): Post {
        return Post(
            url = url,
            title = title,
            description = "",
            hash = "",
            time = "",
            private = false,
            readLater = false,
            tags = tags
        )
    }
}
