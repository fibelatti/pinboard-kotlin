package com.fibelatti.pinboard.core.util

import com.fibelatti.core.test.extension.shouldBe
import org.junit.jupiter.api.Test

internal class DateFormatterTest {

    private val dateFormatter = DateFormatter()

    @Test
    fun `WHEN tzFormatToDisplayFormat is called THEN FORMAT_DISPLAY is returned`() {
        dateFormatter.tzFormatToDisplayFormat("1991-08-20T11:00:00Z") shouldBe "20/08/91, 11:00"
    }
}
