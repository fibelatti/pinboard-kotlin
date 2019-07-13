package com.fibelatti.pinboard.core.util

import androidx.annotation.VisibleForTesting
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

private const val FORMAT_TZ = "yyyy-MM-dd'T'HH:mm:ss'Z'"

@VisibleForTesting
const val FORMAT_DISPLAY = "dd/MM/yy, HH:mm"

class DateFormatter @Inject constructor() {

    @VisibleForTesting
    val utcFormat = getSimpleDateFormat(FORMAT_TZ, timeZone = TimeZone.getTimeZone("UTC"))

    fun tzFormatToDisplayFormat(input: String): String =
        getSimpleDateFormat(FORMAT_DISPLAY).format(utcFormat.parse(input))

    fun nowAsTzFormat(): String = utcFormat.format(Date())

    @VisibleForTesting
    fun getSimpleDateFormat(format: String, timeZone: TimeZone? = null): SimpleDateFormat =
        SimpleDateFormat(format, Locale.US).apply { timeZone?.let(::setTimeZone) }
}
