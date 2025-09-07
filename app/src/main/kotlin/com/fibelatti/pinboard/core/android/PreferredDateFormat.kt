package com.fibelatti.pinboard.core.android

sealed class PreferredDateFormat {

    abstract val value: String
    abstract val includeTime: Boolean

    data class DayMonthYearWithTime(
        override val includeTime: Boolean = true,
    ) : PreferredDateFormat() {

        override val value: String = if (includeTime) "dd/MM/yy, HH:mm" else "dd/MM/yy"
    }

    data class MonthDayYearWithTime(
        override val includeTime: Boolean = true,
    ) : PreferredDateFormat() {

        override val value: String = if (includeTime) "MM/dd/yy, HH:mm" else "MM/dd/yy"
    }

    data class ShortYearMonthDayWithTime(
        override val includeTime: Boolean = true,
    ) : PreferredDateFormat() {

        override val value: String = if (includeTime) "yy/MM/dd, HH:mm" else "yy/MM/dd"
    }

    data class YearMonthDayWithTime(
        override val includeTime: Boolean = true,
    ) : PreferredDateFormat() {

        override val value: String = if (includeTime) "yyyy-MM-dd, HH:mm" else "yyyy-MM-dd"
    }

    data object NoDate : PreferredDateFormat() {

        override val value: String = "no_date"
        override val includeTime: Boolean = false
    }
}
