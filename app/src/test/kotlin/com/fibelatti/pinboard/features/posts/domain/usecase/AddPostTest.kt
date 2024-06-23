package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.bookmarking.features.posts.domain.model.Post
import com.fibelatti.bookmarking.test.MockDataProvider.MOCK_URL_VALID
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.pinboard.core.network.ApiException
import com.fibelatti.pinboard.core.network.InvalidRequestException
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class AddPostTest {

    private val mockPostsRepository = mockk<PostsRepository>()
    private val mockValidateUrl = mockk<ValidateUrl>()

    private val params = mockk<Post> {
        every { url } returns MOCK_URL_VALID
    }

    private val addPost = AddPost(
        postsRepository = mockPostsRepository,
        validateUrl = mockValidateUrl,
    )

    @Test
    fun `GIVEN ValidateUrl fails WHEN AddPost is called THEN Failure is returned`() = runTest {
        // GIVEN
        coEvery { mockValidateUrl(MOCK_URL_VALID) } returns Failure(InvalidRequestException())

        // WHEN
        val result = addPost(params)

        // THEN
        assertThat(result.exceptionOrNull()).isInstanceOf(InvalidRequestException::class.java)
        coVerify { mockPostsRepository wasNot Called }
    }

    @Test
    fun `GIVEN posts repository add fails WHEN AddPost is called THEN Failure is returned`() = runTest {
        // GIVEN
        coEvery { mockValidateUrl(MOCK_URL_VALID) } returns Success(MOCK_URL_VALID)
        coEvery { mockPostsRepository.add(post = params) } returns Failure(ApiException())

        // WHEN
        val result = addPost(params)

        // THEN
        assertThat(result.exceptionOrNull()).isInstanceOf(ApiException::class.java)
        coVerify(exactly = 0) { mockPostsRepository.getPost(id = any(), url = any()) }
    }

    @Test
    fun `GIVEN posts repository add succeeds WHEN AddPost is called THEN Success is returned`() = runTest {
        // GIVEN
        val mockPost = mockk<Post>()
        coEvery { mockValidateUrl(MOCK_URL_VALID) } returns Success(MOCK_URL_VALID)
        coEvery { mockPostsRepository.add(post = params) } returns Success(mockPost)

        // WHEN
        val result = addPost(params)

        // THEN
        assertThat(result.getOrNull()).isEqualTo(mockPost)
    }
}
