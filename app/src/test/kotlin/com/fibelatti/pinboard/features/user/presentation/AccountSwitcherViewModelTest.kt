package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.features.appstate.AddAccount
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.UserLoggedIn
import com.fibelatti.pinboard.features.appstate.UserLoggedOut
import com.fibelatti.pinboard.features.user.domain.UserRepository
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class AccountSwitcherViewModelTest : BaseViewModelTest() {

    private val mockAppStateRepository = mockk<AppStateRepository> {
        coJustRun { runAction(any()) }
    }
    private val mockUserRepository = mockk<UserRepository> {
        every { userCredentials } returns MutableStateFlow(mockk())
    }

    private val accountSwitcherViewModel = AccountSwitcherViewModel(
        scope = TestScope(dispatcher),
        appStateRepository = mockAppStateRepository,
        userRepository = mockUserRepository,
    )

    @Test
    fun `select runs the login action`() = runTest {
        val appMode = mockk<AppMode>()

        accountSwitcherViewModel.select(appMode = appMode)

        coVerify { mockAppStateRepository.runAction(UserLoggedIn(appMode)) }
    }

    @Test
    fun `add account runs the add account action`() = runTest {
        val appMode = mockk<AppMode>()

        accountSwitcherViewModel.addAccount(appMode = appMode)

        coVerify { mockAppStateRepository.runAction(AddAccount(appMode)) }
    }

    @Test
    fun `logout runs the logout action`() = runTest {
        val appMode = mockk<AppMode>()

        accountSwitcherViewModel.logout(appMode = appMode)

        coVerify { mockAppStateRepository.runAction(UserLoggedOut(appMode)) }
    }
}
