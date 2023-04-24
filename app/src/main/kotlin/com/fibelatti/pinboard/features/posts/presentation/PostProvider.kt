@file:Suppress("PrivatePropertyName", "UNUSED")

package com.fibelatti.pinboard.features.posts.presentation

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import java.util.Random

private val LOREM_IPSUM_SOURCE: List<String> = """
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

Duis nec erat dolor. Nulla vitae consectetur ligula. Quisque nec mi est. Ut
quam ante, rutrum at pellentesque gravida, pretium in dui. Cras eget sapien
velit. Suspendisse ut sem nec tellus vehicula eleifend sit amet quis velit.
Phasellus quis suscipit nisi. Nam elementum malesuada tincidunt. Curabitur
iaculis pretium eros, malesuada faucibus leo eleifend a. Curabitur congue
orci in neque euismod a blandit libero vehicula.
""".trim().split(" ")

class PostProvider : PreviewParameterProvider<Post> {

    override val values: Sequence<Post>
        get() = sequenceOf(generatePost())

    private val random = Random()

    private fun generatePost(): Post {
        val value = random.nextInt()
        return Post(
            url = "https://post-$value.com",
            title = "Post #$value",
            description = LOREM_IPSUM_SOURCE.take(20).joinToString { " " },
            private = random.nextBoolean(),
            readLater = random.nextBoolean(),
            tags = LOREM_IPSUM_SOURCE.shuffled().take(3).map { Tag(name = it) },
        )
    }
}

class PostListProvider(val size: Int) : PreviewParameterProvider<List<Post>> {

    constructor() : this(10)

    override val values: Sequence<List<Post>>
        get() = sequenceOf(List(size, ::generatePost))

    private val random = Random()

    private fun generatePost(index: Int): Post = Post(
        url = "https://post-$index.com",
        title = "Post #$index",
        description = LOREM_IPSUM_SOURCE.take(20).joinToString { " " },
        private = random.nextBoolean(),
        readLater = random.nextBoolean(),
        tags = LOREM_IPSUM_SOURCE.shuffled().take(3).map { Tag(name = it) },
    )
}
