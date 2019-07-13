package com.fibelatti.pinboard.core.util

import com.fibelatti.core.test.extension.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.willReturn
import org.mockito.Mockito.spy
import java.util.TimeZone

internal class DateFormatterTest {

    private val dateFormatter = spy(DateFormatter())

    @BeforeEach
    fun setup() {
        val displayFormatAsUtc = dateFormatter.getSimpleDateFormat(
            FORMAT_DISPLAY,
            timeZone = TimeZone.getTimeZone("UTC")
        )

        willReturn(displayFormatAsUtc)
            .given(dateFormatter).getSimpleDateFormat(FORMAT_DISPLAY, timeZone = null)
    }

    @Test
    fun `sdfTZ timezone should be UTC`() {
        dateFormatter.utcFormat.timeZone.id shouldBe "UTC"
    }

    @Test
    fun `WHEN tzFormatToDisplayFormat is called THEN FORMAT_DISPLAY is returned`() {
        dateFormatter.tzFormatToDisplayFormat("1991-08-20T11:00:00Z") shouldBe "20/08/91, 11:00"
    }

    @Test
    fun `WHEN nowAsTzFormat is called THEN format should be correct`() {
        val result = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$".toRegex()
            .matches(dateFormatter.nowAsTzFormat())

        result shouldBe true
    }
}
