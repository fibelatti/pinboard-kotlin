package com.fibelatti.pinboard.core.android.composable

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import androidx.compose.ui.platform.LocalContext
import com.fibelatti.core.android.extension.findOwner

/**
 * Provides the [AppCompatActivity] belonging to the current [LocalContext].
 *
 * Alternative API to `LocalActivity` that returns an [AppCompatActivity] instead.
 */
val LocalAppCompatActivity = compositionLocalWithComputedDefaultOf<AppCompatActivity> {
    LocalContext.currentValue.findOwner() ?: error("No AppCompatActivity found in the Context hierarchy")
}
