package com.fibelatti.pinboard.features.posts.data.model

import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.createPostDto
import com.fibelatti.pinboard.core.AppConfig.PinboardApiLiterals
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PostDtoMapperTest {

    private val mapper = PostDtoMapper()

    @Nested
    inner class MapTests {

        @Test
        fun `GIVEN url has unescaped % characters THEN Post is returned AND the url is fixed`() {
            val url = "http://example.com/test?q=%1$"

            assertThat(mapper.map(createPostDto(href = url))).isEqualTo(createPost(url = url))
        }

        @Test
        fun `GIVEN shared is no WHEN map is called THEN Post is returned AND private is true`() {
            assertThat(mapper.map(createPostDto(shared = "no"))).isEqualTo(createPost(private = true))
        }

        @Test
        fun `GIVEN shared is not yes WHEN map is called THEN Post is returned AND private is true`() {
            assertThat(mapper.map(createPostDto(shared = "dsadsa"))).isEqualTo(createPost(private = false))
        }

        @Test
        fun `GIVEN toread is yes WHEN map is called THEN Post is returned AND readLater is true`() {
            assertThat(mapper.map(createPostDto(toread = "yes"))).isEqualTo(createPost(readLater = true))
        }

        @Test
        fun `GIVEN toread is not yes WHEN map is called THEN Post is returned AND readLater is true`() {
            assertThat(mapper.map(createPostDto(toread = "dsadsa"))).isEqualTo(createPost(readLater = false))
        }

        @Test
        fun `GIVEN tags is empty WHEN map is called THEN Post is returned AND tags is null`() {
            assertThat(mapper.map(createPostDto(tags = ""))).isEqualTo(createPost(tags = null))
        }

        @Test
        fun `GIVEN tags contained html WHEN map is called THEN Post is returned AND tags is formatted`() {
            assertThat(mapper.map(createPostDto(tags = "&lt;&gt;&quot;&amp;"))).isEqualTo(
                createPost(
                    tags = listOf(Tag("<>\"&"))
                )
            )
        }
    }

    @Nested
    inner class MapReverseTests {

        @Test
        fun `GIVEN private is true WHEN map is called THEN PostDto is returned AND shared is no`() {
            assertThat(mapper.mapReverse(createPost(private = true))).isEqualTo(createPostDto(shared = PinboardApiLiterals.NO))
        }

        @Test
        fun `GIVEN private is false WHEN map is called THEN PostDto is returned AND shared is yes`() {
            assertThat(mapper.mapReverse(createPost(private = false))).isEqualTo(
                createPostDto(
                    shared = PinboardApiLiterals.YES
                )
            )
        }

        @Test
        fun `GIVEN toread is true WHEN mapReverse is called THEN PostDto is returned AND toread is yes`() {
            assertThat(mapper.mapReverse(createPost(readLater = true))).isEqualTo(
                createPostDto(
                    toread = PinboardApiLiterals.YES
                )
            )
        }

        @Test
        fun `GIVEN toread is false WHEN mapReverse is called THEN PostDto is returned AND toread is no`() {
            assertThat(mapper.mapReverse(createPost(readLater = false))).isEqualTo(
                createPostDto(
                    toread = PinboardApiLiterals.NO
                )
            )
        }

        @Test
        fun `GIVEN tags is null WHEN mapReverse is called THEN PostDto is returned AND tags is empty`() {
            assertThat(mapper.mapReverse(createPost(tags = null))).isEqualTo(createPostDto(tags = ""))
        }
    }
}
