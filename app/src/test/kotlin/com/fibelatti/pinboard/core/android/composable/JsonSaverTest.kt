package com.fibelatti.pinboard.core.android.composable

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class JsonSaverTest {

    private val saverScope = SaverScope { true }

    private val saver: Saver<Post?, Any> = jsonSaver(Post.serializer())

    private val post = Post(
        url = "https://www.url.com",
        title = "Title",
        description = "Description",
        tags = listOf(Tag(name = "tag", posts = 1)),
    )

    @Test
    fun `WHEN a value is saved THEN it is restored as an equal value`() {
        val saved = with(saver) { saverScope.save(post) }

        assertThat(saved).isNotNull()

        val restored = saver.restore(saved!!)

        assertThat(restored).isEqualTo(post)
        // `Tag` compares by name alone, so the remaining fields need an explicit assertion
        assertThat(restored?.tags?.single()?.posts).isEqualTo(1)
    }

    @Test
    fun `WHEN the value is null THEN nothing is saved`() {
        assertThat(with(saver) { saverScope.save(null) }).isNull()
    }

    @Test
    fun `WHEN the saved value no longer matches the model THEN null is restored`() {
        assertThat(saver.restore("""{"unknown":"value"}""")).isNull()
    }
}
