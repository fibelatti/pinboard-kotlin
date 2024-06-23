package com.fibelatti.pinboard.features.user.domain

import com.fibelatti.bookmarking.features.posts.domain.PostsRepository
import com.fibelatti.bookmarking.features.user.domain.UserRepository
import com.fibelatti.bookmarking.test.MockDataProvider.MOCK_API_TOKEN
import com.fibelatti.bookmarking.test.MockDataProvider.MOCK_INSTANCE_URL
import com.fibelatti.bookmarking.test.MockDataProvider.MOCK_TIME
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.UserLoggedIn
import com.fibelatti.pinboard.features.appstate.UserLoggedOut
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class LoginTest {

    private val mockUserRepository = mockk<UserRepository>(relaxed = true)
    private val mockAppStateRepository = mockk<AppStateRepository>(relaxUnitFun = true)
    private val mockPostsRepository = mockk<PostsRepository>()

    private val login = Login(
        userRepository = mockUserRepository,
        appStateRepository = mockAppStateRepository,
        postsRepository = mockPostsRepository,
    )

    @Test
    fun `GIVEN repository call fails WHEN Login is called THEN UserLoggedOut runs`() = runTest {
        // GIVEN
        coEvery { mockPostsRepository.update() } returns Failure(Exception())

        // WHEN
        val result = login(Login.Params(authToken = MOCK_API_TOKEN))

        // THEN
        assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        coVerify { mockUserRepository.setAuthToken(MOCK_API_TOKEN) }
        coVerify { mockAppStateRepository.runAction(UserLoggedOut) }
    }

    @Test
    fun `GIVEN repository call is successful WHEN Login is called THEN UserLoggedIn runs`() = runTest {
        // GIVEN
        coEvery { mockPostsRepository.update() } returns Success(MOCK_TIME)
        coEvery { mockPostsRepository.clearCache() } returns Success(Unit)

        // WHEN
        val result = login(Login.Params(authToken = MOCK_API_TOKEN, instanceUrl = MOCK_INSTANCE_URL))

        // THEN
        assertThat(result.getOrNull()).isEqualTo(Unit)
        coVerify { mockUserRepository.setAuthToken(MOCK_API_TOKEN) }
        coVerify { mockUserRepository.linkdingInstanceUrl = MOCK_INSTANCE_URL }
        coVerify { mockPostsRepository.clearCache() }
        coVerify { mockAppStateRepository.runAction(UserLoggedIn) }
    }
}
