package com.fibelatti.pinboard.core

import app.cash.turbine.test
import com.fibelatti.pinboard.features.user.domain.UserCredentials
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class AppModeProviderTest {

    private val credentialsFlow = MutableStateFlow(
        UserCredentials(
            pinboardAuthToken = null,
            linkdingInstanceUrl = null,
            linkdingAuthToken = null,
        ),
    )
    private val mockUserRepository: UserRepository = mockk {
        every { userCredentials } returns credentialsFlow
    }

    private val dispatcher = UnconfinedTestDispatcher()
    private val appModeProvider by lazy {
        AppModeProvider(
            userRepository = mockUserRepository,
            dispatcher = dispatcher,
            scope = TestScope(dispatcher),
            sharingStarted = SharingStarted.Lazily,
        )
    }

    @Test
    fun `app mode emits the expected values`() = runTest {
        appModeProvider.appMode.test {
            assertThat(awaitItem()).isEqualTo(AppMode.UNSET)

            credentialsFlow.update { it.copy(appReviewMode = true) }
            assertThat(awaitItem()).isEqualTo(AppMode.NO_API)

            credentialsFlow.update {
                it.copy(
                    pinboardAuthToken = "token",
                    appReviewMode = false,
                )
            }
            assertThat(awaitItem()).isEqualTo(AppMode.PINBOARD)

            credentialsFlow.update {
                it.copy(
                    pinboardAuthToken = null,
                    linkdingAuthToken = "token",
                )
            }
            assertThat(awaitItem()).isEqualTo(AppMode.LINKDING)
        }
    }

    @Test
    fun `set selection updates the app mode - no api`() = runTest {
        credentialsFlow.update {
            UserCredentials(
                pinboardAuthToken = "pinboard-token",
                linkdingInstanceUrl = "linkding-url",
                linkdingAuthToken = "linkding-token",
                appReviewMode = true,
            )
        }

        appModeProvider.setSelection(appMode = AppMode.NO_API)

        appModeProvider.appMode.test {
            assertThat(awaitItem()).isEqualTo(AppMode.NO_API)
        }
    }

    @Test
    fun `set selection updates the app mode - pinboard`() = runTest {
        credentialsFlow.update {
            UserCredentials(
                pinboardAuthToken = "pinboard-token",
                linkdingInstanceUrl = "linkding-url",
                linkdingAuthToken = "linkding-token",
            )
        }

        appModeProvider.setSelection(appMode = AppMode.PINBOARD)

        appModeProvider.appMode.test {
            assertThat(awaitItem()).isEqualTo(AppMode.PINBOARD)
        }
    }

    @Test
    fun `set selection updates the app mode - linkding`() = runTest {
        credentialsFlow.update {
            UserCredentials(
                pinboardAuthToken = "pinboard-token",
                linkdingInstanceUrl = "linkding-url",
                linkdingAuthToken = "linkding-token",
            )
        }

        appModeProvider.setSelection(appMode = AppMode.LINKDING)

        appModeProvider.appMode.test {
            assertThat(awaitItem()).isEqualTo(AppMode.LINKDING)
        }
    }
}
