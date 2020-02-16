package com.fibelatti.pinboard.core.extension

import com.fibelatti.core.test.extension.shouldBe
import org.junit.jupiter.api.Test

internal class StringTest {

    @Test
    fun `containsHtmlChars should return true for known html characters`() {
        "&lt;".containsHtmlChars() shouldBe true
        "&gt;".containsHtmlChars() shouldBe true
        "&quot;".containsHtmlChars() shouldBe true
        "&amp;".containsHtmlChars() shouldBe true
        "no html chars".containsHtmlChars() shouldBe false
    }

    @Test
    fun `replaceHtmlChars should replace known html characters`() {
        val result = "&lt;&gt;&quot;&amp;".replaceHtmlChars()

        result shouldBe "<>\"&"
    }
}
