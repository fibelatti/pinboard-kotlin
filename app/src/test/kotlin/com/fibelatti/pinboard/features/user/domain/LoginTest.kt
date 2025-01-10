package com.fibelatti.pinboard.features.user.domain

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_API_TOKEN
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_DATE_TIME
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_INSTANCE_URL
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.UserLoggedIn
import com.fibelatti.pinboard.features.appstate.UserLoggedOut
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
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
        val result = login(Login.Params(authToken = SAMPLE_API_TOKEN))

        // THEN
        assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        coVerify { mockUserRepository.setAuthToken(SAMPLE_API_TOKEN) }
        coVerify { mockAppStateRepository.runAction(UserLoggedOut) }
    }

    @Test
    fun `GIVEN repository call is successful WHEN Login is called THEN UserLoggedIn runs`() = runTest {
        // GIVEN
        coEvery { mockPostsRepository.update() } returns Success(SAMPLE_DATE_TIME)
        coEvery { mockPostsRepository.clearCache() } returns Success(Unit)

        // WHEN
        val result = login(Login.Params(authToken = SAMPLE_API_TOKEN, instanceUrl = SAMPLE_INSTANCE_URL))

        // THEN
        assertThat(result.getOrNull()).isEqualTo(Unit)
        coVerify { mockUserRepository.setAuthToken(SAMPLE_API_TOKEN) }
        coVerify { mockUserRepository.linkdingInstanceUrl = SAMPLE_INSTANCE_URL }
        coVerify { mockPostsRepository.clearCache() }
        coVerify { mockAppStateRepository.runAction(UserLoggedIn) }
    }
}
