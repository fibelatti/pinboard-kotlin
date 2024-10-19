package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.pinboard.MockDataProvider
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.fibelatti.pinboard.core.network.ApiException
import com.fibelatti.pinboard.core.network.InvalidRequestException
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DeletePostTest {

    private val mockPostsRepository = mockk<PostsRepository>()
    private val mockValidateUrl = mockk<ValidateUrl>()

    private val mockPost = MockDataProvider.createPost()

    private val deletePost = DeletePost(
        postsRepository = mockPostsRepository,
        validateUrl = mockValidateUrl,
    )

    @Test
    fun `GIVEN ValidateUrl fails WHEN AddPost is called THEN Failure is returned`() = runTest {
        // GIVEN
        coEvery { mockValidateUrl(mockUrlValid) } returns Failure(InvalidRequestException())

        // WHEN
        val result = deletePost(mockPost)

        // THEN
        assertThat(result.exceptionOrNull()).isInstanceOf(InvalidRequestException::class.java)
    }

    @Test
    fun `GIVEN posts repository add fails WHEN AddPost is called THEN Failure is returned`() = runTest {
        // GIVEN
        coEvery { mockValidateUrl(mockUrlValid) } returns Success(mockUrlValid)
        coEvery { mockPostsRepository.delete(post = mockPost) } returns Failure(ApiException())

        // WHEN
        val result = deletePost(mockPost)

        // THEN
        assertThat(result.exceptionOrNull()).isInstanceOf(ApiException::class.java)
    }

    @Test
    fun `GIVEN posts repository add succeeds WHEN AddPost is called THEN Success is returned`() = runTest {
        // GIVEN
        coEvery { mockValidateUrl(mockUrlValid) } returns Success(mockUrlValid)
        coEvery { mockPostsRepository.delete(post = mockPost) } returns Success(Unit)

        // WHEN
        val result = deletePost(mockPost)

        // THEN
        assertThat(result.getOrNull()).isEqualTo(Unit)
    }
}
