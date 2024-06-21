@file:Suppress("PrivatePropertyName", "UNUSED")

package com.fibelatti.pinboard.features.posts.presentation

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.fibelatti.bookmarking.features.posts.domain.model.PendingSync
import com.fibelatti.bookmarking.features.posts.domain.model.Post
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import java.util.Random

private val LOREM_IPSUM_SOURCE: String = """
Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer sodales
laoreet commodo. Phasellus a purus eu risus elementum consequat. Aenean eu
elit ut nunc convallis laoreet non ut libero. Suspendisse interdum placerat
risus vel ornare. Donec vehicula, turpis sed consectetur ullamcorper, ante
nunc egestas quam, ultricies adipiscing velit enim at nunc. Aenean id diam
neque. Praesent ut lacus sed justo viverra fermentum et ut sem. Fusce
convallis gravida lacinia. Integer semper dolor ut elit sagittis lacinia.
Praesent sodales scelerisque eros at rhoncus. Duis posuere sapien vel ipsum
ornare interdum at eu quam. Vestibulum vel massa erat. Aenean quis sagittis
purus. Phasellus arcu purus, rutrum id consectetur non, bibendum at nibh.
""".trim()

class PostProvider : PreviewParameterProvider<Post> {

    override val values: Sequence<Post>
        get() = sequenceOf(generatePost())

    private val random = Random()

    private fun generatePost(): Post {
        val range = 0..9
        val value = range.random()
        return Post(
            url = "https://post-$value.com",
            title = "Post #$value",
            description = LOREM_IPSUM_SOURCE,
            private = random.nextBoolean(),
            readLater = random.nextBoolean(),
            tags = LOREM_IPSUM_SOURCE.split(" ").take(value).map { Tag(name = it) },
            pendingSync = when {
                value % 2 == 0 -> PendingSync.ADD
                value % 3 == 0 -> PendingSync.UPDATE
                value % 5 == 0 -> PendingSync.DELETE
                else -> null
            },
        )
    }
}

class PostListProvider(val size: Int) : PreviewParameterProvider<List<Post>> {

    constructor() : this(10)

    override val values: Sequence<List<Post>>
        get() = sequenceOf(List(size, ::generatePost))

    private val random = Random()

    private fun generatePost(index: Int): Post {
        val range = 0..9
        val value = range.random()
        return Post(
            url = "https://post-$index.com",
            title = "Post #$index",
            description = LOREM_IPSUM_SOURCE,
            private = random.nextBoolean(),
            readLater = random.nextBoolean(),
            tags = LOREM_IPSUM_SOURCE.split(" ").take(value).map { Tag(name = it) },
            pendingSync = when {
                value % 2 == 0 -> PendingSync.ADD
                value % 3 == 0 -> PendingSync.UPDATE
                value % 5 == 0 -> PendingSync.DELETE
                else -> null
            },
        )
    }
}
