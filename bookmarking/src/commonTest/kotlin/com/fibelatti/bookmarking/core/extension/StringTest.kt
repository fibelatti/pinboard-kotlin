package com.fibelatti.bookmarking.core.extension

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class StringTest {

    @Test
    fun `containsHtmlChars should return true for known html characters`() {
        assertThat("&lt;".containsHtmlChars()).isTrue()
        assertThat("&gt;".containsHtmlChars()).isTrue()
        assertThat("&quot;".containsHtmlChars()).isTrue()
        assertThat("&amp;".containsHtmlChars()).isTrue()
        assertThat("no html chars".containsHtmlChars()).isFalse()
    }

    @Test
    fun `replaceHtmlChars should replace known html characters`() {
        val result = "&lt;&gt;&quot;&amp;".replaceHtmlChars()

        assertThat(result).isEqualTo("<>\"&")
    }
}
