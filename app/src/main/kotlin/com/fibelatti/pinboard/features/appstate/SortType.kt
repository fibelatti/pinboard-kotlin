package com.fibelatti.pinboard.features.appstate

sealed class SortType(val index: Int)

data object ByDateAddedNewestFirst : SortType(index = 0)
data object ByDateAddedOldestFirst : SortType(index = 1)
data object ByDateModifiedNewestFirst : SortType(index = 2)
data object ByDateModifiedOldestFirst : SortType(index = 3)
data object ByTitleAlphabetical : SortType(index = 4)
data object ByTitleAlphabeticalReverse : SortType(index = 5)
