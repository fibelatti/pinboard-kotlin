package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.pinboard.MockDataProvider.mockTagString1
import com.fibelatti.pinboard.MockDataProvider.mockTagString2
import com.fibelatti.pinboard.MockDataProvider.mockTagString3
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

internal class GetSuggestedTagsTest {

    private val mockPostsRepository = mockk<PostsRepository>()

    private val getSuggestedTags = GetSuggestedTags(mockPostsRepository)

    @Test
    fun `WHEN repository fails THEN Failure is returned`() {
        // GIVEN
        val expectedResult = Failure(Exception())
        coEvery { mockPostsRepository.searchExistingPostTag(any()) } returns expectedResult

        // WHEN
        val result = runBlocking {
            getSuggestedTags(GetSuggestedTags.Params("any-value", mockk()))
        }

        // THEN
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `WHEN repository succeeds THEN tags different than currentTags are returned`() {
        // GIVEN
        coEvery { mockPostsRepository.searchExistingPostTag(any()) } returns Success(
            listOf(
                mockTagString1,
                mockTagString2,
                mockTagString3
            )
        )

        // WHEN
        val result = runBlocking {
            getSuggestedTags(GetSuggestedTags.Params("any-value", listOf(Tag(mockTagString1))))
        }

        // THEN
        assertThat(result.getOrNull()).isEqualTo(listOf(mockTagString2, mockTagString3))
    }
}
