package com.fibelatti.pinboard.core.util

import androidx.annotation.VisibleForTesting
import com.fibelatti.pinboard.features.user.domain.UserRepository
import org.koin.core.annotation.Factory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val FORMAT_TZ = "yyyy-MM-dd'T'HH:mm:ss'Z'"
private const val FORMAT_NOTES = "yyyy-MM-dd HH:mm:ss"

@Factory
class DateFormatter(
    private val userRepository: UserRepository,
) {

    fun tzFormatToDisplayFormat(input: String): String? = getUtcFormat(FORMAT_TZ)
        .parse(input)
        ?.let(getSimpleDateFormat(userRepository.preferredDateFormat.value)::format)

    fun notesFormatToDisplayFormat(input: String): String? = getUtcFormat(FORMAT_NOTES)
        .parse(input)
        ?.let(getSimpleDateFormat(userRepository.preferredDateFormat.value)::format)

    fun nowAsTzFormat(): String = getUtcFormat(FORMAT_TZ).format(Date())

    fun displayFormatToMillis(input: String): Long? = getSimpleDateFormat(userRepository.preferredDateFormat.value)
        .parse(input)
        ?.time

    @VisibleForTesting
    fun getUtcFormat(format: String) = getSimpleDateFormat(format, timeZone = TimeZone.getTimeZone("UTC"))

    @VisibleForTesting
    fun getSimpleDateFormat(format: String, timeZone: TimeZone? = null): SimpleDateFormat =
        SimpleDateFormat(format, Locale.US).apply { timeZone?.let(::setTimeZone) }
}
