package com.fibelatti.pinboard.core

import com.fibelatti.core.test.extension.shouldBe
import org.junit.jupiter.api.Test

internal class AppConfigTest {

    @Test
    fun `API_BASE_URL should be correct `() {
        AppConfig.API_BASE_URL shouldBe "https://api.pinboard.in/v1/"
    }

    @Test
    fun `API_ENCODING should be UTF-8`() {
        AppConfig.API_ENCODING shouldBe "UTF-8"
    }

    @Test
    fun `API_FILTER_MAX_TAGS should be 3`() {
        AppConfig.API_FILTER_MAX_TAGS shouldBe 3
    }

    @Test
    fun `PinboardApiLiterals YES should be yes`() {
        AppConfig.PinboardApiLiterals.YES shouldBe "yes"
    }

    @Test
    fun `PinboardApiLiterals NO should be no`() {
        AppConfig.PinboardApiLiterals.NO shouldBe "no"
    }

    @Test
    fun `PinboardApiLiterals TAG_SEPARATOR_REQUEST should be +`() {
        AppConfig.PinboardApiLiterals.TAG_SEPARATOR_REQUEST shouldBe "+"
    }

    @Test
    fun `PinboardApiLiterals TAG_SEPARATOR_RESPONSE should be an empty space`() {
        AppConfig.PinboardApiLiterals.TAG_SEPARATOR_RESPONSE shouldBe " "
    }
}
