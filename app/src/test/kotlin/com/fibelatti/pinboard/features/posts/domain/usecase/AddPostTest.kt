package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.test.extension.callSuspend
import com.fibelatti.core.test.extension.givenSuspend
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBeAnInstanceOf
import com.fibelatti.pinboard.MockDataProvider.mockUrlTitle
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.fibelatti.pinboard.core.network.ApiException
import com.fibelatti.pinboard.core.network.InvalidRequestException
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import org.junit.jupiter.api.Test

class AddPostTest {

    private val mockPostsRepository = mock<PostsRepository>()
    private val mockValidateUrl = mock<ValidateUrl>()

    private val params = AddPost.Params(mockUrlValid, mockUrlTitle)

    private val addPost = AddPost(
        mockPostsRepository,
        mockValidateUrl
    )

    @Test
    fun `GIVEN ValidateUrl fails WHEN AddPost is called THEN Failure is returned`() {
        // GIVEN
        givenSuspend { mockValidateUrl(mockUrlValid) }
            .willReturn(Failure(InvalidRequestException()))

        // WHEN
        val result = callSuspend { addPost(params) }

        // THEN
        result.shouldBeAnInstanceOf<Failure>()
        result.exceptionOrNull()?.shouldBeAnInstanceOf<InvalidRequestException>()
    }

    @Test
    fun `GIVEN posts repository add fails WHEN AddPost is called THEN Failure is returned`() {
        // GIVEN
        givenSuspend { mockValidateUrl(mockUrlValid) }
            .willReturn(Success(mockUrlValid))
        givenSuspend {
            mockPostsRepository.add(
                params.url,
                params.title,
                params.description,
                params.private,
                params.readLater,
                params.tags
            )
        }.willReturn(Failure(ApiException()))

        // WHEN
        val result = callSuspend { addPost(AddPost.Params(mockUrlValid, mockUrlTitle)) }

        // THEN
        result.shouldBeAnInstanceOf<Failure>()
        result.exceptionOrNull()?.shouldBeAnInstanceOf<ApiException>()
    }

    @Test
    fun `GIVEN posts repository add succeeds WHEN AddPost is called THEN Success is returned`() {
        // GIVEN
        givenSuspend { mockValidateUrl(mockUrlValid) }
            .willReturn(Success(mockUrlValid))
        givenSuspend {
            mockPostsRepository.add(
                params.url,
                params.title,
                params.description,
                params.private,
                params.readLater,
                params.tags
            )
        }.willReturn(Success(Unit))

        // WHEN
        val result = callSuspend { addPost(AddPost.Params(mockUrlValid, mockUrlTitle)) }

        // THEN
        result.shouldBeAnInstanceOf<Success<Unit>>()
    }
}
