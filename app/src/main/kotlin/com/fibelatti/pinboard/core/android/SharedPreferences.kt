package com.fibelatti.pinboard.core.android

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

// region getSharedPreferences
fun Context.getSharedPreferences(name: String): SharedPreferences =
    getSharedPreferences(name, MODE_PRIVATE)

fun Context.getUserPreferences() = getSharedPreferences("user_preferences")
// endregion
