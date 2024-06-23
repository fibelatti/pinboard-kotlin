package com.fibelatti.bookmarking.features.appstate

public sealed class SortType(public val index: Int)

public data object NewestFirst : SortType(index = 0)
public data object OldestFirst : SortType(index = 1)
public data object Alphabetical : SortType(index = 2)
public data object AlphabeticalReverse : SortType(index = 3)
