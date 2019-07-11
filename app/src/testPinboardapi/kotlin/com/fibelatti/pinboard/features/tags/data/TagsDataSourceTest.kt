package com.fibelatti.pinboard.features.tags.data

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.test.extension.callSuspend
import com.fibelatti.core.test.extension.givenSuspend
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.core.test.extension.shouldBeAnInstanceOf
import com.fibelatti.pinboard.TestRateLimitRunner
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TagsDataSourceTest {

    private val mockApi = mock<TagsApi>()
    private val mockRunner = TestRateLimitRunner()

    private val dataSource = TagsDataSource(mockApi, mockRunner)

    @Nested
    inner class GetAllTagsTests {

        @Test
        fun `GIVEN getTags returns an error WHEN getAllTags is called THEN Failure is returned`() {
            // GIVEN
            givenSuspend { mockApi.getTags() }
                .willAnswer { throw Exception() }

            // WHEN
            val result = callSuspend { dataSource.getAllTags() }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<Exception>()
        }

        @Test
        fun `GIVEN getTags returns a map with an invalid value WHEN getAllTags is called THEN Failure is returned`() {
            // GIVEN
            givenSuspend { mockApi.getTags() }
                .willReturn(mapOf("tag" to "a"))

            // WHEN
            val result = callSuspend { dataSource.getAllTags() }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<NumberFormatException>()
        }

        @Test
        fun `GIVEN getTags returns an empty map WHEN getAllTags is called THEN Success is returned`() {
            // GIVEN
            givenSuspend { mockApi.getTags() }
                .willReturn(emptyMap())

            // WHEN
            val result = callSuspend { dataSource.getAllTags() }

            // THEN
            result.shouldBeAnInstanceOf<Success<*>>()
            result.getOrNull() shouldBe emptyList<Tag>()
        }

        @Test
        fun `WHEN getAllTags is called THEN Success is returned`() {
            // GIVEN
            givenSuspend { mockApi.getTags() }
                .willReturn(mapOf("tag" to "1"))

            // WHEN
            val result = callSuspend { dataSource.getAllTags() }

            // THEN
            result.shouldBeAnInstanceOf<Success<*>>()
            result.getOrNull() shouldBe listOf(Tag("tag", 1))
        }
    }
}
