package com.fibelatti.pinboard.features.appstate

sealed class SortType(val index: Int)

data object NewestFirst : SortType(index = 0)
data object OldestFirst : SortType(index = 1)
data object Alphabetical : SortType(index = 2)
data object AlphabeticalReverse : SortType(index = 3)
