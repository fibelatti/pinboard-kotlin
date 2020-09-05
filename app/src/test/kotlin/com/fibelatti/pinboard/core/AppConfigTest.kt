package com.fibelatti.pinboard.core

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class AppConfigTest {

    @Test
    fun `API_BASE_URL should be correct `() {
        assertThat(AppConfig.API_BASE_URL).isEqualTo("https://api.pinboard.in/v1/")
    }

    @Test
    fun `API_ENCODING should be UTF-8`() {
        assertThat(AppConfig.API_ENCODING).isEqualTo("UTF-8")
    }

    @Test
    fun `API_MAX_LENGTH should be 255`() {
        assertThat(AppConfig.API_MAX_LENGTH).isEqualTo(255)
    }

    @Test
    fun `DEFAULT_PAGE_SIZE should be 100`() {
        assertThat(AppConfig.DEFAULT_PAGE_SIZE).isEqualTo(100)
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
    fun `PinboardApiLiterals TAG_SEPARATOR_REQUEST should be +`() {
        assertThat(AppConfig.PinboardApiLiterals.TAG_SEPARATOR_REQUEST).isEqualTo("+")
    }

    @Test
    fun `PinboardApiLiterals TAG_SEPARATOR_RESPONSE should be an empty space`() {
        assertThat(AppConfig.PinboardApiLiterals.TAG_SEPARATOR_RESPONSE).isEqualTo(" ")
    }
}
