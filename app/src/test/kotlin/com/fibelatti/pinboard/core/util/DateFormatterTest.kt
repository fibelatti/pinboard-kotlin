package com.fibelatti.pinboard.core.util

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.util.TimeZone

internal class DateFormatterTest {

    private val dateFormatter = spyk(DateFormatter())

    @Test
    fun `WHEN tzFormatToDisplayFormat is called THEN FORMAT_DISPLAY is returned`() {
        val displayFormatAsUtc = dateFormatter.getSimpleDateFormat(
            FORMAT_DISPLAY,
            timeZone = TimeZone.getTimeZone("UTC")
        )


        every {
            dateFormatter.getSimpleDateFormat(FORMAT_DISPLAY, timeZone = null)
        } returns displayFormatAsUtc

        assertThat(dateFormatter.tzFormatToDisplayFormat("1991-08-20T11:00:00Z"))
            .isEqualTo("20/08/91, 11:00")
    }

    @Test
    fun `WHEN notesFormatToDisplayFormat is called THEN FORMAT_DISPLAY is returned`() {
        val displayFormatAsUtc = dateFormatter.getSimpleDateFormat(
            FORMAT_DISPLAY,
            timeZone = TimeZone.getTimeZone("UTC")
        )

        every {
            dateFormatter.getSimpleDateFormat(FORMAT_DISPLAY, timeZone = null)
        } returns displayFormatAsUtc

        assertThat(dateFormatter.notesFormatToDisplayFormat("1991-08-20 11:00:00"))
            .isEqualTo("20/08/91, 11:00")
    }

    @Test
    fun `WHEN nowAsTzFormat is called THEN format should be correct`() {
        val result = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$".toRegex()
            .matches(dateFormatter.nowAsTzFormat())

        assertThat(result).isTrue()
    }

    @Test
    fun `getUtcFormat should call getSimpleDateFormat and the timezone should be UTC`() {
        every { dateFormatter.getSimpleDateFormat(any(), any()) } returns SimpleDateFormat()

        dateFormatter.getUtcFormat("any")

        verify { dateFormatter.getSimpleDateFormat("any", TimeZone.getTimeZone("UTC")) }
    }
}
