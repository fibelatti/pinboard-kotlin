package com.fibelatti.pinboard.core.util

import androidx.annotation.VisibleForTesting
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

private const val FORMAT_TZ = "yyyy-MM-dd'T'HH:mm:ss'Z'"
private const val FORMAT_NOTES = "yyyy-MM-dd HH:mm:ss"

@VisibleForTesting
const val FORMAT_DISPLAY = "dd/MM/yy, HH:mm"

class DateFormatter @Inject constructor() {

    fun tzFormatToDisplayFormat(input: String): String? =
        getUtcFormat(FORMAT_TZ).parse(input)?.let(getSimpleDateFormat(FORMAT_DISPLAY)::format)

    fun notesFormatToDisplayFormat(input: String): String? =
        getUtcFormat(FORMAT_NOTES).parse(input)?.let(getSimpleDateFormat(FORMAT_DISPLAY)::format)

    fun nowAsTzFormat(): String = getUtcFormat(FORMAT_TZ).format(Date())

    fun displayFormatToMillis(input: String): Long? = getSimpleDateFormat(FORMAT_DISPLAY).parse(input)?.time

    @VisibleForTesting
    fun getUtcFormat(format: String) = getSimpleDateFormat(format, timeZone = TimeZone.getTimeZone("UTC"))

    @VisibleForTesting
    fun getSimpleDateFormat(format: String, timeZone: TimeZone? = null): SimpleDateFormat =
        SimpleDateFormat(format, Locale.US).apply { timeZone?.let(::setTimeZone) }
}
