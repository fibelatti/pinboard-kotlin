package com.fibelatti.pinboard.features.posts.data.model

import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.createPostDto
import com.fibelatti.pinboard.core.AppConfig.PinboardApiLiterals
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PostDtoMapperTest {

    private val mapper = PostDtoMapper()

    @Nested
    inner class MapTests {
        @Test
        fun `GIVEN shared is no WHEN map is called THEN Post is returned AND private is true`() {
            mapper.map(createPostDto(shared = "no")) shouldBe createPost(private = true)
        }

        @Test
        fun `GIVEN shared is not yes WHEN map is called THEN Post is returned AND private is true`() {
            mapper.map(createPostDto(shared = "dsadsa")) shouldBe createPost(private = false)
        }

        @Test
        fun `GIVEN toread is yes WHEN map is called THEN Post is returned AND readLater is true`() {
            mapper.map(createPostDto(toread = "yes")) shouldBe createPost(readLater = true)
        }

        @Test
        fun `GIVEN toread is not yes WHEN map is called THEN Post is returned AND readLater is true`() {
            mapper.map(createPostDto(toread = "dsadsa")) shouldBe createPost(readLater = false)
        }

        @Test
        fun `GIVEN tags is empty WHEN map is called THEN Post is returned AND tags is null`() {
            mapper.map(createPostDto(tags = "")) shouldBe createPost(tags = null)
        }

        @Test
        fun `GIVEN tags contained html WHEN map is called THEN Post is returned AND tags is formatted`() {
            mapper.map(createPostDto(tags = "&lt;&gt;&quot;&amp;")) shouldBe createPost(tags = listOf(Tag("<>\"&")))
        }
    }

    @Nested
    inner class MapReverseTests {
        @Test
        fun `GIVEN private is true WHEN map is called THEN PostDto is returned AND shared is no`() {
            mapper.mapReverse(createPost(private = true)) shouldBe createPostDto(shared = PinboardApiLiterals.NO)
        }

        @Test
        fun `GIVEN private is false WHEN map is called THEN PostDto is returned AND shared is yes`() {
            mapper.mapReverse(createPost(private = false)) shouldBe createPostDto(shared = PinboardApiLiterals.YES)
        }

        @Test
        fun `GIVEN toread is true WHEN mapReverse is called THEN PostDto is returned AND toread is yes`() {
            mapper.mapReverse(createPost(readLater = true)) shouldBe createPostDto(toread = PinboardApiLiterals.YES)
        }

        @Test
        fun `GIVEN toread is false WHEN mapReverse is called THEN PostDto is returned AND toread is no`() {
            mapper.mapReverse(createPost(readLater = false)) shouldBe createPostDto(toread = PinboardApiLiterals.NO)
        }

        @Test
        fun `GIVEN tags is null WHEN mapReverse is called THEN PostDto is returned AND tags is empty`() {
            mapper.mapReverse(createPost(tags = null)) shouldBe createPostDto(tags = "")
        }
    }
}
