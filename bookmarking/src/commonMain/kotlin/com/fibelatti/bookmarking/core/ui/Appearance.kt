package com.fibelatti.bookmarking.core.ui

public sealed class Appearance(public val value: String) {
    public data object DarkTheme : Appearance("DARK")
    public data object LightTheme : Appearance("LIGHT")
    public data object SystemDefault : Appearance("SYSTEM_DEFAULT")
}
