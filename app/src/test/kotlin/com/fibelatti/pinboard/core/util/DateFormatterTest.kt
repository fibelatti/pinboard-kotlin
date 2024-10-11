package com.fibelatti.pinboard.core.util

import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.datetime.TimeZone
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DateFormatterTest {

    private val testUserRepository = mockk<UserRepository>()
    private val dateFormatter = DateFormatter(testUserRepository)

    @BeforeEach
    fun setup() {
        mockkObject(TimeZone)
        every { TimeZone.currentSystemDefault() } returns TimeZone.UTC
    }

    @Test
    fun `WHEN tzFormatToDisplayFormat is called AND preferred date format is DayMonthYearWithTime THEN it is correctly returned`() {
        every { testUserRepository.preferredDateFormat } returns PreferredDateFormat.DayMonthYearWithTime

        assertThat(dateFormatter.tzFormatToDisplayFormat("1991-08-20T11:00:00Z"))
            .isEqualTo("20/08/91, 11:00")
    }

    @Test
    fun `WHEN tzFormatToDisplayFormat is called AND preferred date format is DayMonthYearWithTime THEN it is correctly returned - with millis`() {
        every { testUserRepository.preferredDateFormat } returns PreferredDateFormat.DayMonthYearWithTime

        assertThat(dateFormatter.tzFormatToDisplayFormat("1991-08-20T11:00:00.123456Z"))
            .isEqualTo("20/08/91, 11:00")
    }

    @Test
    fun `WHEN tzFormatToDisplayFormat is called AND preferred date format is MonthDayYearWithTime THEN it is correctly returned`() {
        every { testUserRepository.preferredDateFormat } returns PreferredDateFormat.MonthDayYearWithTime

        assertThat(dateFormatter.tzFormatToDisplayFormat("1991-08-20T11:00:00Z"))
            .isEqualTo("08/20/91, 11:00")
    }

    @Test
    fun `WHEN notesFormatToDisplayFormat is called AND preferred date format is DayMonthYearWithTime THEN it is correctly returned`() {
        every { testUserRepository.preferredDateFormat } returns PreferredDateFormat.DayMonthYearWithTime

        assertThat(dateFormatter.notesFormatToDisplayFormat("1991-08-20 11:00:00"))
            .isEqualTo("20/08/91, 11:00")
    }

    @Test
    fun `WHEN notesFormatToDisplayFormat is called AND preferred date format is MonthDayYearWithTime THEN it is correctly returned`() {
        every { testUserRepository.preferredDateFormat } returns PreferredDateFormat.MonthDayYearWithTime

        assertThat(dateFormatter.notesFormatToDisplayFormat("1991-08-20 11:00:00"))
            .isEqualTo("08/20/91, 11:00")
    }

    @Test
    fun `WHEN nowAsTzFormat is called THEN format should be correct`() {
        val result = dateFormatter.nowAsTzFormat().matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{6}Z$".toRegex())

        assertThat(result).isTrue()
    }

    @Test
    fun `WHEN displayFormatToMillis is called THEN millis is returned`() {
        every { testUserRepository.preferredDateFormat } returns PreferredDateFormat.DayMonthYearWithTime

        val result = dateFormatter.displayFormatToMillis("20/08/91, 11:00")

        assertThat(result).isEqualTo(682_686_000_000)
    }
}
