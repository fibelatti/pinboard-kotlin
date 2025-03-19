package com.fibelatti.pinboard.features.appstate

sealed class SortType(val index: Int, val value: String)

data object ByDateAddedNewestFirst : SortType(index = 0, value = "BY_DATE_ADDED_NEWEST_FIRST")
data object ByDateAddedOldestFirst : SortType(index = 1, value = "BY_DATE_ADDED_OLDEST_FIRST")
data object ByDateModifiedNewestFirst : SortType(index = 2, value = "BY_DATE_MODIFIED_NEWEST_FIRST")
data object ByDateModifiedOldestFirst : SortType(index = 3, value = "BY_DATE_MODIFIED_OLDEST_FIRST")
data object ByTitleAlphabetical : SortType(index = 4, value = "BY_TITLE_ALPHABETICAL")
data object ByTitleAlphabeticalReverse : SortType(index = 5, value = "BY_TITLE_ALPHABETICAL_REVERSE")
