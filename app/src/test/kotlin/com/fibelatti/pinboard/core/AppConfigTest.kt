package com.fibelatti.pinboard.core

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class AppConfigTest {

    @Test
    fun `DEFAULT_PAGE_SIZE should be 1000`() {
        assertThat(AppConfig.DEFAULT_PAGE_SIZE).isEqualTo(1_000)
    }

    @Test
    fun `DEFAULT_PAGE_SIZE_RECENT should be 50`() {
        assertThat(AppConfig.DEFAULT_RECENT_QUANTITY).isEqualTo(50)
    }

    @Test
    fun `DEFAULT_FILTER_MAX_TAGS should be 3`() {
        assertThat(AppConfig.DEFAULT_FILTER_MAX_TAGS).isEqualTo(3)
    }

    @Test
    fun `PinboardApiLiterals YES should be yes`() {
        assertThat(AppConfig.PinboardApiLiterals.YES).isEqualTo("yes")
    }

    @Test
    fun `PinboardApiLiterals NO should be no`() {
        assertThat(AppConfig.PinboardApiLiterals.NO).isEqualTo("no")
    }

    @Test
    fun `PinboardApiLiterals TAG_SEPARATOR should be an empty space`() {
        assertThat(AppConfig.PinboardApiLiterals.TAG_SEPARATOR).isEqualTo(" ")
    }
}
