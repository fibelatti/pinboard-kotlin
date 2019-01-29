package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.test.extension.callSuspend
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.core.test.extension.shouldBeAnInstanceOf
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.mockTag1
import com.fibelatti.pinboard.MockDataProvider.mockTag2
import com.fibelatti.pinboard.MockDataProvider.mockTag3
import com.fibelatti.pinboard.MockDataProvider.mockTag4
import com.fibelatti.pinboard.MockDataProvider.mockTime1
import com.fibelatti.pinboard.MockDataProvider.mockTime2
import com.fibelatti.pinboard.MockDataProvider.mockTime3
import com.fibelatti.pinboard.MockDataProvider.mockTime4
import com.fibelatti.pinboard.features.posts.domain.model.Post
import org.junit.jupiter.api.Test

class FilterTagsTest {

    private val mockDataSet = listOf(
        createPost(time = mockTime1, tags = listOf(mockTag1)),
        createPost(time = mockTime2, tags = listOf(mockTag2)),
        createPost(time = mockTime3, tags = listOf(mockTag3)),
        createPost(time = mockTime4, tags = listOf(mockTag4))
    )

    private val filterTags = FilterTags()

    @Test
    fun `GIVEN Params tags is null WHEN FilterTags is called THEN all Params posts are returned`() {
        // WHEN
        val result = callSuspend { filterTags(FilterTags.Params(mockDataSet, tags = null)) }

        // THEN
        result.shouldBeAnInstanceOf<Success<List<Post>>>()
        result.getOrNull() shouldBe mockDataSet
    }

    @Test
    fun `GIVEN Params tags contains values WHEN FilterTags is called THEN only posts with at least one of those tags are returned`() {
        // GIVEN
        val expectedResult = listOf(
            createPost(time = mockTime1, tags = listOf(mockTag1)),
            createPost(time = mockTime2, tags = listOf(mockTag2))
        )

        // WHEN
        val result = callSuspend { filterTags(FilterTags.Params(mockDataSet, tags = listOf(mockTag1, mockTag2))) }

        // THEN
        result.shouldBeAnInstanceOf<Success<List<Post>>>()
        result.getOrNull() shouldBe expectedResult
    }

    @Test
    fun `GIVEN Params tags is empty WHEN FilterTags is called THEN all Params posts are returned`() {
        // WHEN
        val result = callSuspend { filterTags(FilterTags.Params(mockDataSet, tags = emptyList())) }

        // THEN
        result.shouldBeAnInstanceOf<Success<List<Post>>>()
        result.getOrNull() shouldBe mockDataSet
    }
}
