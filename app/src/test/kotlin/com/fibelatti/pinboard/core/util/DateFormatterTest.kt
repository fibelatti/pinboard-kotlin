package com.fibelatti.pinboard.core.util

import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.util.TimeZone

internal class DateFormatterTest {

    private val testUserRepository = mockk<UserRepository>()
    private val dateFormatter = spyk(DateFormatter(testUserRepository))

    @Test
    fun `WHEN tzFormatToDisplayFormat is called AND preferred date format is DayMonthYearWithTime THEN it is correctly returned`() {
        every { testUserRepository.preferredDateFormat } returns PreferredDateFormat.DayMonthYearWithTime
        every {
            dateFormatter.getSimpleDateFormat(PreferredDateFormat.DayMonthYearWithTime.value, timeZone = null)
        } returns dateFormatter.getSimpleDateFormat(
            PreferredDateFormat.DayMonthYearWithTime.value,
            timeZone = TimeZone.getTimeZone("UTC"),
        )

        assertThat(dateFormatter.tzFormatToDisplayFormat("1991-08-20T11:00:00Z"))
            .isEqualTo("20/08/91, 11:00")
    }

    @Test
    fun `WHEN tzFormatToDisplayFormat is called AND preferred date format is MonthDayYearWithTime THEN it is correctly returned`() {
        every { testUserRepository.preferredDateFormat } returns PreferredDateFormat.MonthDayYearWithTime
        every {
            dateFormatter.getSimpleDateFormat(PreferredDateFormat.MonthDayYearWithTime.value, timeZone = null)
        } returns dateFormatter.getSimpleDateFormat(
            PreferredDateFormat.MonthDayYearWithTime.value,
            timeZone = TimeZone.getTimeZone("UTC"),
        )

        assertThat(dateFormatter.tzFormatToDisplayFormat("1991-08-20T11:00:00Z"))
            .isEqualTo("08/20/91, 11:00")
    }

    @Test
    fun `WHEN notesFormatToDisplayFormat is called AND preferred date format is DayMonthYearWithTime THEN it is correctly returned`() {
        every { testUserRepository.preferredDateFormat } returns PreferredDateFormat.DayMonthYearWithTime
        every {
            dateFormatter.getSimpleDateFormat(PreferredDateFormat.DayMonthYearWithTime.value, timeZone = null)
        } returns dateFormatter.getSimpleDateFormat(
            PreferredDateFormat.DayMonthYearWithTime.value,
            timeZone = TimeZone.getTimeZone("UTC"),
        )

        assertThat(dateFormatter.notesFormatToDisplayFormat("1991-08-20 11:00:00"))
            .isEqualTo("20/08/91, 11:00")
    }

    @Test
    fun `WHEN notesFormatToDisplayFormat is called AND preferred date format is MonthDayYearWithTime THEN it is correctly returned`() {
        every { testUserRepository.preferredDateFormat } returns PreferredDateFormat.MonthDayYearWithTime
        every {
            dateFormatter.getSimpleDateFormat(PreferredDateFormat.MonthDayYearWithTime.value, timeZone = null)
        } returns dateFormatter.getSimpleDateFormat(
            PreferredDateFormat.MonthDayYearWithTime.value,
            timeZone = TimeZone.getTimeZone("UTC"),
        )

        assertThat(dateFormatter.notesFormatToDisplayFormat("1991-08-20 11:00:00"))
            .isEqualTo("08/20/91, 11:00")
    }

    @Test
    fun `WHEN nowAsTzFormat is called THEN format should be correct`() {
        val result = dateFormatter.nowAsTzFormat().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$".toRegex())

        assertThat(result).isTrue()
    }

    @Test
    fun `getUtcFormat should call getSimpleDateFormat and the timezone should be UTC`() {
        every { dateFormatter.getSimpleDateFormat(any(), any()) } returns SimpleDateFormat()

        dateFormatter.getUtcFormat("any")

        verify { dateFormatter.getSimpleDateFormat("any", TimeZone.getTimeZone("UTC")) }
    }
}
