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
import com.fibelatti.pinboard.features.appstate.NewestFirst
import com.fibelatti.pinboard.features.appstate.OldestFirst
import com.fibelatti.pinboard.features.posts.domain.model.Post
import org.junit.jupiter.api.Test

class SortTest {

    private val mockResponseFull = listOf(
        createPost(time = mockTime1, tags = listOf(mockTag1)),
        createPost(time = mockTime2, tags = listOf(mockTag2)),
        createPost(time = mockTime3, tags = listOf(mockTag3)),
        createPost(time = mockTime4, tags = listOf(mockTag4))
    )
    private val mockResponseExpectedOldestFirst = listOf(
        createPost(time = mockTime1, tags = listOf(mockTag1)),
        createPost(time = mockTime2, tags = listOf(mockTag2)),
        createPost(time = mockTime3, tags = listOf(mockTag3)),
        createPost(time = mockTime4, tags = listOf(mockTag4))
    )
    private val mockResponseExpectedNewestFirst = listOf(
        createPost(time = mockTime4, tags = listOf(mockTag4)),
        createPost(time = mockTime3, tags = listOf(mockTag3)),
        createPost(time = mockTime2, tags = listOf(mockTag2)),
        createPost(time = mockTime1, tags = listOf(mockTag1))
    )

    private val sort = Sort()

    @Test
    fun `GIVEN sorting is NewestFirst WHEN Sort is called THEN list is returned sorted by time descending`() {
        // WHEN
        val result = callSuspend { sort(Sort.Params(mockResponseFull, NewestFirst)) }

        // THEN
        result.shouldBeAnInstanceOf<Success<List<Post>>>()
        result.getOrNull() shouldBe mockResponseExpectedNewestFirst
    }

    @Test
    fun `GIVEN sorting is OldestFirst WHEN Sort is called THEN list is returned sorted by time ascending`() {
        // WHEN
        val result = callSuspend { sort(Sort.Params(mockResponseFull, OldestFirst)) }

        // THEN
        result.shouldBeAnInstanceOf<Success<List<Post>>>()
        result.getOrNull() shouldBe mockResponseExpectedOldestFirst
    }
}
