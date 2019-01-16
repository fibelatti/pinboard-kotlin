package com.fibelatti.pinboard.features.posts.data.model

import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.createPostDto
import com.fibelatti.pinboard.core.AppConfig.PinboardApiLiterals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PostDtoMapperTest {

    private val mapper = PostDtoMapper()

    @Nested
    inner class MapTests {
        @Test
        fun `GIVEN shared is yes WHEN map is called THEN Post is returned AND public is true`() {
            mapper.map(createPostDto(shared = "yes")) shouldBe createPost(public = true)
        }

        @Test
        fun `GIVEN shared is not yes WHEN map is called THEN Post is returned AND public is true`() {
            mapper.map(createPostDto(shared = "dsadsa")) shouldBe createPost(public = false)
        }

        @Test
        fun `GIVEN toread is yes WHEN map is called THEN Post is returned AND unread is true`() {
            mapper.map(createPostDto(toread = "yes")) shouldBe createPost(unread = true)
        }

        @Test
        fun `GIVEN toread is not yes WHEN map is called THEN Post is returned AND unread is true`() {
            mapper.map(createPostDto(toread = "dsadsa")) shouldBe createPost(unread = false)
        }
    }

    @Nested
    inner class MapReverseTests {
        @Test
        fun `GIVEN public is true WHEN map is called THEN PostDto is returned AND shared is yes`() {
            mapper.mapReverse(createPost(public = true)) shouldBe createPostDto(shared = PinboardApiLiterals.YES)
        }

        @Test
        fun `GIVEN public is false WHEN map is called THEN PostDto is returned AND shared is no`() {
            mapper.mapReverse(createPost(public = false)) shouldBe createPostDto(shared = PinboardApiLiterals.NO)
        }

        @Test
        fun `GIVEN toread is true WHEN mapReverse is called THEN PostDto is returned AND toread is yes`() {
            mapper.mapReverse(createPost(unread = true)) shouldBe createPostDto(toread = PinboardApiLiterals.YES)
        }

        @Test
        fun `GIVEN toread is false WHEN mapReverse is called THEN PostDto is returned AND toread is no`() {
            mapper.mapReverse(createPost(unread = false)) shouldBe createPostDto(toread = PinboardApiLiterals.NO)
        }
    }
}
