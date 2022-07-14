package com.fibelatti.pinboard.features.user.domain

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.pinboard.MockDataProvider.mockApiToken
import com.fibelatti.pinboard.MockDataProvider.mockTime
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.UserLoggedIn
import com.fibelatti.pinboard.features.appstate.UserLoggedOut
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class LoginTest {

    private val mockUserRepository = mockk<UserRepository>(relaxed = true)
    private val mockAppStateRepository = mockk<AppStateRepository>(relaxUnitFun = true)
    private val mockPostsRepository = mockk<PostsRepository>()

    private val login = Login(
        mockUserRepository,
        mockAppStateRepository,
        mockPostsRepository,
    )

    @Test
    fun `GIVEN repository call fails WHEN Login is called THEN UserLoggedOut runs`() = runTest {
        // GIVEN
        coEvery { mockPostsRepository.update() } returns Failure(Exception())

        // WHEN
        val result = login(mockApiToken)

        // THEN
        assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        coVerify { mockUserRepository.setAuthToken(mockApiToken) }
        coVerify { mockAppStateRepository.runAction(UserLoggedOut) }
    }

    @Test
    fun `GIVEN repository call is successful WHEN Login is called THEN UserLoggedIn runs`() = runTest {
        // GIVEN
        coEvery { mockPostsRepository.update() } returns Success(mockTime)
        coEvery { mockPostsRepository.clearCache() } returns Success(Unit)

        // WHEN
        val result = login(mockApiToken)

        // THEN
        assertThat(result.getOrNull()).isEqualTo(Unit)
        coVerify { mockUserRepository.setAuthToken(mockApiToken) }
        coVerify { mockPostsRepository.clearCache() }
        coVerify { mockAppStateRepository.runAction(UserLoggedIn) }
    }

    @Test
    fun `GIVEN app_review_mode is used WHEN Login is called THEN UserLoggedIn runs`() = runTest {
        // GIVEN
        coEvery { mockPostsRepository.clearCache() } returns Success(Unit)

        // WHEN
        val result = login(params = "app_review_mode")

        // THEN
        assertThat(result.getOrNull()).isEqualTo(Unit)
        verify { mockUserRepository.appReviewMode = true }
        coVerify { mockPostsRepository.clearCache() }
        coVerify { mockAppStateRepository.runAction(UserLoggedIn) }
        coVerify(exactly = 0) { mockPostsRepository.update() }
    }
}
