package com.fibelatti.pinboard.features.appstate

sealed class SortType

object NewestFirst : SortType()
object OldestFirst : SortType()
