package com.fibelatti.pinboard.core.util

import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.features.user.domain.UserRepository
import javax.inject.Inject
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

@OptIn(FormatStringsInDatetimeFormats::class)
class DateFormatter @Inject constructor(
    private val userRepository: UserRepository,
) {

    /**
     * Each server/resource uses a different format. Samples:
     *
     * - **Pinboard bookmark**: 2024-10-21T11:00:00Z
     * - **Pinboard note**: 2024-10-21 11:00:00
     * - **Linkding**: 2024-10-21T11:00:00.123456Z
     */
    private val dateTimeFormat = LocalDateTime.Format {
        byUnicodePattern(pattern = "yyyy-MM-dd['T'][' ']HH:mm:ss[.SSSSSS]['Z']")
    }

    fun dataFormatToDisplayFormat(input: String): String = try {
        val parsed: LocalDateTime = dateTimeFormat.parse(input)
            .toInstant(TimeZone.UTC)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        getLocalDateTimeFormat(userRepository.preferredDateFormat).format(parsed)
    } catch (_: Exception) {
        input
    }

    fun nowAsDataFormat(): String {
        val nowInUtc = Clock.System.now().toLocalDateTime(TimeZone.UTC)

        return dateTimeFormat.format(nowInUtc).replace(" ", "")
    }

    fun displayFormatToMillis(input: String): Long {
        return getLocalDateTimeFormat(userRepository.preferredDateFormat)
            .parse(input)
            .toInstant(TimeZone.UTC)
            .toEpochMilliseconds()
    }

    private fun getLocalDateTimeFormat(preferredDateFormat: PreferredDateFormat): DateTimeFormat<LocalDateTime> {
        return LocalDateTime.Format {
            when (preferredDateFormat) {
                PreferredDateFormat.DayMonthYearWithTime -> {
                    dayOfMonth(padding = Padding.ZERO)
                    char(value = '/')
                    monthNumber(padding = Padding.ZERO)
                    char(value = '/')
                    yearTwoDigits(baseYear = 1970)
                }

                PreferredDateFormat.MonthDayYearWithTime -> {
                    monthNumber(padding = Padding.ZERO)
                    char(value = '/')
                    dayOfMonth(padding = Padding.ZERO)
                    char(value = '/')
                    yearTwoDigits(baseYear = 1970)
                }

                PreferredDateFormat.ShortYearMonthDayWithTime -> {
                    yearTwoDigits(baseYear = 1970)
                    char(value = '/')
                    monthNumber(padding = Padding.ZERO)
                    char(value = '/')
                    dayOfMonth(padding = Padding.ZERO)
                }

                PreferredDateFormat.YearMonthDayWithTime -> {
                    year()
                    char(value = '-')
                    monthNumber(padding = Padding.ZERO)
                    char(value = '-')
                    dayOfMonth(padding = Padding.ZERO)
                }
            }

            chars(", ")
            hour(padding = Padding.ZERO)
            char(value = ':')
            minute(padding = Padding.ZERO)
        }
    }
}
