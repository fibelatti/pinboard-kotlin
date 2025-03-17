package com.fibelatti.pinboard.features.user.domain

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_API_TOKEN
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_DATE_TIME
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_INSTANCE_URL
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.AppModeProvider
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.UserLoggedIn
import com.fibelatti.pinboard.features.appstate.UserLoginFailed
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerifySequence
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class LoginTest {

    private val mockUserRepository = mockk<UserRepository> {
        coJustRun { linkdingInstanceUrl = any() }
        coJustRun { setAuthToken(appMode = any(), authToken = any()) }
    }
    private val mockAppStateRepository = mockk<AppStateRepository> {
        coJustRun { runAction(any()) }
    }
    private val mockPostsRepository = mockk<PostsRepository>()
    private val mockAppModeProvider = mockk<AppModeProvider> {
        coJustRun { setSelection(any()) }
    }

    private val login = Login(
        userRepository = mockUserRepository,
        appStateRepository = mockAppStateRepository,
        postsRepository = mockPostsRepository,
        appModeProvider = mockAppModeProvider,
    )

    @Test
    fun `GIVEN repository call fails WHEN Login is called THEN UserLoginFailed runs`() = runTest {
        // GIVEN
        coEvery { mockPostsRepository.update() } returns Failure(Exception())

        // WHEN
        val result = login(Login.PinboardParams(authToken = SAMPLE_API_TOKEN))

        // THEN
        assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        coVerifySequence {
            mockUserRepository.setAuthToken(appMode = AppMode.PINBOARD, authToken = SAMPLE_API_TOKEN)
            mockAppModeProvider.setSelection(appMode = AppMode.PINBOARD)
            mockPostsRepository.update()
            mockAppStateRepository.runAction(UserLoginFailed(appMode = AppMode.PINBOARD))
        }
    }

    @Test
    fun `GIVEN repository call is successful WHEN Login is called THEN UserLoggedIn runs`() = runTest {
        // GIVEN
        coEvery { mockPostsRepository.update() } returns Success(SAMPLE_DATE_TIME)
        coEvery { mockPostsRepository.clearCache() } returns Success(Unit)

        // WHEN
        val result = login(Login.LinkdingParams(authToken = SAMPLE_API_TOKEN, instanceUrl = SAMPLE_INSTANCE_URL))

        // THEN
        assertThat(result.getOrNull()).isEqualTo(Unit)
        coVerifySequence {
            mockUserRepository.linkdingInstanceUrl = SAMPLE_INSTANCE_URL
            mockUserRepository.setAuthToken(appMode = AppMode.LINKDING, authToken = SAMPLE_API_TOKEN)
            mockAppModeProvider.setSelection(appMode = AppMode.LINKDING)
            mockPostsRepository.update()
            mockPostsRepository.clearCache()
            mockAppStateRepository.runAction(UserLoggedIn(appMode = AppMode.LINKDING))
        }
    }
}
