package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.test.extension.givenSuspend
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.MockDataProvider.mockTagString1
import com.fibelatti.pinboard.MockDataProvider.mockTagString2
import com.fibelatti.pinboard.MockDataProvider.mockTagString3
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString

internal class GetSuggestedTagsTest {

    private val mockPostsRepository = mock<PostsRepository>()

    private val getSuggestedTags = GetSuggestedTags(mockPostsRepository)

    @Test
    fun `WHEN repository fails THEN Failure is returned`() {
        // GIVEN
        val expectedResult = Failure(Exception())
        givenSuspend { mockPostsRepository.searchExistingPostTag(anyString()) }
            .willReturn(expectedResult)

        // WHEN
        val result = runBlocking {
            getSuggestedTags(GetSuggestedTags.Params("any-value", mock()))
        }

        // THEN
        result shouldBe expectedResult
    }

    @Test
    fun `WHEN repository succeeds THEN tags different than currentTags are returned`() {
        // GIVEN
        givenSuspend { mockPostsRepository.searchExistingPostTag(anyString()) }
            .willReturn(Success(listOf(mockTagString1, mockTagString2, mockTagString3)))

        // WHEN
        val result = runBlocking {
            getSuggestedTags(GetSuggestedTags.Params("any-value", listOf(Tag(mockTagString1))))
        }

        // THEN
        result.getOrNull() shouldBe listOf(mockTagString2, mockTagString3)
    }
}
