package com.fibelatti.bookmarking.core.ui

public sealed class PreferredDateFormat(public val value: String) {
    public data object DayMonthYearWithTime : PreferredDateFormat("dd/MM/yy, HH:mm")
    public data object MonthDayYearWithTime : PreferredDateFormat("MM/dd/yy, HH:mm")
    public data object ShortYearMonthDayWithTime : PreferredDateFormat("yy/MM/dd, HH:mm")
    public data object YearMonthDayWithTime : PreferredDateFormat("yyyy-MM-dd, HH:mm")
}
