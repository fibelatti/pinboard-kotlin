package com.fibelatti.pinboard.features.user.domain

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.test.extension.callSuspend
import com.fibelatti.core.test.extension.givenSuspend
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBeAnInstanceOf
import com.fibelatti.pinboard.MockDataProvider.mockApiToken
import com.fibelatti.pinboard.MockDataProvider.mockTime
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class LoginTest {

    private val mockUserRepository = mock<UserRepository>()
    private val mockPostsRepository = mock<PostsRepository>()

    private val login = Login(
        mockUserRepository,
        mockPostsRepository
    )

    @Test
    fun `GIVEN repository call fails WHEN Login is called THEN loggedIn is never called`() {
        // GIVEN
        givenSuspend { mockPostsRepository.update() }
            .willReturn(Failure(Exception()))

        // WHEN
        val result = callSuspend { login(Login.Params(mockApiToken)) }

        // THEN
        result.shouldBeAnInstanceOf<Failure>()
        verify(mockUserRepository).loginAttempt(mockApiToken)
        verify(mockUserRepository, never()).loggedIn()
    }

    @Test
    fun `GIVEN repository call is successful WHEN Login is called THEN Success is returned and user is logged in`() {
        // GIVEN
        givenSuspend { mockPostsRepository.update() }
            .willReturn(Success(mockTime))

        // WHEN
        val result = callSuspend { login(Login.Params(mockApiToken)) }

        // THEN
        result.shouldBeAnInstanceOf<Success<Unit>>()
        verify(mockUserRepository).loginAttempt(mockApiToken)
        verify(mockUserRepository).loggedIn()
    }
}
