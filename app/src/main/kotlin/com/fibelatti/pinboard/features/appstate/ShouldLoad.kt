package com.fibelatti.pinboard.features.appstate

sealed class ShouldLoad

object Loaded : ShouldLoad()
object ShouldLoadFirstPage : ShouldLoad()
data class ShouldLoadNextPage(val offset: Int) : ShouldLoad()
