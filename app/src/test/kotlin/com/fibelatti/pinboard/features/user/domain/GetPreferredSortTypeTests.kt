package com.fibelatti.pinboard.features.user.domain

import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.AppModeProvider
import com.fibelatti.pinboard.features.appstate.ByDateAddedNewestFirst
import com.fibelatti.pinboard.features.appstate.ByDateAddedOldestFirst
import com.fibelatti.pinboard.features.appstate.ByDateModifiedNewestFirst
import com.fibelatti.pinboard.features.appstate.ByDateModifiedOldestFirst
import com.fibelatti.pinboard.features.appstate.SortType
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Test

class GetPreferredSortTypeTests {

    private val mockUserRepository = mockk<UserRepository>()
    private val mockAppModeProvider = mockk<AppModeProvider>()

    private val getPreferredSortType = GetPreferredSortType(
        userRepository = mockUserRepository,
        appModeProvider = mockAppModeProvider,
    )

    @Test
    fun `WHEN invoke is called AND app mode is Linkding THEN preferred sort type is returned`() {
        val expectedSortType = mockk<SortType>()

        every { mockUserRepository.preferredSortType } returns expectedSortType
        every { mockAppModeProvider.appMode } returns MutableStateFlow(AppMode.LINKDING)

        val result = getPreferredSortType()

        assertThat(result).isEqualTo(expectedSortType)
    }

    @Test
    fun `WHEN invoke is called AND app mode is not Linkding THEN preferred sort type is converted - ByDateModifiedNewestFirst`() {
        every { mockUserRepository.preferredSortType } returns ByDateModifiedNewestFirst
        every { mockAppModeProvider.appMode } returns MutableStateFlow(AppMode.PINBOARD)

        val result = getPreferredSortType()

        assertThat(result).isEqualTo(ByDateAddedNewestFirst)
    }

    @Test
    fun `WHEN invoke is called AND app mode is not Linkding THEN preferred sort type is converted - ByDateModifiedOldestFirst`() {
        every { mockUserRepository.preferredSortType } returns ByDateModifiedOldestFirst
        every { mockAppModeProvider.appMode } returns MutableStateFlow(AppMode.PINBOARD)

        val result = getPreferredSortType()

        assertThat(result).isEqualTo(ByDateAddedOldestFirst)
    }

    @Test
    fun `WHEN invoke is called AND app mode is not Linkding THEN preferred sort type is not converted - All other types`() {
        val expectedSortType = mockk<SortType>()

        every { mockUserRepository.preferredSortType } returns expectedSortType
        every { mockAppModeProvider.appMode } returns MutableStateFlow(AppMode.PINBOARD)

        val result = getPreferredSortType()

        assertThat(result).isEqualTo(expectedSortType)
    }
}
