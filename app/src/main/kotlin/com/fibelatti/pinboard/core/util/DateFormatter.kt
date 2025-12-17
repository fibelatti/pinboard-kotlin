package com.fibelatti.pinboard.core.util

import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.features.user.domain.UserRepository
import javax.inject.Inject
import kotlin.time.Clock
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
    private val dateTimeFormat: DateTimeFormat<LocalDateTime> = LocalDateTime.Format {
        byUnicodePattern(pattern = "yyyy-MM-dd['T'][' ']HH:mm:ss[.SSSSSS]['Z']")
    }

    fun dataFormatToDisplayFormat(input: String): String {
        try {
            val preferredDateFormat: PreferredDateFormat = userRepository.preferredDateFormat

            if (preferredDateFormat == PreferredDateFormat.NoDate) {
                return ""
            }

            val parsed: LocalDateTime = dateTimeFormat.parse(input)
                .toInstant(TimeZone.UTC)
                .toLocalDateTime(TimeZone.currentSystemDefault())

            return getLocalDateTimeFormat(preferredDateFormat).format(parsed)
        } catch (_: Exception) {
            return input
        }
    }

    fun dataFormatToEpoch(input: String): Long {
        return try {
            dateTimeFormat.parse(input)
                .toInstant(TimeZone.UTC)
                .epochSeconds
        } catch (_: Exception) {
            -1
        }
    }

    fun nowAsDataFormat(): String {
        val nowInUtc: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)

        return dateTimeFormat.format(nowInUtc).replace(" ", "")
    }

    @Throws(IllegalStateException::class)
    private fun getLocalDateTimeFormat(preferredDateFormat: PreferredDateFormat): DateTimeFormat<LocalDateTime> {
        return LocalDateTime.Format {
            when (preferredDateFormat) {
                is PreferredDateFormat.DayMonthYearWithTime -> {
                    day(padding = Padding.ZERO)
                    char(value = '/')
                    monthNumber(padding = Padding.ZERO)
                    char(value = '/')
                    yearTwoDigits(baseYear = 1970)
                }

                is PreferredDateFormat.MonthDayYearWithTime -> {
                    monthNumber(padding = Padding.ZERO)
                    char(value = '/')
                    day(padding = Padding.ZERO)
                    char(value = '/')
                    yearTwoDigits(baseYear = 1970)
                }

                is PreferredDateFormat.ShortYearMonthDayWithTime -> {
                    yearTwoDigits(baseYear = 1970)
                    char(value = '/')
                    monthNumber(padding = Padding.ZERO)
                    char(value = '/')
                    day(padding = Padding.ZERO)
                }

                is PreferredDateFormat.YearMonthDayWithTime -> {
                    year()
                    char(value = '-')
                    monthNumber(padding = Padding.ZERO)
                    char(value = '-')
                    day(padding = Padding.ZERO)
                }

                is PreferredDateFormat.NoDate -> {
                    error("`NoDate` cannot be formatted.")
                }
            }

            if (preferredDateFormat.includeTime) {
                chars(", ")
                hour(padding = Padding.ZERO)
                char(value = ':')
                minute(padding = Padding.ZERO)
            }
        }
    }
}
