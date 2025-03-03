package com.fibelatti.pinboard.core

import app.cash.turbine.test
import com.fibelatti.pinboard.features.user.domain.UserPreferences
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class AppModeProviderTest {

    private val mockUserRepository: UserRepository = mockk()

    private val dispatcher = UnconfinedTestDispatcher()
    private val appModeProvider by lazy {
        AppModeProvider(
            userRepository = mockUserRepository,
            scope = TestScope(dispatcher),
            sharingStarted = SharingStarted.Lazily,
        )
    }

    @Test
    fun `app mode emits the expected values`() = runTest {
        val preferencesFlow = MutableStateFlow(mockk<UserPreferences> { every { useLinkding } returns false })
        val authTokenFlow = MutableStateFlow("")

        every { mockUserRepository.currentPreferences } returns preferencesFlow
        every { mockUserRepository.authToken } returns authTokenFlow
        every { mockUserRepository.useLinkding } returns false

        appModeProvider.appMode.test {
            assertThat(awaitItem()).isEqualTo(AppMode.UNSET)

            authTokenFlow.value = "app_review_mode"
            assertThat(awaitItem()).isEqualTo(AppMode.NO_API)

            preferencesFlow.value = mockk<UserPreferences> { every { useLinkding } returns true }
            authTokenFlow.value = "token"
            assertThat(awaitItem()).isEqualTo(AppMode.LINKDING)

            preferencesFlow.value = mockk<UserPreferences> { every { useLinkding } returns false }
            assertThat(awaitItem()).isEqualTo(AppMode.PINBOARD)
        }

        verify(exactly = 2) { mockUserRepository.authToken }
        verify(exactly = 1) { mockUserRepository.useLinkding }
    }
}
