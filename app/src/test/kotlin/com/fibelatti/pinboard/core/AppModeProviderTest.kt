package com.fibelatti.pinboard.core

import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.collectIn
import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import com.fibelatti.pinboard.runUnconfinedTest
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AppModeProviderTest {

    private val mockUserSharedPreferences: UserSharedPreferences = mockk {
        every { useLinkding } returns false
    }

    private val appModeProvider by lazy {
        AppModeProvider(
            userSharedPreferences = mockUserSharedPreferences,
        )
    }

    @BeforeEach
    fun setup() {
        mockkStatic(BuildConfig::class)
    }

    @Test
    fun `appMode is NO_API when review mode is set to true`() = runUnconfinedTest {
        appModeProvider.setReviewMode(true)

        val values = appModeProvider.appMode.collectIn(this)

        assertThat(values).containsExactly(AppMode.NO_API)
    }

    @Test
    fun `appMode is LINKDING when useLinkding is true`() = runUnconfinedTest {
        every { mockUserSharedPreferences.useLinkding } returns true
        every { mockUserSharedPreferences.linkdingInstanceUrl } returns "https://test.com"

        val values = appModeProvider.appMode.collectIn(this)

        assertThat(values).containsExactly(AppMode.LINKDING)
    }

    @Test
    fun `appMode is PINBOARD when review mode is set to false`() = runUnconfinedTest {
        appModeProvider.setReviewMode(false)

        val values = appModeProvider.appMode.collectIn(this)

        assertThat(values).containsExactly(AppMode.PINBOARD)
    }
}
