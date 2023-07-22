package com.fibelatti.pinboard.features.appstate

sealed class SortType

data object NewestFirst : SortType()
data object OldestFirst : SortType()
