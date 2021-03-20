package com.fibelatti.pinboard.core.android

sealed class PreferredDateFormat(val value: String) {
    object DayMonthYearWithTime : PreferredDateFormat("dd/MM/yy, HH:mm")
    object MonthDayYearWithTime : PreferredDateFormat("MM/dd/yy, HH:mm")
}
