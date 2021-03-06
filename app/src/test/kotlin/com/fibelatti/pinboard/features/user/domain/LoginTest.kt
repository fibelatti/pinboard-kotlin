package com.fibelatti.pinboard.features.user.domain

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.pinboard.MockDataProvider.mockApiToken
import com.fibelatti.pinboard.MockDataProvider.mockTime
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class LoginTest {

    private val mockUserRepository = mockk<UserRepository>(relaxed = true)
    private val mockPostsRepository = mockk<PostsRepository>()

    private val login = Login(
        mockUserRepository,
        mockPostsRepository
    )

    @Test
    fun `GIVEN repository call fails WHEN Login is called THEN loggedIn is never called`() {
        // GIVEN
        coEvery { mockPostsRepository.update() } returns Failure(Exception())

        // WHEN
        val result = runBlocking { login(mockApiToken) }

        // THEN
        assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        coVerify { mockUserRepository.loginAttempt(mockApiToken) }
        coVerify(exactly = 0) { mockPostsRepository.clearCache() }
        coVerify(exactly = 0) { mockUserRepository.loggedIn() }
    }

    @Test
    fun `GIVEN repository call is successful WHEN Login is called THEN Success is returned and user is logged in`() {
        // GIVEN
        coEvery { mockPostsRepository.update() } returns Success(mockTime)
        coEvery { mockPostsRepository.clearCache() } returns Success(Unit)

        // WHEN
        val result = runBlocking { login(mockApiToken) }

        // THEN
        assertThat(result.getOrNull()).isEqualTo(Unit)
        coVerify { mockUserRepository.loginAttempt(mockApiToken) }
        coVerify { mockPostsRepository.clearCache() }
        coVerify { mockUserRepository.loggedIn() }
    }
}
