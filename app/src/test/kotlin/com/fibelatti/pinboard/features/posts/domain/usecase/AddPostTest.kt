package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.mockUrlTitle
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.fibelatti.pinboard.core.network.ApiException
import com.fibelatti.pinboard.core.network.InvalidRequestException
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class AddPostTest {

    private val mockPostsRepository = mockk<PostsRepository>()
    private val mockValidateUrl = mockk<ValidateUrl>()

    private val params = AddPost.Params(mockUrlValid, mockUrlTitle)

    private val addPost = AddPost(
        mockPostsRepository,
        mockValidateUrl,
    )

    @Test
    fun `GIVEN ValidateUrl fails WHEN AddPost is called THEN Failure is returned`() = runTest {
        // GIVEN
        coEvery { mockValidateUrl(mockUrlValid) } returns Failure(InvalidRequestException())

        // WHEN
        val result = addPost(params)

        // THEN
        assertThat(result.exceptionOrNull()).isInstanceOf(InvalidRequestException::class.java)
        coVerify { mockPostsRepository wasNot Called }
    }

    @Test
    fun `GIVEN posts repository add fails WHEN AddPost is called THEN Failure is returned`() = runTest {
        // GIVEN
        coEvery { mockValidateUrl(mockUrlValid) } returns Success(mockUrlValid)
        coEvery {
            mockPostsRepository.add(
                url = params.url,
                title = params.title,
                description = params.description,
                private = params.private,
                readLater = params.readLater,
                tags = params.tags,
                replace = params.replace,
                hash = params.hash,
            )
        } returns Failure(ApiException())

        // WHEN
        val result = addPost(AddPost.Params(mockUrlValid, mockUrlTitle))

        // THEN
        assertThat(result.exceptionOrNull()).isInstanceOf(ApiException::class.java)
        coVerify(exactly = 0) { mockPostsRepository.getPost(any()) }
    }

    @Test
    fun `GIVEN posts repository add succeeds WHEN AddPost is called THEN Success is returned`() = runTest {
        // GIVEN
        val mockPost = mockk<Post>()
        coEvery { mockValidateUrl(mockUrlValid) } returns Success(mockUrlValid)
        coEvery {
            mockPostsRepository.add(
                url = params.url,
                title = params.title,
                description = params.description,
                private = params.private,
                readLater = params.readLater,
                tags = params.tags,
                replace = params.replace,
                hash = params.hash,
            )
        } returns Success(mockPost)

        // WHEN
        val result = addPost(AddPost.Params(mockUrlValid, mockUrlTitle))

        // THEN
        assertThat(result.getOrNull()).isEqualTo(mockPost)
    }

    @Test
    fun `GIVEN Params secondary constructor is called THEN the Params is instantiate with the correct values`() {
        val testPost = createPost()

        val params = AddPost.Params(testPost)

        assertThat(testPost.url).isEqualTo(params.url)
        assertThat(testPost.title).isEqualTo(params.title)
        assertThat(testPost.description).isEqualTo(params.description)
        assertThat(testPost.private).isEqualTo(params.private)
        assertThat(testPost.readLater).isEqualTo(params.readLater)
        assertThat(testPost.tags).isEqualTo(params.tags)
    }
}
