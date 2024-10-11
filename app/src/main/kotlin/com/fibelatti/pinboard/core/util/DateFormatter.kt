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

    fun tzFormatToDisplayFormat(input: String): String = try {
        val parsed: LocalDateTime = LocalDateTime.Format { byUnicodePattern(FORMAT_TZ) }.parse(input)
            .toInstant(TimeZone.UTC)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        getLocalDateTimeFormat(userRepository.preferredDateFormat).format(parsed)
    } catch (_: Exception) {
        input
    }

    fun notesFormatToDisplayFormat(input: String): String = try {
        val parsed: LocalDateTime = LocalDateTime.Format { byUnicodePattern(FORMAT_NOTES) }.parse(input)
            .toInstant(TimeZone.UTC)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        getLocalDateTimeFormat(userRepository.preferredDateFormat).format(parsed)
    } catch (_: Exception) {
        input
    }

    fun nowAsTzFormat(): String {
        val nowInUtc = Clock.System.now().toLocalDateTime(TimeZone.UTC)

        return LocalDateTime.Format { byUnicodePattern(FORMAT_TZ) }.format(nowInUtc)
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

            char(value = ',')
            char(value = ' ')
            hour(padding = Padding.ZERO)
            char(value = ':')
            minute(padding = Padding.ZERO)
        }
    }

    private companion object {

        const val FORMAT_TZ = "yyyy-MM-dd'T'HH:mm:ss[.SSSSSS]'Z'"
        const val FORMAT_NOTES = "yyyy-MM-dd HH:mm:ss"
    }
}
