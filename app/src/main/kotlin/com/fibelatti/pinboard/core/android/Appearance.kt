package com.fibelatti.pinboard.core.android

sealed class Appearance(val value: String) {
    data object DarkTheme : Appearance("DARK")
    data object LightTheme : Appearance("LIGHT")
    data object SystemDefault : Appearance("SYSTEM_DEFAULT")
}
