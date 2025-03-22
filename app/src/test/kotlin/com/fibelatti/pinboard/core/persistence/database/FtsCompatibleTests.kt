package com.fibelatti.pinboard.core.persistence.database

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class FtsCompatibleTests {

    @Test
    fun `isFtsCompatible returns true for english queries`() {
        assertThat(isFtsCompatible(value = "App theme")).isTrue()
    }

    @Test
    fun `isFtsCompatible returns true for cyrillic queries`() {
        assertThat(isFtsCompatible(value = "Тема приложения")).isFalse()
    }

    @Test
    fun `isFtsCompatible returns true for chinese queries`() {
        assertThat(isFtsCompatible(value = "主题")).isFalse()
    }
}
