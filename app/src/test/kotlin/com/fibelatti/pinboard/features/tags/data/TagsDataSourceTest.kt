package com.fibelatti.pinboard.features.tags.data

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.test.extension.callSuspend
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.core.test.extension.shouldBeAnInstanceOf
import com.fibelatti.pinboard.MockDataProvider.createErrorResponse
import com.fibelatti.pinboard.MockDataProvider.createResponse
import com.fibelatti.pinboard.core.network.ApiException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given

class TagsDataSourceTest {

    private val mockApi = mock<TagsApi>()
    private val dataSource = TagsDataSource(mockApi)

    @Nested
    inner class GetAllTagsTests {

        @Test
        fun `GIVEN getTags returns an error WHEN getAllTags is called THEN Failure is returned`() {
            // GIVEN
            given(mockApi.getTags())
                .willReturn(createErrorResponse())

            // WHEN
            val result = callSuspend { dataSource.getAllTags() }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<ApiException>()
        }

        @Test
        fun `GIVEN getTags returns a map with an invalid value WHEN getAllTags is called THEN Failure is returned`() {
            // GIVEN
            given(mockApi.getTags())
                .willReturn(createResponse(emptyMap()))

            // WHEN
            val result = callSuspend { dataSource.getAllTags() }

            // THEN
            result.shouldBeAnInstanceOf<Success<*>>()
            result.getOrNull() shouldBe emptyMap<String, Int>()
        }

        @Test
        fun `GIVEN getTags returns an empty map WHEN getAllTags is called THEN Success is returned`() {
            // GIVEN
            given(mockApi.getTags())
                .willReturn(createResponse(mapOf("tag" to "a")))

            // WHEN
            val result = callSuspend { dataSource.getAllTags() }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<NumberFormatException>()
        }

        @Test
        fun `WHEN getAllTags is called THEN Success is returned`() {
            // GIVEN
            given(mockApi.getTags())
                .willReturn(createResponse(mapOf("tag" to "1")))

            // WHEN
            val result = callSuspend { dataSource.getAllTags() }

            // THEN
            result.shouldBeAnInstanceOf<Success<*>>()
            result.getOrNull() shouldBe mapOf("tag" to 1)
        }
    }
}
