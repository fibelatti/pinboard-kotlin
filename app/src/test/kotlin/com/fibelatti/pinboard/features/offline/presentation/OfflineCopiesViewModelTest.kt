@file:Suppress("UnusedFlow")

package com.fibelatti.pinboard.features.offline.presentation

import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.createAppState
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.features.appstate.AppState
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.OfflineCopyListContent
import com.fibelatti.pinboard.features.appstate.SetOfflineCopies
import com.fibelatti.pinboard.features.offline.domain.OfflineCopyRepository
import com.fibelatti.pinboard.features.offline.domain.model.OfflineCopy
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class OfflineCopiesViewModelTest : BaseViewModelTest() {

    private val appStateFlow: MutableStateFlow<AppState> = MutableStateFlow(createAppState())
    private val mockAppStateRepository = mockk<AppStateRepository>(relaxed = true) {
        every { appState } returns appStateFlow
    }

    private val offlineCopies: List<OfflineCopy> = listOf(mockk(), mockk())
    private val mockOfflineCopyRepository = mockk<OfflineCopyRepository>(relaxed = true) {
        every { getOfflineCopies(any()) } returns flowOf(offlineCopies)
    }

    private val offlineCopiesViewModel = OfflineCopiesViewModel(
        scope = TestScope(dispatcher),
        appStateRepository = mockAppStateRepository,
        offlineCopyRepository = mockOfflineCopyRepository,
    )

    @Test
    fun `WHEN the content is not OfflineCopiesContent THEN the copies are not queried`() = runTest {
        // THEN the query must not run for the lifetime of the app, only while the screen asks for it
        verifyNoCopiesQueried()
    }

    @Test
    fun `WHEN the content does not need loading THEN the copies are not queried`() = runTest {
        // WHEN returning to a list that has already been populated
        appStateFlow.value = createAppState(content = createContent(shouldLoad = false))

        // THEN
        verifyNoCopiesQueried()
    }

    @Test
    fun `WHEN the content needs loading THEN the copies are set`() = runTest {
        // WHEN
        appStateFlow.value = createAppState(content = createContent(shouldLoad = true))

        // THEN
        coVerify {
            mockOfflineCopyRepository.getOfflineCopies(appMode = AppMode.PINBOARD)
            mockAppStateRepository.runAction(SetOfflineCopies(offlineCopies))
        }
    }

    @Test
    fun `WHEN the copies change while the screen is open THEN the list keeps up`() = runTest {
        // GIVEN the query stays subscribed, so a copy saved or deleted elsewhere still arrives
        val laterCopies: List<OfflineCopy> = listOf(mockk())
        val copiesFlow = MutableStateFlow(offlineCopies)
        every { mockOfflineCopyRepository.getOfflineCopies(any()) } returns copiesFlow

        appStateFlow.value = createAppState(content = createContent(shouldLoad = true))

        // WHEN the content stops asking to load, as SetOfflineCopies does
        appStateFlow.value = createAppState(content = createContent(shouldLoad = false))
        copiesFlow.value = laterCopies

        // THEN
        coVerify { mockAppStateRepository.runAction(SetOfflineCopies(laterCopies)) }
    }

    @Test
    fun `WHEN delete is called THEN the copy is removed using its own app mode`() = runTest {
        // GIVEN a copy belonging to an account that is not necessarily the current one
        val offlineCopy = mockk<OfflineCopy> {
            every { appMode } returns AppMode.LINKDING
            every { bookmarkId } returns "some-id"
        }

        // WHEN
        offlineCopiesViewModel.delete(offlineCopy)

        // THEN
        coVerify {
            mockOfflineCopyRepository.delete(appMode = AppMode.LINKDING, bookmarkId = "some-id")
        }
        assertThat(offlineCopiesViewModel.screenState.first().deletedCount).isEqualTo(1)
    }

    @Test
    fun `WHEN delete is called with a list THEN every copy is removed and the screen is notified once`() = runTest {
        // GIVEN a selection spanning both backends
        val offlineCopies = listOf(
            mockk<OfflineCopy> {
                every { appMode } returns AppMode.LINKDING
                every { bookmarkId } returns "some-id"
            },
            mockk<OfflineCopy> {
                every { appMode } returns AppMode.PINBOARD
                every { bookmarkId } returns "another-id"
            },
        )

        // WHEN
        offlineCopiesViewModel.delete(offlineCopies)

        // THEN
        coVerify {
            mockOfflineCopyRepository.delete(appMode = AppMode.LINKDING, bookmarkId = "some-id")
            mockOfflineCopyRepository.delete(appMode = AppMode.PINBOARD, bookmarkId = "another-id")
        }
        assertThat(offlineCopiesViewModel.screenState.first().deletedCount).isEqualTo(2)
    }

    @Test
    fun `WHEN userNotified is called THEN the deleted count is cleared`() = runTest {
        // GIVEN
        offlineCopiesViewModel.delete(
            mockk<OfflineCopy> {
                every { appMode } returns AppMode.PINBOARD
                every { bookmarkId } returns "some-id"
            },
        )

        // WHEN
        offlineCopiesViewModel.userNotified()

        // THEN the banner must not fire again on the next state change
        assertThat(offlineCopiesViewModel.screenState.first().deletedCount).isEqualTo(0)
    }

    private fun createContent(shouldLoad: Boolean): OfflineCopyListContent = OfflineCopyListContent(
        offlineCopies = emptyList(),
        totalSize = 0,
        shouldLoad = shouldLoad,
        previousContent = mockk(),
    )

    private fun verifyNoCopiesQueried() {
        coVerify(exactly = 0) {
            mockOfflineCopyRepository.getOfflineCopies(any())
            mockAppStateRepository.runAction(any<SetOfflineCopies>())
        }
    }
}
