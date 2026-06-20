package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.pinboard.MockDataProvider
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_URL_VALID
import com.fibelatti.pinboard.core.network.ApiException
import com.fibelatti.pinboard.core.network.InvalidRequestException
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class UnarchivePostTest {

    private val mockPostsRepository = mockk<PostsRepository>()
    private val mockValidateUrl = mockk<ValidateUrl>()

    private val mockPost = MockDataProvider.createPost()

    private val unarchivePost = UnarchivePost(
        postsRepository = mockPostsRepository,
        validateUrl = mockValidateUrl,
    )

    @Test
    fun `GIVEN ValidateUrl fails WHEN UnarchivePost is called THEN Failure is returned`() = runTest {
        // GIVEN
        coEvery { mockValidateUrl(SAMPLE_URL_VALID) } returns Failure(InvalidRequestException())

        // WHEN
        val result = unarchivePost(mockPost)

        // THEN
        assertThat(result.exceptionOrNull()).isInstanceOf(InvalidRequestException::class.java)
    }

    @Test
    fun `GIVEN posts repository unarchive fails WHEN UnarchivePost is called THEN Failure is returned`() = runTest {
        // GIVEN
        coEvery { mockValidateUrl(SAMPLE_URL_VALID) } returns Success(SAMPLE_URL_VALID)
        coEvery { mockPostsRepository.unarchive(post = mockPost) } returns Failure(ApiException())

        // WHEN
        val result = unarchivePost(mockPost)

        // THEN
        assertThat(result.exceptionOrNull()).isInstanceOf(ApiException::class.java)
    }

    @Test
    fun `GIVEN posts repository unarchive succeeds WHEN UnarchivePost is called THEN Success is returned`() = runTest {
        // GIVEN
        val unarchivedPost = mockPost.copy(isArchived = false)
        coEvery { mockValidateUrl(SAMPLE_URL_VALID) } returns Success(SAMPLE_URL_VALID)
        coEvery { mockPostsRepository.unarchive(post = mockPost) } returns Success(unarchivedPost)

        // WHEN
        val result = unarchivePost(mockPost)

        // THEN
        assertThat(result.getOrNull()).isEqualTo(unarchivedPost)
    }
}
