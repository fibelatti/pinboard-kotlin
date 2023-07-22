package com.fibelatti.pinboard.core.android

sealed class PreferredDateFormat(val value: String) {
    data object DayMonthYearWithTime : PreferredDateFormat("dd/MM/yy, HH:mm")
    data object MonthDayYearWithTime : PreferredDateFormat("MM/dd/yy, HH:mm")
    data object ShortYearMonthDayWithTime : PreferredDateFormat("yy/MM/dd, HH:mm")
    data object YearMonthDayWithTime : PreferredDateFormat("yyyy-MM-dd, HH:mm")
}
