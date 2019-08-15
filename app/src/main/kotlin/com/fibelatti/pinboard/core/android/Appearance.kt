package com.fibelatti.pinboard.core.android

sealed class Appearance(val value: String)
object DarkTheme : Appearance("DARK")
object LightTheme : Appearance("LIGHT")
