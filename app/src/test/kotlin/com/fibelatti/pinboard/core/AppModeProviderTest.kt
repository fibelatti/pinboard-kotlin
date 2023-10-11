package com.fibelatti.pinboard.core

import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.collectIn
import com.fibelatti.pinboard.runUnconfinedTest
import com.google.common.truth.Truth.assertThat
import io.mockk.mockkStatic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AppModeProviderTest {

    private val appModeProvider = AppModeProvider()

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
    fun `appMode is PINBOARD when review mode is set to false`() = runUnconfinedTest {
        appModeProvider.setReviewMode(false)

        val values = appModeProvider.appMode.collectIn(this)

        assertThat(values).containsExactly(AppMode.PINBOARD)
    }
}
