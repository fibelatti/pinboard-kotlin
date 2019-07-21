package com.fibelatti.pinboard.core.util

import com.fibelatti.core.test.extension.safeAny
import com.fibelatti.core.test.extension.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.willReturn
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import java.text.SimpleDateFormat
import java.util.TimeZone

internal class DateFormatterTest {

    private val dateFormatter = spy(DateFormatter())

    @Test
    fun `WHEN tzFormatToDisplayFormat is called THEN FORMAT_DISPLAY is returned`() {
        val displayFormatAsUtc = dateFormatter.getSimpleDateFormat(
            FORMAT_DISPLAY,
            timeZone = TimeZone.getTimeZone("UTC")
        )

        willReturn(displayFormatAsUtc)
            .given(dateFormatter).getSimpleDateFormat(FORMAT_DISPLAY, timeZone = null)

        dateFormatter.tzFormatToDisplayFormat("1991-08-20T11:00:00Z") shouldBe "20/08/91, 11:00"
    }

    @Test
    fun `WHEN notesFormatToDisplayFormat is called THEN FORMAT_DISPLAY is returned`() {
        val displayFormatAsUtc = dateFormatter.getSimpleDateFormat(
            FORMAT_DISPLAY,
            timeZone = TimeZone.getTimeZone("UTC")
        )

        willReturn(displayFormatAsUtc)
            .given(dateFormatter).getSimpleDateFormat(FORMAT_DISPLAY, timeZone = null)

        dateFormatter.notesFormatToDisplayFormat("1991-08-20 11:00:00") shouldBe "20/08/91, 11:00"
    }

    @Test
    fun `WHEN nowAsTzFormat is called THEN format should be correct`() {
        val result = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$".toRegex()
            .matches(dateFormatter.nowAsTzFormat())

        result shouldBe true
    }

    @Test
    fun `getUtcFormat should call getSimpleDateFormat and the timezone should be UTC`() {
        willReturn(SimpleDateFormat())
            .given(dateFormatter).getSimpleDateFormat(anyString(), safeAny())

        dateFormatter.getUtcFormat("any")

        verify(dateFormatter).getSimpleDateFormat("any", TimeZone.getTimeZone("UTC"))
    }
}
