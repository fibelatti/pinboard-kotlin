package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.test.extension.callSuspend
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.core.test.extension.shouldBeAnInstanceOf
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.mockTag1
import com.fibelatti.pinboard.MockDataProvider.mockTag2
import com.fibelatti.pinboard.MockDataProvider.mockTime1
import com.fibelatti.pinboard.MockDataProvider.mockTime2
import com.fibelatti.pinboard.MockDataProvider.mockTime3
import com.fibelatti.pinboard.MockDataProvider.mockTime4
import com.fibelatti.pinboard.MockDataProvider.mockTime5
import com.fibelatti.pinboard.MockDataProvider.mockUrlTitle
import com.fibelatti.pinboard.MockDataProvider.mockUrlDescription
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.fibelatti.pinboard.features.posts.domain.model.Post
import org.junit.jupiter.api.Test

class FilterPostsTest {

    // region Mock data
    private val postMultiTags = createPost(
        time = mockTime1,
        url = "",
        title = "",
        description = "",
        tags = listOf(mockTag1, mockTag2)
    )
    private val postSingleTag = createPost(
        time = mockTime2,
        url = "",
        title = "",
        description = "",
        tags = listOf(mockTag2)
    )
    private val postUrl = createPost(
        time = mockTime3,
        url = mockUrlValid,
        title = "",
        description = "",
        tags = emptyList()
    )
    private val postDescription = createPost(
        time = mockTime4,
        url = "",
        title = mockUrlTitle,
        description = "",
        tags = emptyList()
    )
    private val postExtended = createPost(
        time = mockTime5,
        url = "",
        title = "",
        description = mockUrlDescription,
        tags = emptyList()
    )

    private val mockDataSet = listOf(postMultiTags, postSingleTag, postUrl, postDescription, postExtended)
    // endregion

    private val filterPosts = FilterPosts()

    @Test
    fun `GIVEN Params term is empty and tags is null WHEN FilterPosts is called THEN all Params posts are returned`() {
        // WHEN
        val result = callSuspend { filterPosts(FilterPosts.Params(mockDataSet, term = "", tags = emptyList())) }

        // THEN
        result.shouldBeAnInstanceOf<Success<List<Post>>>()
        result.getOrNull() shouldBe mockDataSet
    }

    @Test
    fun `GIVEN Params term is empty and tags is empty WHEN FilterPosts is called THEN all Params posts are returned`() {
        // WHEN
        val result = callSuspend { filterPosts(FilterPosts.Params(mockDataSet, term = "", tags = emptyList())) }

        // THEN
        result.shouldBeAnInstanceOf<Success<List<Post>>>()
        result.getOrNull() shouldBe mockDataSet
    }

    @Test
    fun `GIVEN Params tags contains values WHEN FilterPosts is called THEN only posts with all filter tags are returned`() {
        // GIVEN
        val expectedResult = listOf(postMultiTags)

        // WHEN
        val result =
            callSuspend { filterPosts(FilterPosts.Params(mockDataSet, term = "", tags = listOf(mockTag1, mockTag2))) }

        // THEN
        result.shouldBeAnInstanceOf<Success<List<Post>>>()
        result.getOrNull() shouldBe expectedResult
    }

    @Test
    fun `GIVEN Params term is not empty WHEN FilterPosts is called THEN posts that contains the term in the url are returned`() {
        // GIVEN
        val expectedResult = listOf(postUrl)

        // WHEN
        val result =
            callSuspend { filterPosts(FilterPosts.Params(mockDataSet, term = "www", tags = emptyList())) }

        // THEN
        result.shouldBeAnInstanceOf<Success<List<Post>>>()
        result.getOrNull() shouldBe expectedResult
    }

    @Test
    fun `GIVEN Params term is not empty WHEN FilterPosts is called THEN posts that contains the term in the title are returned`() {
        // GIVEN
        val expectedResult = listOf(postDescription)

        // WHEN
        val result =
            callSuspend { filterPosts(FilterPosts.Params(mockDataSet, term = "title", tags = emptyList())) }

        // THEN
        result.shouldBeAnInstanceOf<Success<List<Post>>>()
        result.getOrNull() shouldBe expectedResult
    }

    @Test
    fun `GIVEN Params term is not empty WHEN FilterPosts is called THEN posts that contains the term in the description are returned`() {
        // GIVEN
        val expectedResult = listOf(postExtended)

        // WHEN
        val result =
            callSuspend { filterPosts(FilterPosts.Params(mockDataSet, term = "about", tags = emptyList())) }

        // THEN
        result.shouldBeAnInstanceOf<Success<List<Post>>>()
        result.getOrNull() shouldBe expectedResult
    }
}
