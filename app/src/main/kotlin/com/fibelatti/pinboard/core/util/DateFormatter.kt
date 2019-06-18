package com.fibelatti.pinboard.core.util

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import javax.inject.Inject

private const val FORMAT_TZ = "yyyy-MM-dd'T'HH:mm:ss'Z'"
private const val FORMAT_DISPLAY = "dd/MM/yy, HH:mm"

class DateFormatter @Inject constructor() {

    private val dtfTZ: DateTimeFormatter = DateTimeFormat.forPattern(FORMAT_TZ)
    private val dtfDisplay: DateTimeFormatter = DateTimeFormat.forPattern(FORMAT_DISPLAY)

    fun tzFormatToDisplayFormat(input: String): String =
        dtfDisplay.print(dtfTZ.parseDateTime(input).toDateTime())

    fun nowAsTzFormat(): String = dtfTZ.print(DateTime())
}
